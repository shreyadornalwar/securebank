package com.bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.service.BeneficiaryService;
import com.bank.service.BeneficiaryService.BeneficiaryDTO;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping
    public ResponseEntity<?> getBeneficiaries(@RequestParam Long userId) {
        try {
            List<BeneficiaryDTO> beneficiaries = beneficiaryService.getBeneficiariesByUserId(userId);
            List<Map<String, Object>> result = beneficiaries.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addBeneficiary(@RequestBody Map<String, Object> request) {
        try {
            Long userId = toLong(request.get("userId"));
            String name = (String) request.get("name");
            String accountNumber = (String) request.get("accountNumber");
            String bankName = (String) request.get("bankName");
            String ifscCode = (String) request.get("ifscCode");

            if (userId == null || name == null || accountNumber == null || bankName == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Missing required fields"));
            }

            beneficiaryService.addBeneficiary(userId, name, accountNumber, bankName, ifscCode);
            return ResponseEntity.ok(Map.of("success", true, "message", "Beneficiary added successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBeneficiary(@PathVariable Long id, @RequestParam Long userId) {
        try {
            beneficiaryService.deleteBeneficiary(id, userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Beneficiary deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> toResponse(BeneficiaryDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("userId", dto.getUserId());
        map.put("name", dto.getName());
        map.put("accountNumber", dto.getAccountNumber());
        map.put("bankName", dto.getBankName());
        map.put("ifscCode", dto.getIfscCode());
        map.put("createdAt", dto.getCreatedAt());
        return map;
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
}