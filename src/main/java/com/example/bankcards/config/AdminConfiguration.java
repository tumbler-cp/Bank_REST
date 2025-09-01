package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.example.bankcards.dto.request.security.AuthRequest;
import com.example.bankcards.entity.security.Role;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final AuthService authService;
    
    @Value("${admin.name}")
    private String adminName;

    @Value("${admin.pass}")
    private String adminPass;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (!userRepository.findByRole(Role.ROLE_ADMIN).isEmpty()) return;
        User user = authService.createUser(
            AuthRequest.builder()
                .username(adminName)
                .password(adminPass)
                .build()
        );
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    

}
