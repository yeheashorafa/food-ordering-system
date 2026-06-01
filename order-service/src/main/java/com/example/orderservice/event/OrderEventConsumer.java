package com.example.orderservice.event;

import com.example.orderservice.config.RabbitMQConfig;
import com.example.orderservice.client.RestaurantClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.saga.SagaStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Component
public class OrderEventConsumer {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final RestaurantClient restaurantClient;

    public OrderEventConsumer(OrderRepository orderRepository, OrderEventPublisher eventPublisher,
                              RestaurantClient restaurantClient) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.restaurantClient = restaurantClient;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_RESULT_QUEUE)
    @Transactional
    public void handlePaymentResult(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String status = String.valueOf(event.get("paymentStatus"));
        Order order = find(orderId);
        if ("SUCCESS".equalsIgnoreCase(status)) {
            order.setTransactionId(String.valueOf(event.get("transactionId")));
            order.setStatus("PAID");
            order.setSagaStatus(SagaStatus.PAYMENT_COMPLETED);
            orderRepository.save(order);
            order.setSagaStatus(SagaStatus.DELIVERY_REQUESTED);
            orderRepository.save(order);
            eventPublisher.publishOrderReadyForDelivery(order);
            System.out.println("[OrderService] Payment succeeded, delivery requested for orderId=" + orderId);
        } else {
            order.setStatus("CANCELLED");
            order.setSagaStatus(SagaStatus.COMPENSATING);
            orderRepository.save(order);
            restaurantClient.releaseItem(order.getRestaurantItemId(), order.getQuantity());
            eventPublisher.publishOrderCancelled(order, String.valueOf(event.getOrDefault("reason", "PAYMENT_FAILED")));
            order.setSagaStatus(SagaStatus.CANCELLED);
            orderRepository.save(order);
            System.out.println("[OrderService] Payment failed, compensation started for orderId=" + orderId);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_STATUS_QUEUE)
    @Transactional
    public void handleDeliveryStatus(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        String status = String.valueOf(event.get("deliveryStatus"));
        Order order = find(orderId);
        if ("ASSIGNED".equalsIgnoreCase(status)) {
            order.setDriverId(String.valueOf(event.get("driverId")));
            order.setEstimatedArrival(String.valueOf(event.get("estimatedArrival")));
            order.setStatus("OUT_FOR_DELIVERY");
            order.setSagaStatus(SagaStatus.DELIVERY_ASSIGNED);
            orderRepository.save(order);
            System.out.println("[OrderService] Driver assigned for orderId=" + orderId);
        } else {
            order.setStatus("CANCELLED");
            order.setSagaStatus(SagaStatus.COMPENSATING);
            orderRepository.save(order);
            restaurantClient.releaseItem(order.getRestaurantItemId(), order.getQuantity());
            eventPublisher.publishOrderCancelled(order, "DELIVERY_FAILED");
            order.setSagaStatus(SagaStatus.CANCELLED);
            orderRepository.save(order);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_RESULT_QUEUE)
    @Transactional
    public void handleNotificationResult(Map<String, Object> event) {
        Long orderId = toLong(event.get("orderId"));
        Order order = find(orderId);
        order.setSagaStatus(SagaStatus.NOTIFICATION_SENT);
        order.setStatus("CONFIRMED");
        orderRepository.save(order);
        order.setSagaStatus(SagaStatus.COMPLETED);
        orderRepository.save(order);
        System.out.println("[OrderService] Saga completed for orderId=" + orderId);
    }

    private Order find(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    private Long toLong(Object value) {
        return Long.valueOf(String.valueOf(value));
    }
}
