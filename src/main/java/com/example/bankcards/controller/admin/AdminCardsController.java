package com.example.bankcards.controller.admin;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.app.CardRequest;
import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.entity.app.CardStatus;
import com.example.bankcards.service.AdminCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class AdminCardsController {
    private final AdminCardService adminCardService;

    @PostMapping("/new")
    public CardResponse createCard(@RequestBody @Valid CardRequest cardRequest) {
        return adminCardService.create(cardRequest);
    }

    @PostMapping("/block")
    public CardResponse blockCard(@RequestBody Long cardId) {
        return adminCardService.updateCardStatus(cardId, CardStatus.BLOCKED);
    }

    @PostMapping("/activate")
    public CardResponse activateCard(@RequestBody Long cardId) {
        return adminCardService.updateCardStatus(cardId, CardStatus.ACTIVE);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCard(@RequestBody Long cardId) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get")
    public List<CardResponse> getCards(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
        @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
    ) {
        Order order = new Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
        return adminCardService.getAllCards(pageable);
    }

}
