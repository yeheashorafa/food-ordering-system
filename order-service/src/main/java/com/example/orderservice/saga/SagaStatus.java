package com.example.orderservice.saga;

public enum SagaStatus {
    STARTED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    DELIVERY_REQUESTED,
    DELIVERY_ASSIGNED,
    NOTIFICATION_SENT,
    COMPLETED,
    COMPENSATING,
    CANCELLED
}
