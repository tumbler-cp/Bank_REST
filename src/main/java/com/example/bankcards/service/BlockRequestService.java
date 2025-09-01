package com.example.bankcards.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.response.app.BlockRequestResponse;
import com.example.bankcards.entity.app.BlockRequest;
import com.example.bankcards.entity.app.CardBlockRequestStatus;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.util.CardNumberEncryption;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BlockRequestService {
    private final BlockRequestRepository blockRequestRepository;
    private final UserCardService cardService;
    private final CardNumberEncryption cardNumberEncryption;

    public BlockRequestResponse createBlockRequst(Long cardId) {
        var card = cardService.findUserCard(cardId);

        var br = blockRequestRepository.save(
            BlockRequest.builder()
                .card(card)
                .status(CardBlockRequestStatus.PENDING)
                .build()
        );
        return entityToDto(br);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<BlockRequestResponse> getAllRequests(Pageable pageable) {
        var requests = blockRequestRepository.findAll(pageable);
        return requests.stream().map(r -> entityToDto(r)).toList();
    }

    private BlockRequestResponse entityToDto(BlockRequest blockRequest) {
        return BlockRequestResponse.builder()
            .id(blockRequest.getId())
            .cardId(blockRequest.getCard().getId())
            .maskedNumber(
                cardNumberEncryption.maskCardNumber(
                    cardNumberEncryption.decryptCardNumber(blockRequest.getCard().getNumber())
                )
            )
            .status(blockRequest.getStatus())
            .build();
    }
}
