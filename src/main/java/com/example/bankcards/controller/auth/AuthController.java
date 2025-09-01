package com.example.bankcards.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.security.AuthRequest;
import com.example.bankcards.dto.response.security.TokenResponse;
import com.example.bankcards.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signUp(
        @RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<TokenResponse> signIn(
        @RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.signIn(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
        @RequestBody String refreshToken
    ) {
        return ResponseEntity.ok(authService.refreshTokens(refreshToken));
    }


}
