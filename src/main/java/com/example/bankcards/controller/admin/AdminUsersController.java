package com.example.bankcards.controller.admin;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.request.security.AuthRequest;
import com.example.bankcards.dto.response.security.UserResponse;
import com.example.bankcards.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class AdminUsersController {
    private final AdminUserService adminUserService;

    @PostMapping("/new")
    public ResponseEntity<UserResponse> createUser(
        @RequestBody @Valid AuthRequest newUser) {
        log.debug("Received new user: {}", newUser);
        return new ResponseEntity<UserResponse>(adminUserService.createUser(newUser), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get")
    public List<UserResponse> getAllUsers(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
        @RequestParam(name = "sort", defaultValue = "id,asc") String[] sort
    ) {
        Order order = new Order(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));
        return adminUserService.findAllUser(pageable);
    }
}
