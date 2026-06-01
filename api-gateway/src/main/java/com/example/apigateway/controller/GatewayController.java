package com.example.apigateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {
    private final RestTemplate restTemplate;
    private final String orderServiceUrl;
    private final String customerServiceUrl;
    private final String restaurantServiceUrl;

    public GatewayController(RestTemplate restTemplate,
                             @Value("${order.service.url}") String orderServiceUrl,
                             @Value("${customer.service.url}") String customerServiceUrl,
                             @Value("${restaurant.service.url}") String restaurantServiceUrl) {
        this.restTemplate = restTemplate;
        this.orderServiceUrl = orderServiceUrl;
        this.customerServiceUrl = customerServiceUrl;
        this.restaurantServiceUrl = restaurantServiceUrl;
    }

    @GetMapping("/health")
    public String health() { return "API Gateway is running"; }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody String body) {
        return forwardPost(orderServiceUrl + "/api/orders", body);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<String> getOrder(@PathVariable Long id) {
        return restTemplate.getForEntity(orderServiceUrl + "/api/orders/" + id, String.class);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<String> getCustomer(@PathVariable Long id) {
        return restTemplate.getForEntity(customerServiceUrl + "/api/customers/" + id, String.class);
    }

    @GetMapping("/restaurants/menu")
    public ResponseEntity<String> getMenu() {
        return restTemplate.getForEntity(restaurantServiceUrl + "/api/restaurants/menu", String.class);
    }

    private ResponseEntity<String> forwardPost(String url, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
