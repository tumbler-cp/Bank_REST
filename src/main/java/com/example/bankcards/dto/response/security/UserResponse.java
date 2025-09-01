package com.example.bankcards.dto.response.security;

import com.example.bankcards.entity.security.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
}
