package com.example.bankcards.controller.cards;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.service.UserCardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/my")
@RequiredArgsConstructor
public class UserCardController {

    private final UserCardService cardService;
    
}
