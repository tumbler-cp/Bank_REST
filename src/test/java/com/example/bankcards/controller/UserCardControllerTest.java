package com.example.bankcards.controller;

import com.example.bankcards.controller.cards.UserCardController;
import com.example.bankcards.dto.response.app.BlockRequestResponse;
import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.app.CardBlockRequestStatus;
import com.example.bankcards.service.BlockRequestService;
import com.example.bankcards.service.UserCardService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserCardService userCardService;

    @Mock
    private BlockRequestService blockRequestService;

    @InjectMocks
    private UserCardController userCardController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userCardController).build();
    }

    @Test
    void myCards_ShouldReturnListOfUserCards() throws Exception {
        CardResponse card1 = new CardResponse();
        card1.setId(1L);
        card1.setMaskedNumber("**** **** **** 5678");
        
        CardResponse card2 = new CardResponse();
        card2.setId(2L);
        card2.setMaskedNumber("**** **** **** 4321");
        
        List<CardResponse> cards = Arrays.asList(card1, card2);
        
        when(userCardService.findUserCardsPaged(any(Pageable.class))).thenReturn(cards);

        mockMvc.perform(get("/usercards/cards")
                .param("page", "0")
                .param("pageSize", "5")
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].maskedNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].maskedNumber").value("**** **** **** 4321"));

        verify(userCardService, times(1)).findUserCardsPaged(any(Pageable.class));
    }

    @Test
    void myCards_WithDefaultParameters_ShouldUseDefaults() throws Exception {
        when(userCardService.findUserCardsPaged(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/usercards/cards"))
                .andExpect(status().isOk());

        verify(userCardService, times(1)).findUserCardsPaged(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"))
        );
    }

    @Test
    void myCards_WithCustomSort_ShouldUseCorrectSort() throws Exception {
        when(userCardService.findUserCardsPaged(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/usercards/cards")
                .param("sort", "cardNumber,desc"))
                .andExpect(status().isOk());

        verify(userCardService, times(1)).findUserCardsPaged(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "cardNumber"))
        );
    }

    @Test
    void cardBalance_ShouldReturnCardBalance() throws Exception {
        Long cardId = 1L;
        BigDecimal balance = new BigDecimal("1500.75");

        Card card = new Card();
        card.setId(cardId);
        card.setBalance(balance);
        
        when(userCardService.findUserCard(eq(cardId))).thenReturn(card);

        mockMvc.perform(get("/usercards/balance")
                .param("cardId", cardId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.75"));

        verify(userCardService, times(1)).findUserCard(eq(cardId));
    }


    @Test
    void blockCardRequest_ShouldReturnBlockRequestResponse() throws Exception {
        Long cardId = 1L;
        BlockRequestResponse blockRequestResponse = new BlockRequestResponse();
        blockRequestResponse.setId(100L);
        blockRequestResponse.setCardId(cardId);
        blockRequestResponse.setStatus(CardBlockRequestStatus.PENDING);
        
        when(blockRequestService.createBlockRequst(eq(cardId))).thenReturn(blockRequestResponse);

        mockMvc.perform(post("/usercards/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.cardId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(blockRequestService, times(1)).createBlockRequst(eq(cardId));
    }

    @Test
    void blockCardRequest_WithInvalidCardId_ShouldReturnBadRequest() throws Exception {
        when(blockRequestService.createBlockRequst(any())).thenThrow(new IllegalArgumentException("Invalid card ID"));

        mockMvc.perform(post("/usercards/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString("invalid")))
                .andExpect(status().isBadRequest());

        verify(blockRequestService, never()).createBlockRequst(any());
    }
}