package com.bank.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.service.LoanService;
import com.bank.service.LoanService.LoanActivityDTO;
import com.bank.service.LoanService.LoanDTO;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public ResponseEntity<?> getLoans(@RequestParam(required = false) Long userId) {
        try {
            List<LoanDTO> loans;
            if (userId != null) {
                loans = loanService.getLoansByUserId(userId);
            } else {
                loans = loanService.getAllLoans();
            }
            List<Map<String, Object>> result = loans.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{loanId}/activities")
    public ResponseEntity<?> getLoanActivities(@PathVariable String loanId) {
        try {
            List<LoanActivityDTO> activities = loanService.getLoanActivities(loanId);
            List<Map<String, Object>> result = activities.stream().map(this::activityToResponse).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> applyLoan(@RequestBody Map<String, Object> request) {
        try {
            Long userId = toLong(request.get("userId"));
            String type = (String) request.get("type");
            double amount = toDouble(request.get("amount"));
            int tenure = toInteger(request.get("tenure"));

            if (userId == null || type == null || amount <= 0 || tenure <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid loan application data"));
            }

            String loanId = loanService.applyLoan(userId, type, amount, tenure);
            return ResponseEntity.ok(Map.of("success", true, "loanId", loanId, "message", "Loan application submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{loanId}/status")
    public ResponseEntity<?> updateLoanStatus(@PathVariable String loanId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String description = request.get("description");

            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
            }

            loanService.updateLoanStatus(loanId, status, description);
            return ResponseEntity.ok(Map.of("success", true, "message", "Loan status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> toResponse(LoanDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("userId", dto.getUserId());
        map.put("type", dto.getType());
        map.put("amount", dto.getAmount());
        map.put("interest", dto.getInterestRate());
        map.put("emi", dto.getEmi());
        map.put("tenure", dto.getTenureMonths());
        map.put("status", dto.getStatus());
        map.put("appliedDate", dto.getAppliedDate());
        map.put("createdAt", dto.getCreatedAt());
        if (dto.getCustomerName() != null) {
            map.put("customerName", dto.getCustomerName());
            map.put("customerEmail", dto.getCustomerEmail());
        }
        return map;
    }

    private Map<String, Object> activityToResponse(LoanActivityDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("loanId", dto.getLoanId());
        map.put("status", dto.getStatus());
        map.put("description", dto.getDescription());
        map.put("activityDate", dto.getActivityDate());
        map.put("activityTime", dto.getActivityTime());
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

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }
}