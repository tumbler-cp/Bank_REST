package com.example.bankcards.util;

import org.springframework.data.jpa.domain.Specification;

import com.example.bankcards.entity.app.Card;
import com.example.bankcards.entity.security.User;

public class CardSpecifications {
    public static Specification<Card> hasOwner(User owner) {
        return (root, query, criteriaBuilder) ->
            owner != null ? criteriaBuilder.equal(root.get("owner"), owner) : null;
    }
}
