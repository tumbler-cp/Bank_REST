package com.example.bankcards.dto.response.app;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.bankcards.entity.app.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private CardResponse from;
    private CardResponse to;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private TransactionStatus status;
}
