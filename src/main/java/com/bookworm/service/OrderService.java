package com.bookworm.service;

import com.bookworm.dto.AddressDto;
import com.bookworm.dto.CheckoutDto;
import com.bookworm.dto.OrderDto;
import com.bookworm.dto.PaginationDto;
import com.bookworm.dto.PaymentDto;
import com.bookworm.entity.*;
import com.bookworm.exception.BadRequestException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final PaymentTransactionRepository paymentRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        CheckoutService checkoutService,
                        PaymentTransactionRepository paymentRepository,
                        AddressRepository addressRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.paymentRepository = paymentRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public OrderDto.OrderListResponse listOrders(UUID userId, int page, int pageSize) {
        Page<OrderEntity> result = orderRepository.findByUserId(
                userId, PageRequest.of(page - 1, pageSize, Sort.by("placedAt").descending()));
        var data = result.getContent().stream().map(this::toResponse).toList();
        var pagination = new PaginationDto(result.getNumber() + 1, result.getSize(),
                result.getTotalElements(), result.getTotalPages());
        return new OrderDto.OrderListResponse(data, pagination);
    }

    public OrderDto.OrderResponse getOrder(UUID userId, UUID orderId) {
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    @Transactional
    public PaymentDto.PaymentResponse initiatePayment(UUID userId, PaymentDto.PaymentRequest req) {
        CartEntity cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        CheckoutDto.CheckoutSummaryResponse summary = checkoutService.getSummary(userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create the order
        OrderEntity order = OrderEntity.builder().user(user)
                .subtotal(summary.subtotal()).tax(summary.tax())
                .deliveryCharges(summary.deliveryCharges())
                .couponCode(summary.couponCode())
                .discount(summary.discount())
                .totalPayable(summary.totalPayable())
                .status(OrderEntity.OrderStatus.CONFIRMED)
                .build();

        // Snapshot items
        for (CartItemEntity ci : cart.getItems()) {
            OrderItemEntity oi = OrderItemEntity.builder()
                    .order(order).book(ci.getBook())
                    .quantity(ci.getQuantity()).unitPrice(ci.getUnitPrice())
                    .build();
            order.getItems().add(oi);
        }
        orderRepository.save(order);

        // Record payment
        PaymentTransactionEntity.PaymentMethod method =
                PaymentTransactionEntity.PaymentMethod.valueOf(req.paymentMethod().toUpperCase());
        PaymentTransactionEntity tx = PaymentTransactionEntity.builder()
                .order(order).paymentMethod(method)
                .payableAmount(summary.totalPayable())
                .status(PaymentTransactionEntity.PaymentStatus.SUCCESS)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(tx);

        // Clear cart
        cart.getItems().clear();
        cartService.getOrCreateCart(userId);

        return toPaymentResponse(tx);
    }

    public PaymentDto.PaymentResponse getPaymentStatus(UUID transactionId) {
        PaymentTransactionEntity tx = paymentRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        return toPaymentResponse(tx);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private OrderDto.OrderResponse toResponse(OrderEntity o) {
        List<OrderDto.OrderItemResponse> items = o.getItems().stream().map(i ->
                new OrderDto.OrderItemResponse(i.getId(), toBookSummary(i.getBook()),
                        i.getQuantity(), i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
        ).toList();

        AddressDto.AddressResponse addrResp = o.getDeliveryAddress() != null
                ? toAddressResponse(o.getDeliveryAddress()) : null;

        CheckoutDto.CheckoutSummaryResponse summary = new CheckoutDto.CheckoutSummaryResponse(
                o.getSubtotal(), o.getTax(), o.getDeliveryCharges(),
                o.getCouponCode(), o.getDiscount(), o.getTotalPayable(), o.getCurrency());

        PaymentDto.PaymentResponse payResp = paymentRepository.findByOrderId(o.getId())
                .map(this::toPaymentResponse).orElse(null);

        return new OrderDto.OrderResponse(o.getId(), o.getUser().getId(), items, addrResp,
                summary, payResp, o.getStatus().name(), o.getPlacedAt(), o.getEstimatedDelivery());
    }

    private PaymentDto.PaymentResponse toPaymentResponse(PaymentTransactionEntity tx) {
        return new PaymentDto.PaymentResponse(tx.getId(), tx.getOrder().getId(),
                tx.getStatus().name(), tx.getPayableAmount(), tx.getCurrency(),
                tx.getPaymentMethod().name(), tx.getPaidAt());
    }

    private com.bookworm.dto.BookDto.BookSummaryDto toBookSummary(BookEntity b) {
        return new com.bookworm.dto.BookDto.BookSummaryDto(
                b.getId(), b.getTitle(),
                new com.bookworm.dto.BookDto.AuthorRefDto(b.getAuthor().getId(), b.getAuthor().getName()),
                b.getCoverImageUrl(), b.getFormat().name(), b.getCategories(),
                b.getPrice(), b.getCurrency(), b.getEstimatedDelivery(), b.getRating());
    }

    private AddressDto.AddressResponse toAddressResponse(AddressEntity a) {
        return new AddressDto.AddressResponse(a.getId(), a.getFirstName(), a.getLastName(),
                a.getAddressLine1(), a.getAddressLine2(), a.getCity(), a.getState(),
                a.getPinCode(), a.getCountry(), a.getEmail(), a.getPhoneNumber());
    }
}
