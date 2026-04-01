package com.bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.service.CardService;
import com.bank.service.CardService.CardDTO;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<?> getCards(@RequestParam(required = false) Long userId) {
        try {
            List<CardDTO> cards;
            if (userId != null) {
                cards = cardService.getCardsByUserId(userId);
            } else {
                cards = cardService.getAllCards();
            }
            List<Map<String, Object>> result = cards.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> applyCard(@RequestBody Map<String, Object> request) {
        try {
            Long userId = toLong(request.get("userId"));
            String type = (String) request.get("type");
            double limitAmount = toDouble(request.get("limitAmount"));

            if (userId == null || type == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid card application data"));
            }

            String cardId = cardService.applyCard(userId, type, limitAmount);
            return ResponseEntity.ok(Map.of("success", true, "cardId", cardId, "message", "Card application successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/status")
    public ResponseEntity<?> updateCardStatus(@RequestBody Map<String, Object> request) {
        try {
            String cardId = (String) request.get("cardId");
            Long userId = toLong(request.get("userId"));
            String status = (String) request.get("status");

            if (cardId == null || userId == null || status == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
            }

            cardService.updateCardStatus(cardId, userId, status);
            return ResponseEntity.ok(Map.of("success", true, "message", "Card status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> toResponse(CardDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("userId", dto.getUserId());
        map.put("number", dto.getNumber());
        map.put("type", dto.getType());
        map.put("expiry", dto.getExpiryDate());
        map.put("holder", dto.getHolderName());
        map.put("limit", dto.getLimitAmount());
        map.put("status", dto.getStatus());
        map.put("createdAt", dto.getCreatedAt());
        if (dto.getCustomerName() != null) {
            map.put("customerName", dto.getCustomerName());
            map.put("customerEmail", dto.getCustomerEmail());
        }
        return map;
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private double toDouble(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }
}