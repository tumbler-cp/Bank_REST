package com.example.bankcards.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.app.CardStatus;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberEncryption;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CardService {
    private CardRepository cardRepository;
    private CardNumberEncryption cardNumberEncryption;

    public CardResponse createCard(String number, LocalDate expiry, User owner) {
        Card card = cardRepository.save(
            Card.builder()
                .owner(owner)
                .number(
                    cardNumberEncryption.encryptCardNumber(number)
                )
                .expiryDate(expiry)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal(0))
                .build()
        );
        return entityToDto(card);
    }

    public CardResponse updateCardStatus(Long cardId, CardStatus status) {
        Card card = cardRepository.findById(cardId)
            .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        card.setStatus(status);
        card = cardRepository.save(card);
        return entityToDto(card);
    }

    public List<CardResponse> getPage(Pageable pageable) {
        var cards = cardRepository.findAll(pageable);
        return cards.map(c -> entityToDto(c)).toList();
    }

    public void deleteCard(Long cardId) {
        cardRepository.delete(
            cardRepository
                .findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"))
            );
    }

    private CardResponse entityToDto(Card card) {
        return CardResponse.builder()
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

}
