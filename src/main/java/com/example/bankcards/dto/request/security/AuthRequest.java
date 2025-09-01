package com.example.bankcards.dto.request.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;

}
