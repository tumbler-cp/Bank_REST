package com.example.bankcards.controller;

import com.example.bankcards.controller.transactions.TransactionController;
import com.example.bankcards.dto.request.app.TransactionRequest;
import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.dto.response.app.TransactionResponse;
import com.example.bankcards.entity.app.TransactionStatus;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    void newTransaction_ShouldReturnTransactionResponse() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.50"));

        CardResponse fromCard = CardResponse.builder().id(1L).build();
        CardResponse toCard = CardResponse.builder().id(2L).build();
        
        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .from(fromCard)
                .to(toCard)
                .amount(new BigDecimal("100.50"))
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionService.processTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/transaction/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.from.id").value(1L))
                .andExpect(jsonPath("$.to.id").value(2L))
                .andExpect(jsonPath("$.amount").value(100.50))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(transactionService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void transactionHistory_ShouldReturnListOfTransactions() throws Exception {
        CardResponse card1 = CardResponse.builder().id(1L).build();
        CardResponse card2 = CardResponse.builder().id(2L).build();
        
        TransactionResponse transaction1 = TransactionResponse.builder()
                .id(1L)
                .from(card1)
                .to(card2)
                .amount(new BigDecimal("50.00"))
                .timestamp(LocalDateTime.now().minusDays(1))
                .status(TransactionStatus.SUCCESS)
                .build();

        TransactionResponse transaction2 = TransactionResponse.builder()
                .id(2L)
                .from(card2)
                .to(card1)
                .amount(new BigDecimal("75.50"))
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();

        List<TransactionResponse> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionService.getAll(any(Pageable.class))).thenReturn(transactions);

        mockMvc.perform(get("/transaction/history")
                .param("page", "0")
                .param("pageSize", "5")
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].amount").value(75.50))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        verify(transactionService, times(1)).getAll(any(Pageable.class));
    }

    @Test
    void transactionHistory_WithDefaultParameters_ShouldUseDefaults() throws Exception {
        when(transactionService.getAll(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/transaction/history"))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).getAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"))
        );
    }

    @Test
    void transactionHistory_WithCustomSort_ShouldUseCorrectSort() throws Exception {
        when(transactionService.getAll(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/transaction/history")
                .param("sort", "timestamp,desc"))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).getAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "timestamp"))
        );
    }

    @Test
    void transactionHistory_WithDifferentPagination_ShouldUseCorrectPageable() throws Exception {
        when(transactionService.getAll(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/transaction/history")
                .param("page", "2")
                .param("pageSize", "20")
                .param("sort", "amount,desc"))
                .andExpect(status().isOk());

        verify(transactionService, times(1)).getAll(
            PageRequest.of(2, 20, Sort.by(Sort.Direction.DESC, "amount"))
        );
    }

}
