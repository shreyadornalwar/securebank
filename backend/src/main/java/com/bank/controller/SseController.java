package com.bank.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/events")
public class SseController {

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        
        emitter.onCompletion(() -> {
            System.out.println("[SSE] Client disconnected");
            emitters.remove(emitter);
        });
        
        emitter.onTimeout(() -> {
            System.out.println("[SSE] Connection timeout");
            emitters.remove(emitter);
            try {
                emitter.send(SseEmitter.event().name("ping").data("ping"));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        });
        
        emitter.onError((e) -> {
            System.out.println("[SSE] Error: " + e.getMessage());
            emitters.remove(emitter);
        });

        emitters.add(emitter);
        System.out.println("[SSE] Client connected. Total clients: " + emitters.size());

        try {
            // Send initial connection event
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"status\":\"connected\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcast(String event, Object data) {
        String jsonData = data instanceof String ? (String) data : 
            com.fasterxml.jackson.core.type.TypeReference.class.toString();
        
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
                return false;
            } catch (IOException e) {
                return true; // Remove failed emitters
            }
        });
    }

    public void broadcastAccountUpdate(Object accountData) {
        broadcast("account_created", accountData);
    }

    public void broadcastTransaction(Object transactionData) {
        broadcast("transaction", transactionData);
    }

    @PreDestroy
    public void cleanup() {
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }

    public int getClientCount() {
        return emitters.size();
    }
}