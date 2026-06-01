package com.example.orderservice.event;

import com.example.orderservice.config.RabbitMQConfig;
import com.example.orderservice.entity.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderPlaced(Order order) {
        Map<String, Object> event = base(order, "OrderPlaced");
        event.put("customerId", order.getCustomerId());
        event.put("totalAmount", order.getTotalPrice());
        event.put("itemName", order.getItemName());
        event.put("quantity", order.getQuantity());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EVENTS_EXCHANGE, "order.placed", event);
    }

    public void publishOrderReadyForDelivery(Order order) {
        Map<String, Object> event = base(order, "OrderReadyForDelivery");
        event.put("customerId", order.getCustomerId());
        event.put("deliveryAddress", order.getDeliveryAddress());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EVENTS_EXCHANGE, "order.ready.delivery", event);
    }

    public void publishOrderCancelled(Order order, String reason) {
        Map<String, Object> event = base(order, "OrderCancelled");
        event.put("reason", reason);
        event.put("restaurantItemId", order.getRestaurantItemId());
        event.put("quantity", order.getQuantity());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EVENTS_EXCHANGE, "order.cancelled", event);
    }

    private Map<String, Object> base(Order order, String eventType) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("orderId", order.getId());
        event.put("correlationId", order.getCorrelationId());
        return event;
    }
}
