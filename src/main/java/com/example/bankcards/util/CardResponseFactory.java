package com.example.bankcards.util;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.app.Card;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardResponseFactory {
    private final CardNumberEncryption cardNumberEncryption;

    public CardResponse fromCard(Card card) {
        return CardResponse
            .builder()
            .id(card.getId())
            .maskedNumber(
                cardNumberEncryption.maskCardNumber(
                    cardNumberEncryption.decryptCardNumber(card.getNumber())
                )
            )
            .expiryDate(card.getExpiryDate())
            .status(card.getStatus())
            .balance(card.getBalance())
            .build();
    }

    public List<CardResponse> fromList(List<Card> cards) {
        return cards.stream().map(
            c -> fromCard(c)
        ).toList();
    }
}
