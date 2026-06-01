package com.example.deliveryservice.controller;

import com.example.deliveryservice.entity.Delivery;
import com.example.deliveryservice.repository.DeliveryRepository;
import com.example.deliveryservice.service.DeliveryTrackingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryRepository repository;
    private final DeliveryTrackingService trackingService;

    public DeliveryController(DeliveryRepository repository, DeliveryTrackingService trackingService) {
        this.repository = repository;
        this.trackingService = trackingService;
    }

    @GetMapping
    public List<Delivery> all() { return repository.findAll(); }

    @GetMapping("/order/{orderId}")
    public Delivery byOrder(@PathVariable Long orderId) {
        return repository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Delivery not found"));
    }

    @GetMapping("/{orderId}/stream")
    public SseEmitter stream(@PathVariable Long orderId) {
        return trackingService.subscribe(orderId);
    }

    @PostMapping("/{orderId}/location")
    public Delivery updateLocation(@PathVariable Long orderId, @RequestParam Double lat, @RequestParam Double lng) {
        Delivery delivery = repository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Delivery not found"));
        delivery.setLatitude(lat);
        delivery.setLongitude(lng);
        delivery.setStatus("OUT_FOR_DELIVERY");
        delivery.setUpdatedAt(LocalDateTime.now());
        Delivery saved = repository.save(delivery);
        trackingService.send(orderId, Map.of("orderId", orderId, "driverId", saved.getDriverId(),
                "lat", lat, "lng", lng, "status", saved.getStatus()));
        return saved;
    }
}
