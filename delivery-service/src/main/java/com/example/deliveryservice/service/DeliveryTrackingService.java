package com.example.deliveryservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DeliveryTrackingService {
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long orderId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> emitters.getOrDefault(orderId, List.of()).remove(emitter));
        emitter.onTimeout(() -> emitters.getOrDefault(orderId, List.of()).remove(emitter));
        send(orderId, Map.of("message", "Connected to live delivery stream", "orderId", orderId));
        return emitter;
    }

    public void send(Long orderId, Map<String, Object> payload) {
        List<SseEmitter> list = emitters.getOrDefault(orderId, List.of());
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("gps-update").data(payload));
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }
}
