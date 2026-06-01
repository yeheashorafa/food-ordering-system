package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.service.OrderService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class OrderGraphQLController {
    private final OrderService orderService;

    public OrderGraphQLController(OrderService orderService) {
        this.orderService = orderService;
    }

    @QueryMapping
    public OrderResponse orderDetails(@Argument Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @QueryMapping
    public List<OrderResponse> orders() {
        return orderService.getAllOrders();
    }
}
