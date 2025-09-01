package com.example.bankcards.dto.response.app;

import com.example.bankcards.entity.app.CardBlockRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockRequestResponse {
    private Long id;
    private Long cardId;
    private String maskedNumber;
    private CardBlockRequestStatus status;
}
