package com.example.bankcards.controller.transactions;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.app.TransactionRequest;
import com.example.bankcards.dto.response.app.TransactionResponse;
import com.example.bankcards.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    
    @PostMapping("/new")
    public TransactionResponse newTransaction(@RequestBody @Valid TransactionRequest request) {
        return transactionService.processTransaction(request);
    }

    @GetMapping("/history")
    public List<TransactionResponse> transactionHistory(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
        @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
    ) {
        Order order = new Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
        return transactionService.getAll(pageable);
    }
    
}
