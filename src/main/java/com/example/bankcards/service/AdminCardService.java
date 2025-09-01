package com.example.bankcards.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.request.app.CardRequest;
import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.app.BlockRequest;
import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.app.CardBlockRequestStatus;
import com.example.bankcards.entity.app.CardStatus;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberEncryption;
import com.example.bankcards.util.CardResponseFactory;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminCardService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardResponseFactory cardResponseFactory;
    private final CardNumberEncryption cardNumberEncryption;
    private final BlockRequestRepository blockRequestRepository;

    public CardResponse create(CardRequest cardRequest) {
        return cardResponseFactory.fromCard(
            cardRepository.save(
                Card.builder()
                    .number(
                        cardNumberEncryption.encryptCardNumber(cardRequest.getNumber())
                    )
                    .expiryDate(cardRequest.getExpiryDate())
                    .status(cardRequest.getStatus())
                    .balance(cardRequest.getBalance())
                    .owner(
                        userRepository.findById(
                            cardRequest.getUserId()
                        ).orElseThrow(() -> new RuntimeException("User not found"))
                    )
                    .build()
            )
        );
    }
    
    public CardResponse updateCardStatus(Long cardId, CardStatus newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id: " + cardId));
        
        if (card.getStatus() == CardStatus.EXPIRED && newStatus == CardStatus.ACTIVE) {
            throw new IllegalStateException("Cannot activate expired card");
        }
        
        CardStatus oldStatus = card.getStatus();
        card.setStatus(newStatus);
        
        if (newStatus == CardStatus.BLOCKED) {
            var blocks = blockRequestRepository.findByCard(card);
            blocks.forEach(block -> {
                block.setStatus(CardBlockRequestStatus.APPROVED);
                blockRequestRepository.save(block);
            });
        }
        
        Card savedCard = cardRepository.save(card);
        log.info("Card status updated from {} to {} for card id: {}", 
                oldStatus, newStatus, cardId);
        
        return cardResponseFactory.fromCard(savedCard);
    }

    public void deleteCard(Long cardId) {
        var card = cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found"));
        cardRepository.delete(card);
    }

    public List<CardResponse> getAllCards(Pageable pageable) {
        return cardResponseFactory.fromList(
            cardRepository.findAll(pageable).toList()
        );
    }
}
