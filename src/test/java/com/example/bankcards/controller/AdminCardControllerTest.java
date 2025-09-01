package com.example.bankcards.controller;

import com.example.bankcards.controller.admin.AdminCardsController;
import com.example.bankcards.dto.request.app.CardRequest;
import com.example.bankcards.dto.response.app.BlockRequestResponse;
import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.app.CardStatus;
import com.example.bankcards.service.AdminCardService;
import com.example.bankcards.service.BlockRequestService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminCardService adminCardService;

    @Mock
    private BlockRequestService blockRequestService;

    @InjectMocks
    private AdminCardsController adminCardsController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminCardsController).build();
    }

    @Test
    void blockCard_ShouldReturnBlockedCard() throws Exception {
        Long cardId = 1L;
        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(cardId);
        cardResponse.setStatus(CardStatus.BLOCKED);
        
        when(adminCardService.updateCardStatus(eq(cardId), eq(CardStatus.BLOCKED))).thenReturn(cardResponse);

        mockMvc.perform(post("/cards/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        verify(adminCardService, times(1)).updateCardStatus(eq(cardId), eq(CardStatus.BLOCKED));
    }

    @Test
    void activateCard_ShouldReturnActivatedCard() throws Exception {
        Long cardId = 1L;
        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(cardId);
        cardResponse.setStatus(CardStatus.ACTIVE);
        
        when(adminCardService.updateCardStatus(eq(cardId), eq(CardStatus.ACTIVE))).thenReturn(cardResponse);

        mockMvc.perform(post("/cards/activate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(adminCardService, times(1)).updateCardStatus(eq(cardId), eq(CardStatus.ACTIVE));
    }

    @Test
    void deleteCard_ShouldReturnOk() throws Exception {
        Long cardId = 1L;
        doNothing().when(adminCardService).deleteCard(cardId);

        mockMvc.perform(delete("/cards/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardId)))
                .andExpect(status().isOk());

        verify(adminCardService, times(1)).deleteCard(cardId);
    }

    @Test
    void getCards_ShouldReturnListOfCards() throws Exception {
        CardResponse card1 = new CardResponse();
        card1.setId(1L);
        CardResponse card2 = new CardResponse();
        card2.setId(2L);
        List<CardResponse> cards = Arrays.asList(card1, card2);
        
        when(adminCardService.getAllCards(any(Pageable.class))).thenReturn(cards);

        mockMvc.perform(get("/cards/get")
                .param("page", "0")
                .param("pageSize", "5")
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(adminCardService, times(1)).getAllCards(any(Pageable.class));
    }

    @Test
    void getCards_WithDefaultParameters_ShouldUseDefaults() throws Exception {
        when(adminCardService.getAllCards(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/cards/get"))
                .andExpect(status().isOk());

        verify(adminCardService, times(1)).getAllCards(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    void getBlockRequests_ShouldReturnListOfBlockRequests() throws Exception {
        BlockRequestResponse request1 = new BlockRequestResponse();
        request1.setId(1L);
        BlockRequestResponse request2 = new BlockRequestResponse();
        request2.setId(2L);
        List<BlockRequestResponse> requests = Arrays.asList(request1, request2);
        
        when(blockRequestService.getAllRequests(any(Pageable.class))).thenReturn(requests);

        mockMvc.perform(get("/cards/blocks")
                .param("page", "1")
                .param("pageSize", "10")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(blockRequestService, times(1)).getAllRequests(any(Pageable.class));
    }

    @Test
    void getBlockRequests_WithDefaultParameters_ShouldUseDefaults() throws Exception {
        when(blockRequestService.getAllRequests(any(Pageable.class))).thenReturn(List.of());

        mockMvc.perform(get("/cards/blocks"))
                .andExpect(status().isOk());

        verify(blockRequestService, times(1)).getAllRequests(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id")));
    }

    @Test
    void createCard_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        CardRequest invalidRequest = new CardRequest();

        mockMvc.perform(post("/cards/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(adminCardService, never()).create(any(CardRequest.class));
    }
}