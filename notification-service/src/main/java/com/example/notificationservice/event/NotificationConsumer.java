package com.example.notificationservice.event;

import com.example.notificationservice.config.MessagingConfig;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.repository.NotificationLogRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationConsumer {
    private final NotificationLogRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public NotificationConsumer(NotificationLogRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = MessagingConfig.NOTIFICATION_QUEUE)
    public void handleDeliveryAssigned(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String correlationId = String.valueOf(event.get("correlationId"));
        String driverId = String.valueOf(event.get("driverId"));
        String eta = String.valueOf(event.get("estimatedArrival"));
        save(orderId, "ORDER_CONFIRMED", "Your order is confirmed. Driver " + driverId + " will arrive in " + eta,
                "SENT", correlationId);
        Map<String, Object> result = new HashMap<>();
        result.put("eventType", "NotificationSent");
        result.put("orderId", orderId);
        result.put("correlationId", correlationId);
        result.put("notificationStatus", "SENT");
        rabbitTemplate.convertAndSend(MessagingConfig.NOTIFICATION_RESULT_QUEUE, result);
        System.out.println("[NotificationService] Confirmation notification sent for orderId=" + orderId);
    }

    @RabbitListener(queues = MessagingConfig.NOTIFICATION_CANCEL_QUEUE)
    public void handleOrderCancelled(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String correlationId = String.valueOf(event.get("correlationId"));
        String reason = String.valueOf(event.getOrDefault("reason", "ORDER_CANCELLED"));
        save(orderId, "ORDER_CANCELLED", "Your order was cancelled. Reason: " + reason,
                "SENT", correlationId);
        System.out.println("[NotificationService] Cancellation notification sent for orderId=" + orderId);
    }

    private void save(Long orderId, String type, String message, String status, String correlationId) {
        NotificationLog log = new NotificationLog();
        log.setOrderId(orderId);
        log.setType(type);
        log.setMessage(message);
        log.setStatus(status);
        log.setCorrelationId(correlationId);
        repository.save(log);
    }

    private Long toLong(Object value) { return Long.valueOf(String.valueOf(value)); }
}
