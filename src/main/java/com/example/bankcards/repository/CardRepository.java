package com.example.bankcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.security.User;
import java.util.List;



@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    Page<Card> findByOwner(User owner, Pageable pageable);
    List<Card> findByOwnerAndId(User owner, Long id);
}
