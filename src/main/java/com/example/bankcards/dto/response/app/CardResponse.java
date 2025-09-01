package com.example.bankcards.dto.response.app;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.bankcards.entity.app.CardStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardResponse {
    private Long id;
    private String maskedNumber;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
