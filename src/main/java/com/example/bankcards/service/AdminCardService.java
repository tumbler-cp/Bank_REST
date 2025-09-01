package com.example.bankcards.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.request.app.CardRequest;
import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.app.CardBlockRequestStatus;
import com.example.bankcards.entity.app.CardStatus;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberEncryption;
import com.example.bankcards.util.CardResponseFactory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasRole('ADMIN')")
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
    
    public CardResponse updateCardStatus(Long cardId, CardStatus cardStatus) {
        var card = cardRepository.findById(cardId).orElseThrow(() -> new RuntimeException("Card not found"));
        card.setId(cardId);
        card = cardRepository.save(card);
        if (card.getStatus() == CardStatus.BLOCKED) {
            var br = blockRequestRepository.findById(card.getId());
            if (br.isPresent()) {
                var res = br.get();
                res.setStatus(CardBlockRequestStatus.APPROVED);
                blockRequestRepository.save(res);
            }
        }
        return cardResponseFactory.fromCard(
            card
        );
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
