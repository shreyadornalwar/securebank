package com.bank.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/env")
    public ResponseEntity<Map<String, Object>> debugEnv() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Debug endpoint works without database");
        return ResponseEntity.ok(response);
    }
}
