package com.bookworm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class AddressDto {

    public record AddressResponse(
            UUID addressId,
            String firstName,
            String lastName,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String pinCode,
            String country,
            String email,
            String phoneNumber
    ) {}

    public record AddressRequest(
            String addressId,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String addressLine1,
            String addressLine2,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank String pinCode,
            @NotBlank String country,
            @NotBlank @Email String email,
            @NotBlank String phoneNumber
    ) {}
}
