package com.bookworm.service;

import com.bookworm.dto.AddressDto;
import com.bookworm.dto.ReviewDto;
import com.bookworm.dto.UserDto;
import com.bookworm.entity.AddressEntity;
import com.bookworm.entity.UserEntity;
import com.bookworm.exception.ConflictException;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.AddressRepository;
import com.bookworm.repository.UserRepository;
import com.bookworm.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserDto.AuthResponse register(UserDto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered");
        }
        UserEntity user = UserEntity.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .build();
        userRepository.save(user);
        String token = tokenProvider.generateToken(user.getId());
        return new UserDto.AuthResponse(token, "Bearer", tokenProvider.getExpirationMs());
    }

    public UserDto.AuthResponse login(UserDto.LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        // Extract the user ID that was set as principal name
        UserEntity user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = tokenProvider.generateToken(user.getId());
        return new UserDto.AuthResponse(token, "Bearer", tokenProvider.getExpirationMs());
    }

    public UserDto.UserResponse getProfile(UUID userId) {
        UserEntity user = findById(userId);
        return toResponse(user);
    }

    public List<AddressDto.AddressResponse> listAddresses(UUID userId) {
        return addressRepository.findByUserId(userId)
                .stream().map(this::toAddressResponse).toList();
    }

    @Transactional
    public AddressDto.AddressResponse createAddress(UUID userId, AddressDto.AddressRequest req) {
        UserEntity user = findById(userId);
        AddressEntity address = AddressEntity.builder()
                .user(user)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .addressLine1(req.addressLine1())
                .addressLine2(req.addressLine2())
                .city(req.city())
                .state(req.state())
                .pinCode(req.pinCode())
                .country(req.country())
                .email(req.email())
                .phoneNumber(req.phoneNumber())
                .build();
        return toAddressResponse(addressRepository.save(address));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private UserEntity findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserDto.UserResponse toResponse(UserEntity u) {
        return new UserDto.UserResponse(u.getId(), u.getEmail(),
                u.getFirstName(), u.getLastName(), u.getGiftPoints());
    }

    public AddressDto.AddressResponse toAddressResponse(AddressEntity a) {
        return new AddressDto.AddressResponse(a.getId(), a.getFirstName(), a.getLastName(),
                a.getAddressLine1(), a.getAddressLine2(), a.getCity(), a.getState(),
                a.getPinCode(), a.getCountry(), a.getEmail(), a.getPhoneNumber());
    }
}
