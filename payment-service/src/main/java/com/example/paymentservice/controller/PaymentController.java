package com.example.paymentservice.controller;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository repository;

    public PaymentController(PaymentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Payment> all() { return repository.findAll(); }

    @GetMapping("/order/{orderId}")
    public List<Payment> byOrder(@PathVariable Long orderId) { return repository.findByOrderId(orderId); }
}
