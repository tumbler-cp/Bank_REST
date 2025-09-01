package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.bankcards.dto.request.app.TransactionRequest;
import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.dto.response.app.TransactionResponse;
import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.app.CardStatus;
import com.example.bankcards.entity.app.Transaction;
import com.example.bankcards.entity.app.TransactionStatus;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.CardResponseFactory;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AuthService authService;

    @Mock
    private CardResponseFactory cardResponseFactory;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Card activeCard1;
    private Card activeCard2;
    private Card expiredCard;
    private Card inactiveCard;
    private TransactionRequest validRequest;
    private TransactionRequest invalidAmountRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        activeCard1 = Card.builder()
            .id(1L)
            .owner(testUser)
            .balance(new BigDecimal("1000.00"))
            .status(CardStatus.ACTIVE)
            .expiryDate(LocalDate.now().plusYears(1))
            .build();

        activeCard2 = Card.builder()
            .id(2L)
            .owner(testUser)
            .balance(new BigDecimal("500.00"))
            .status(CardStatus.ACTIVE)
            .expiryDate(LocalDate.now().plusYears(1))
            .build();

        expiredCard = Card.builder()
            .id(3L)
            .owner(testUser)
            .balance(new BigDecimal("1000.00"))
            .status(CardStatus.ACTIVE)
            .expiryDate(LocalDate.now().minusDays(1))
            .build();

        inactiveCard = Card.builder()
            .id(4L)
            .owner(testUser)
            .balance(new BigDecimal("1000.00"))
            .status(CardStatus.BLOCKED)
            .expiryDate(LocalDate.now().plusYears(1))
            .build();

        validRequest = TransactionRequest.builder()
            .fromCardId(1L)
            .toCardId(2L)
            .amount(new BigDecimal("100.00"))
            .build();

        invalidAmountRequest = TransactionRequest.builder()
            .fromCardId(1L)
            .toCardId(2L)
            .amount(new BigDecimal("-100.00"))
            .build();
    }

    @Test
    void processTransaction_ValidRequest_ReturnsSuccessResponse() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.of(activeCard2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });
        when(cardRepository.saveAll(anyList())).thenReturn(List.of(activeCard1, activeCard2));
        
        when(cardResponseFactory.fromCard(any(Card.class))).thenReturn(new CardResponse());

        TransactionResponse result = transactionService.processTransaction(validRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        
        verify(authService, times(1)).getCurrentUser();
        verify(cardRepository, times(2)).findByOwnerAndId(any(), anyLong());
        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
        verify(cardRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createAndProcessTransaction_SourceCardNotFound_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(validRequest);
        });
        
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void createAndProcessTransaction_DestinationCardNotFound_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(validRequest);
        });
        
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void createAndProcessTransaction_SourceCardInactive_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(inactiveCard));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.of(activeCard2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(validRequest);
        });
        
        assertEquals("Source card is not active", exception.getMessage());
        verify(cardRepository, never()).saveAll(anyList());
        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
    }

    @Test
    void createAndProcessTransaction_SourceCardExpired_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(expiredCard));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.of(activeCard2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(validRequest);
        });
        
        assertEquals("Source card is expired", exception.getMessage());
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void createAndProcessTransaction_NegativeAmount_ThrowsException() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.of(activeCard2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(invalidAmountRequest);
        });
        
        assertEquals("Transaction amount should be positive", exception.getMessage());
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void createAndProcessTransaction_InsufficientBalance_ThrowsException() {
        TransactionRequest largeAmountRequest = TransactionRequest.builder()
            .fromCardId(1L)
            .toCardId(2L)
            .amount(new BigDecimal("2000.00"))
            .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.of(activeCard2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(largeAmountRequest);
        });
        
        assertEquals("Not enough balance", exception.getMessage());
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void createAndProcessTransaction_SameCard_ThrowsException() {
        TransactionRequest sameCardRequest = TransactionRequest.builder()
            .fromCardId(1L)
            .toCardId(1L)
            .amount(new BigDecimal("100.00"))
            .build();

        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(activeCard1));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(sameCardRequest);
        });
        
        assertEquals("Transaction between same card is not allowed", exception.getMessage());
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void createAndProcessTransaction_ProcessTransactionUpdatesBalances() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.of(activeCard2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });
        
        when(cardRepository.saveAll(any())).thenAnswer(invocation -> 
            invocation.getArgument(0)
        );

        BigDecimal initialFromBalance = activeCard1.getBalance();
        BigDecimal initialToBalance = activeCard2.getBalance();
        BigDecimal amount = new BigDecimal("100.00");

        Transaction result = transactionService.createAndProcessTransaction(validRequest);

        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        
        verify(cardRepository).saveAll(argThat(cardsIterable -> {
            List<Card> cards = new ArrayList<>();
            cardsIterable.forEach(cards::add);
            
            assertEquals(2, cards.size());
            
            Card updatedFrom = cards.stream().filter(c -> c.getId().equals(1L)).findFirst().get();
            Card updatedTo = cards.stream().filter(c -> c.getId().equals(2L)).findFirst().get();
            
            assertEquals(initialFromBalance.subtract(amount), updatedFrom.getBalance());
            assertEquals(initialToBalance.add(amount), updatedTo.getBalance());
            return true;
        }));
    }

    @Test
    void getAll_ReturnsPaginatedTransactions() {
        Transaction transaction = Transaction.builder()
            .id(1L)
            .from(activeCard1)
            .to(activeCard2)
            .amount(new BigDecimal("100.00"))
            .timestamp(LocalDateTime.now())
            .status(TransactionStatus.SUCCESS)
            .user(testUser)
            .build();

        Pageable pageable = Pageable.ofSize(10);
        when(transactionRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(transaction)));
        when(cardResponseFactory.fromCard(any(Card.class))).thenReturn(new CardResponse());

        List<TransactionResponse> result = transactionService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).findAll(pageable);
        verify(cardResponseFactory, times(2)).fromCard(any(Card.class));
    }

    @Test
    void createAndProcessTransaction_ExceptionDuringProcessing_SetsFailedStatus() {
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(cardRepository.findByOwnerAndId(testUser, 1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findByOwnerAndId(testUser, 2L)).thenReturn(Optional.of(activeCard2));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });
        
        doThrow(new RuntimeException("Database error")).when(cardRepository).saveAll(anyList());

        assertThrows(RuntimeException.class, () -> {
            transactionService.createAndProcessTransaction(validRequest);
        });
        
        
        verify(transactionRepository, atLeast(2)).save(argThat(transaction -> 
            transaction.getStatus() == TransactionStatus.FAILED
        ));
    }
}
