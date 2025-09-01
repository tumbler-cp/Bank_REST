package com.example.bankcards.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.request.app.TransactionRequest;
import com.example.bankcards.dto.response.app.TransactionResponse;
import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.app.CardStatus;
import com.example.bankcards.entity.app.Transaction;
import com.example.bankcards.entity.app.TransactionStatus;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.CardResponseFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AuthService authService;
    private final CardResponseFactory cardResponseFactory;

    public TransactionResponse processTransaction(TransactionRequest request) {
        var transaction = createAndProcessTransaction(request);
        return entityToDto(transaction);
    }

    @Transactional
    public Transaction createAndProcessTransaction(TransactionRequest request) {
        User user = authService.getCurrentUser();
        Card fromCard = cardRepository.findByOwnerAndId(user, request.getFromCardId()).orElseThrow(RuntimeException::new);
        Card toCard = cardRepository.findByOwnerAndId(user, request.getToCardId()).orElseThrow(RuntimeException::new);

        Transaction transaction = Transaction.builder()
            .from(fromCard)
            .to(toCard)
            .amount(request.getAmount())
            .timestamp(LocalDateTime.now())
            .status(TransactionStatus.PENDING)
            .build();
        
            try {
                validateTransaction(fromCard, toCard, request.getAmount());
                processTransaction(transaction);
                transaction.setStatus(TransactionStatus.SUCCESS);
            } catch (Exception e) {
                transaction.setStatus(TransactionStatus.FAILED);
                throw new RuntimeException("Error while processing transaction", e);
            } finally {
                transactionRepository.save(transaction);
            }
        
        return transaction;
    }

    private void processTransaction(Transaction transaction) {
        Card from = transaction.getFrom();
        Card to = transaction.getTo();
        BigDecimal amount = transaction.getAmount();

        from.setBalance(from.getBalance().subtract(amount));
        cardRepository.save(from);

        to.setBalance(to.getBalance().add(amount));
        cardRepository.save(to);
    }

    private void validateTransaction(Card from, Card to, BigDecimal amount) {
        if (from.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Card is not active");
        }
        if (to.getStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("Card is not active");
        }
        if (from.getExpiryDate().isBefore(LocalDate.now())) {
            from.setStatus(CardStatus.EXPIRED);
            cardRepository.save(from);
            throw new RuntimeException("Card is expired");
        }
        if (to.getExpiryDate().isBefore(LocalDate.now())) {
            to.setStatus(CardStatus.EXPIRED);
            cardRepository.save(to);
            throw new RuntimeException("Card is expired");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transaction amount should be positive");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Not enough balance");
        }
        if (from.equals(to)) {
            throw new RuntimeException("Transaction between same card is not allowed");
        }

    }

    private TransactionResponse entityToDto(Transaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .from(cardResponseFactory.fromCard(transaction.getFrom()))
            .to(cardResponseFactory.fromCard(transaction.getTo()))
            .amount(transaction.getAmount())
            .timestamp(transaction.getTimestamp())
            .status(transaction.getStatus())
            .build();
    }

    public List<TransactionResponse> getAll(Pageable pageable) {
        var transactions = transactionRepository.findAll(pageable);
        return transactions.stream().map(
            transaction -> entityToDto(transaction)
        ).toList();
    }

}
