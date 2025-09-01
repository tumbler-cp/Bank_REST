package com.example.bankcards.dto.request.app;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
