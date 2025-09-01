package com.example.bankcards.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.request.security.AuthRequest;
import com.example.bankcards.dto.response.security.UserResponse;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserService {
    private final AuthService authService;
    private final UserRepository userRepository;

    public UserResponse createUser(AuthRequest request) {
        var user = authService.createUser(request);
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .role(user.getRole())
            .build();
    }

    public List<UserResponse> findAllUser(Pageable pageable) {
        var users = userRepository.findAll(pageable);
        return users.map(user -> entityToDto(user)).toList();
    }

    private UserResponse entityToDto(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .role(user.getRole())
            .build();
    }

}
