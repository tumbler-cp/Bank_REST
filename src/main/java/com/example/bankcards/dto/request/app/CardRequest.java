package com.example.bankcards.dto.request.app;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.bankcards.entity.app.CardStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {
    @NotNull
    @NotBlank
    @Pattern(regexp = "^\\d{16}$")
    private String number;
    @NotNull
    private LocalDate expiryDate;
    @NotNull
    private CardStatus status;
    @NotNull
    @Min(0)
    private BigDecimal balance;
    @NotNull
    private Long userId;
}
