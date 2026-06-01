package com.example.paymentservice.event;

import com.example.paymentservice.config.MessagingConfig;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentEventConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final PaymentRepository paymentRepository;

    public PaymentEventConsumer(RabbitTemplate rabbitTemplate, PaymentRepository paymentRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.paymentRepository = paymentRepository;
    }

    @RabbitListener(queues = MessagingConfig.PAYMENT_QUEUE)
    public void handleOrderPlaced(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        Long customerId = toLong(event.get("customerId"));
        Double amount = Double.valueOf(String.valueOf(event.get("totalAmount")));
        String correlationId = String.valueOf(event.get("correlationId"));
        boolean success = processPayment(customerId, amount);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setCorrelationId(correlationId);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(success ? "SUCCESS" : "FAILED");
        paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("eventType", "PaymentStatus");
        result.put("orderId", orderId);
        result.put("correlationId", correlationId);
        result.put("transactionId", payment.getTransactionId());
        if (success) {
            result.put("paymentStatus", "SUCCESS");
            System.out.println("[PaymentService] Payment SUCCESS for orderId=" + orderId);
        } else {
            result.put("paymentStatus", "FAILED");
            result.put("reason", "INSUFFICIENT_FUNDS");
            System.out.println("[PaymentService] Payment FAILED for orderId=" + orderId);
        }
        rabbitTemplate.convertAndSend(MessagingConfig.PAYMENT_RESULT_QUEUE, result);
    }

    @RabbitListener(queues = MessagingConfig.PAYMENT_REFUND_QUEUE)
    public void handleRefund(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String correlationId = String.valueOf(event.get("correlationId"));
        Payment refund = new Payment();
        refund.setOrderId(orderId);
        refund.setAmount(0.0);
        refund.setStatus("REFUND_ISSUED");
        refund.setTransactionId("REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        refund.setCorrelationId(correlationId);
        paymentRepository.save(refund);
        System.out.println("[PaymentService] Refund issued for orderId=" + orderId);
    }

    private boolean processPayment(Long customerId, Double amount) {
        return amount <= 1000.0;
    }

    private Long toLong(Object value) {
        return Long.valueOf(String.valueOf(value));
    }
}
