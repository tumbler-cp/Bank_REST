package com.example.bankcards.controller.cards;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.response.app.CardResponse;
import com.example.bankcards.service.UserCardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserCardController {
    private final UserCardService cardService;
    

    @GetMapping("/cards")
    public List<CardResponse> myCards(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
        @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
    ) {
        Order order = new Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
        return cardService.findUserCardsPaged(pageable);
    }


    
}
