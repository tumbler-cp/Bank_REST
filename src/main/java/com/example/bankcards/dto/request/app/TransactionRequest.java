package com.example.bankcards.dto.request.app;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
