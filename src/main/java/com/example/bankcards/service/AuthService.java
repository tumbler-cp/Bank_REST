package com.example.bankcards.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.request.security.AuthRequest;
import com.example.bankcards.dto.response.security.TokenResponse;
import com.example.bankcards.entity.security.Role;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public User createUser(AuthRequest request) {
        var user = User
            .builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .build();
        return userRepository.save(user);
            
    }

    public TokenResponse signUp(AuthRequest request) {
        var user = createUser(request);
        return generateTokenResponse(user);
    }

    public TokenResponse signIn(AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword())
        );
        
        var user = userRepository.findByUsername(request.getUsername());
        if (user == null) throw new RuntimeException("User not found");

        return generateTokenResponse(user);
    }

    public User getCurrentUser() {
        return userRepository.findByUsername(
            SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    private TokenResponse generateTokenResponse(User user) {
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenService.save(refreshToken, user);

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

}
