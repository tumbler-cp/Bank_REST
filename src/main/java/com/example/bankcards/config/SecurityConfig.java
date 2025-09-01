package com.example.bankcards.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.example.bankcards.security.JwtFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(
            request -> {
                var corsConfiguration = new CorsConfiguration();
                corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                corsConfiguration.setAllowedHeaders(List.of("*"));
                corsConfiguration.setAllowCredentials(false);
                return corsConfiguration;
            }
        ))
        .authorizeHttpRequests(
            request -> request
                .requestMatchers("/auth/**")
                .permitAll()
                .requestMatchers("/swagger-ui/**")
                .permitAll()
                .requestMatchers("/swagger-ui")
                .permitAll()
                .requestMatchers("/docs/**")
                .permitAll()
                .requestMatchers("/actuator/**")
                .permitAll()
                .requestMatchers("/swagger-ui.html")
                .permitAll()
                .anyRequest()
                .authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
}
