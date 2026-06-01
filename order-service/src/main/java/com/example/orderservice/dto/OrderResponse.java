package com.example.orderservice.dto;

public class OrderResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long restaurantItemId;
    private String itemName;
    private Integer quantity;
    private Double totalPrice;
    private String deliveryAddress;
    private String status;
    private String sagaStatus;
    private String correlationId;
    private String transactionId;
    private String driverId;
    private String estimatedArrival;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getRestaurantItemId() { return restaurantItemId; }
    public void setRestaurantItemId(Long restaurantItemId) { this.restaurantItemId = restaurantItemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSagaStatus() { return sagaStatus; }
    public void setSagaStatus(String sagaStatus) { this.sagaStatus = sagaStatus; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }
}
