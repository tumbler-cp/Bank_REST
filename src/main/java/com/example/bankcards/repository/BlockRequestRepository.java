package com.example.bankcards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bankcards.entity.app.BlockRequest;

@Repository
public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {
    
}
