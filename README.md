# Food Ordering System - Final Running Project

**Student:** Yehea Fayez Shorafa  
**ID:** 120211088  
**Course:** Advanced Software Engineering - SDEV 4304  
**Supervisor:** Dr. Abdelkareem Alashqar - Islamic University of Gaza

## Overview

This monorepo contains a Spring Boot microservices implementation for a Food Ordering System. It combines the communication work from Chapter 5 with the Choreographed Saga workflow from Chapter 6 and deployment work from Chapter 8.

## Services

| Service | Port | Database | Main Responsibility |
|---|---:|---|---|
| API Gateway | 8088 | - | Simple entry point for client requests |
| Customer Service | 8080 / gRPC 9091 | Customer_DB (H2) | Customer profile management, REST + gRPC |
| Restaurant Service | 8081 | Restaurant_DB (H2) | Menu items and stock reservation |
| Order Service | 8082 | Order_DB (H2) | Main saga coordinator, REST + GraphQL + gRPC client |
| Payment Service | 8083 | Payment_DB (H2) | Processes payment events |
| Delivery Service | 8084 | Delivery_DB (H2) | Driver assignment + live GPS via SSE |
| Notification Service | 8085 | Notification_DB (H2) | Notification log and semantic rollback emails |
| RabbitMQ | 5672 / 15672 | - | Message broker for async events |

## Communication

- REST: API Gateway -> Order Service, Order -> Customer, Order -> Restaurant.
- RabbitMQ: Choreographed Saga events between Order, Payment, Delivery, and Notification.
- gRPC: Order Service can call Customer Service via gRPC.
- GraphQL: Order Service exposes `/graphql` and GraphiQL at `/graphiql`.
- SSE: Delivery Service exposes `/api/deliveries/{orderId}/stream` for live GPS tracking.

## Saga Happy Path

1. Customer creates an order via Order Service.
2. Order Service validates the customer and menu item using REST.
3. Order Service reserves the restaurant item.
4. Order Service publishes `OrderPlaced` to RabbitMQ.
5. Payment Service consumes the event and publishes `PaymentStatus`.
6. If payment succeeds, Order Service publishes `OrderReadyForDelivery`.
7. Delivery Service assigns a driver and publishes `DeliveryAssigned`.
8. Notification Service sends a notification and publishes `NotificationSent`.
9. Order Service marks the saga as `COMPLETED`.

## Compensation

- Payment failure triggers `OrderCancelled`.
- Restaurant reservation is released synchronously by Order Service.
- Payment Service listens for cancellation and issues refund when needed.
- Delivery Service cancels driver assignment.
- Notification Service sends a corrective cancellation notification because emails cannot be unsent.

## Run Locally Without Docker

Start RabbitMQ:

```bash
docker run -d --name food-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Start services in separate terminals:

```bash
cd customer-service && mvn spring-boot:run
cd restaurant-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd delivery-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

## Run With Docker Compose

```bash
docker compose up --build
```

## Test

Create an order through the gateway:

```bash
curl -X POST http://localhost:8088/api/gateway/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"restaurantItemId":1,"quantity":2,"deliveryAddress":"Gaza - Al Remal"}'
```

Create a payment-failure order:

```bash
curl -X POST http://localhost:8088/api/gateway/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"restaurantItemId":3,"quantity":2,"deliveryAddress":"Gaza - Al Remal"}'
```

Query order details:

```bash
curl http://localhost:8082/api/orders/1
```

GraphQL query:

```graphql
query {
  orderDetails(orderId: 1) {
    id
    customerName
    itemName
    totalPrice
    status
    sagaStatus
  }
}
```

SSE live GPS:

```bash
curl http://localhost:8084/api/deliveries/1/stream
curl -X POST "http://localhost:8084/api/deliveries/1/location?lat=31.501&lng=34.466"
```

## Docker Hub / GitHub Actions Bonus

The workflow `.github/workflows/order-service-ci-cd.yml` builds the Order Service and pushes an image to Docker Hub. Add these repository secrets first:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
