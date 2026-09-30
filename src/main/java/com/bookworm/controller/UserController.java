package com.bookworm.controller;

import com.bookworm.dto.AddressDto;
import com.bookworm.dto.UserDto;
import com.bookworm.service.UserService;
import com.bookworm.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ── Auth ──────────────────────────────────────────────────────────────

    @PostMapping("/auth/register")
    public ResponseEntity<UserDto.AuthResponse> register(
            @Valid @RequestBody UserDto.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(req));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UserDto.AuthResponse> login(
            @Valid @RequestBody UserDto.LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    // ── Profile ───────────────────────────────────────────────────────────

    @GetMapping("/users/me")
    public ResponseEntity<UserDto.UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile(SecurityUtils.currentUserId()));
    }

    @GetMapping("/users/me/addresses")
    public ResponseEntity<List<AddressDto.AddressResponse>> listAddresses() {
        return ResponseEntity.ok(userService.listAddresses(SecurityUtils.currentUserId()));
    }

    @PostMapping("/users/me/addresses")
    public ResponseEntity<AddressDto.AddressResponse> createAddress(
            @Valid @RequestBody AddressDto.AddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createAddress(SecurityUtils.currentUserId(), req));
    }
}
