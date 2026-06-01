package com.example.deliveryservice.event;

import com.example.deliveryservice.config.MessagingConfig;
import com.example.deliveryservice.entity.Delivery;
import com.example.deliveryservice.repository.DeliveryRepository;
import com.example.deliveryservice.service.DeliveryTrackingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeliveryEventConsumer {
    private final RabbitTemplate rabbitTemplate;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryTrackingService trackingService;

    public DeliveryEventConsumer(RabbitTemplate rabbitTemplate, DeliveryRepository deliveryRepository,
                                 DeliveryTrackingService trackingService) {
        this.rabbitTemplate = rabbitTemplate;
        this.deliveryRepository = deliveryRepository;
        this.trackingService = trackingService;
    }

    @RabbitListener(queues = MessagingConfig.DELIVERY_QUEUE)
    public void handleOrderReadyForDelivery(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String address = String.valueOf(event.get("deliveryAddress"));
        String correlationId = String.valueOf(event.get("correlationId"));
        boolean driverAvailable = !address.toLowerCase().contains("no-driver");

        Map<String, Object> result = new HashMap<>();
        result.put("eventType", "DeliveryStatus");
        result.put("orderId", orderId);
        result.put("correlationId", correlationId);

        if (driverAvailable) {
            String driverId = "DRIVER-" + ((orderId % 5) + 1);
            Delivery delivery = new Delivery();
            delivery.setOrderId(orderId);
            delivery.setDriverId(driverId);
            delivery.setStatus("ASSIGNED");
            delivery.setEstimatedArrival("20 min");
            delivery.setLatitude(31.501);
            delivery.setLongitude(34.466);
            delivery.setCorrelationId(correlationId);
            deliveryRepository.save(delivery);

            result.put("deliveryStatus", "ASSIGNED");
            result.put("driverId", driverId);
            result.put("estimatedArrival", "20 min");
            rabbitTemplate.convertAndSend(MessagingConfig.DELIVERY_STATUS_QUEUE, result);
            rabbitTemplate.convertAndSend(MessagingConfig.DELIVERY_EVENTS_EXCHANGE, "delivery.assigned", result);
            trackingService.send(orderId, Map.of("orderId", orderId, "driverId", driverId,
                    "lat", 31.501, "lng", 34.466, "status", "ASSIGNED"));
            System.out.println("[DeliveryService] Driver assigned for orderId=" + orderId);
        } else {
            result.put("deliveryStatus", "FAILED");
            result.put("reason", "NO_DRIVER_AVAILABLE");
            rabbitTemplate.convertAndSend(MessagingConfig.DELIVERY_STATUS_QUEUE, result);
        }
    }

    @RabbitListener(queues = MessagingConfig.DELIVERY_CANCEL_QUEUE)
    public void handleOrderCancelled(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        deliveryRepository.findByOrderId(orderId).ifPresent(delivery -> {
            delivery.setStatus("CANCELLED");
            deliveryRepository.save(delivery);
        });
        trackingService.send(orderId, Map.of("orderId", orderId, "status", "CANCELLED"));
        System.out.println("[DeliveryService] Delivery cancelled for orderId=" + orderId);
    }

    private Long toLong(Object value) { return Long.valueOf(String.valueOf(value)); }
}
