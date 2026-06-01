package com.example.notificationservice.controller;

import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.repository.NotificationLogRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationLogRepository repository;

    public NotificationController(NotificationLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NotificationLog> all() { return repository.findAll(); }

    @GetMapping("/order/{orderId}")
    public List<NotificationLog> byOrder(@PathVariable Long orderId) { return repository.findByOrderId(orderId); }
}
