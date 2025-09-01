package com.example.bankcards.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bankcards.entity.app.BlockRequest;
import com.example.bankcards.entity.app.Card;


@Repository
public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {
    List<BlockRequest> findByCard(Card card);
}
