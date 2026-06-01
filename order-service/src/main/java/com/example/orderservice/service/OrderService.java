package com.example.orderservice.service;

import com.example.orderservice.client.CustomerClient;
import com.example.orderservice.client.RestaurantClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.CustomerResponse;
import com.example.orderservice.dto.MenuItemResponse;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.event.OrderEventPublisher;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.saga.SagaStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;
    private final RestaurantClient restaurantClient;
    private final GrpcCustomerClientService grpcCustomerClientService;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        CustomerClient customerClient,
                        RestaurantClient restaurantClient,
                        GrpcCustomerClientService grpcCustomerClientService,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
        this.restaurantClient = restaurantClient;
        this.grpcCustomerClientService = grpcCustomerClientService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        CustomerResponse customer = customerClient.getCustomerById(request.getCustomerId());
        return createValidatedOrder(request, customer, "PLACED");
    }

    @Transactional
    public OrderResponse createOrderUsingGrpc(CreateOrderRequest request) {
        CustomerResponse customer = grpcCustomerClientService.getCustomerById(request.getCustomerId());
        return createValidatedOrder(request, customer, "PLACED_GRPC");
    }

    private OrderResponse createValidatedOrder(CreateOrderRequest request, CustomerResponse customer, String initialStatus) {
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        MenuItemResponse item = restaurantClient.getItem(request.getRestaurantItemId());
        if (item == null || !Boolean.TRUE.equals(item.getAvailable())) {
            throw new RuntimeException("Menu item is not available");
        }
        restaurantClient.reserveItem(item.getId(), request.getQuantity());

        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getName());
        order.setRestaurantItemId(item.getId());
        order.setItemName(item.getItemName());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(item.getPrice() * request.getQuantity());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(initialStatus);
        order.setSagaStatus(SagaStatus.STARTED);
        order.setCorrelationId(UUID.randomUUID().toString());
        order = orderRepository.save(order);

        order.setSagaStatus(SagaStatus.PAYMENT_PENDING);
        orderRepository.save(order);
        eventPublisher.publishOrderPlaced(order);
        return toResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrderById(Long id) {
        return toResponse(findOrder(id));
    }

    public Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, String reason) {
        Order order = findOrder(id);
        order.setStatus("CANCELLED");
        order.setSagaStatus(SagaStatus.COMPENSATING);
        orderRepository.save(order);
        restaurantClient.releaseItem(order.getRestaurantItemId(), order.getQuantity());
        eventPublisher.publishOrderCancelled(order, reason);
        order.setSagaStatus(SagaStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setCustomerName(order.getCustomerName());
        response.setRestaurantItemId(order.getRestaurantItemId());
        response.setItemName(order.getItemName());
        response.setQuantity(order.getQuantity());
        response.setTotalPrice(order.getTotalPrice());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setStatus(order.getStatus());
        response.setSagaStatus(order.getSagaStatus() == null ? null : order.getSagaStatus().name());
        response.setCorrelationId(order.getCorrelationId());
        response.setTransactionId(order.getTransactionId());
        response.setDriverId(order.getDriverId());
        response.setEstimatedArrival(order.getEstimatedArrival());
        return response;
    }
}
