package com.example.bankcards.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardResponseFactory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCardService {
    private final CardRepository cardRepository;
    private final AuthService authService;
    private final CardResponseFactory cardResponseFactory;

    public List<CardResponse> findUserCardsPaged(Pageable pageable) {
        User user = authService.getCurrentUser();
        return cardResponseFactory.fromList(
            cardRepository.findByOwner(user, pageable)
                .toList()
        );
    }

    public CardResponse findUserCard(Long cardId) {
        User user = authService.getCurrentUser();
        var card = cardRepository.findByOwnerAndId(user, cardId);
        if (card.size() == 0) throw new RuntimeException("Card not found");
        return cardResponseFactory.fromCard(card.get(0));
    }

}
