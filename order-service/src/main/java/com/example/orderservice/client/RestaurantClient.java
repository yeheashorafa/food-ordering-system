package com.example.orderservice.client;

import com.example.orderservice.dto.MenuItemResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestaurantClient {
    private final RestTemplate restTemplate;
    private final String restaurantServiceUrl;

    public RestaurantClient(RestTemplate restTemplate,
                            @Value("${restaurant.service.url}") String restaurantServiceUrl) {
        this.restTemplate = restTemplate;
        this.restaurantServiceUrl = restaurantServiceUrl;
    }

    public MenuItemResponse getItem(Long itemId) {
        return restTemplate.getForObject(restaurantServiceUrl + "/api/restaurants/items/" + itemId,
                MenuItemResponse.class);
    }

    public void reserveItem(Long itemId, Integer quantity) {
        restTemplate.postForObject(restaurantServiceUrl + "/api/restaurants/items/" + itemId + "/reserve?quantity=" + quantity,
                null, MenuItemResponse.class);
    }

    public void releaseItem(Long itemId, Integer quantity) {
        restTemplate.postForObject(restaurantServiceUrl + "/api/restaurants/items/" + itemId + "/release?quantity=" + quantity,
                null, MenuItemResponse.class);
    }
}
