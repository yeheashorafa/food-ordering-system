package com.example.orderservice.dto;

public class CreateOrderRequest {
    private Long customerId;
    private Long restaurantItemId;
    private Integer quantity;
    private String deliveryAddress;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getRestaurantItemId() { return restaurantItemId; }
    public void setRestaurantItemId(Long restaurantItemId) { this.restaurantItemId = restaurantItemId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}
