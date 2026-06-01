# Food Ordering System - Final Running Project

**Student:** Yehea Fayez Shorafa  
**ID:** 120211088  
**Course:** Advanced Software Engineering - SDEV 4304  
**Supervisor:** Dr. Abdelkareem Alashqar - Islamic University of Gaza  

---

## Overview

This repository contains my final running project for the Advanced Software Engineering course.

The project is a **Food Ordering System** implemented using **Spring Boot microservices**.  
It continues the work done during the semester and combines several topics from the course, including REST communication, RabbitMQ messaging, gRPC, GraphQL, Saga workflow, Docker deployment, and a simple GitHub Actions workflow.

The goal of the project is to show how a food ordering application can be divided into small independent services, where each service has its own responsibility and database.

---

## Project Structure

The project is organized as a monorepo. Each microservice has its own folder, source code, Maven configuration, and Dockerfile.

```text
food-ordering-system
├── api-gateway
├── customer-service
├── restaurant-service
├── order-service
├── payment-service
├── delivery-service
├── notification-service
├── postman
├── k8s
├── .github/workflows
├── docker-compose.yml
└── README.md

```

---

## Services

| Service | Port | Database | Main Responsibility |
|---|---:|---|---|
| API Gateway | 8088 | - | Entry point for client requests |
| Customer Service | 8080 / gRPC 9091 | Customer_DB (H2) | Customer profile and validation |
| Restaurant Service | 8081 | Restaurant_DB (H2) | Menu items and item reservation |
| Order Service | 8082 | Order_DB (H2) | Main service for creating and tracking orders |
| Payment Service | 8083 | Payment_DB (H2) | Handles payment events |
| Delivery Service | 8084 | Delivery_DB (H2) | Assigns driver and provides live GPS updates |
| Notification Service | 8085 | Notification_DB (H2) | Sends order notifications |
| RabbitMQ | 5672 / 15672 | - | Message broker for asynchronous events |

---

## Main Technologies

- Java 17
- Spring Boot
- Spring Web REST APIs
- Spring Data JPA
- H2 Database
- RabbitMQ
- gRPC
- GraphQL
- Server-Sent Events / Live GPS tracking
- Docker and Docker Compose
- GitHub Actions

---

## Communication Between Services

The system uses more than one communication style:

### REST Communication

REST is used for synchronous communication between services.

Examples:

- Client sends requests to API Gateway.
- API Gateway routes requests to backend services.
- Order Service calls Customer Service to validate a customer.
- Order Service calls Restaurant Service to validate and reserve menu items.

### RabbitMQ Messaging

RabbitMQ is used for asynchronous communication.

Examples:

- Order Service publishes `OrderPlaced`.
- Payment Service consumes the order event and publishes payment status.
- Delivery Service handles delivery assignment.
- Notification Service sends order notifications.

### gRPC

gRPC is used as an additional communication method between Order Service and Customer Service.

### GraphQL

Order Service exposes simple GraphQL queries to retrieve order details.

### SSE / Live GPS Tracking

Delivery Service provides a simple live GPS tracking endpoint using Server-Sent Events.

---

## Saga Workflow

The project uses a **Choreographed Saga** pattern.

There is no central Saga coordinator. Each service reacts to events from RabbitMQ and publishes new events based on its result.

### Successful Flow

1. Customer creates an order.
2. Order Service validates the customer.
3. Order Service validates the menu item with Restaurant Service.
4. Restaurant Service reserves the selected item.
5. Order Service creates the order and publishes `OrderPlaced`.
6. Payment Service processes the payment.
7. If payment succeeds, Order Service publishes an event for delivery.
8. Delivery Service assigns a driver.
9. Notification Service sends a confirmation notification.
10. Order Service updates the final order status.

### Compensation Flow

If something fails during the order process, the system applies compensating actions:

- If payment fails, the order is cancelled.
- If delivery fails, the payment is refunded and the reserved item is released.
- If notification fails, the order remains valid because notification failure does not affect the main transaction.

---

## Databases

Each service has its own H2 database. The databases are created automatically when the services start.

H2 console URLs:

| Service | H2 Console |
|---|---|
| Customer Service | http://localhost:8080/h2-console |
| Restaurant Service | http://localhost:8081/h2-console |
| Order Service | http://localhost:8082/h2-console |
| Payment Service | http://localhost:8083/h2-console |
| Delivery Service | http://localhost:8084/h2-console |
| Notification Service | http://localhost:8085/h2-console |

Default login:

```text
User Name: sa
Password: empty
```

Example JDBC URLs:

```text
jdbc:h2:mem:customerdb
jdbc:h2:mem:restaurantdb
jdbc:h2:mem:orderdb
jdbc:h2:mem:paymentdb
jdbc:h2:mem:deliverydb
jdbc:h2:mem:notificationdb
```

---

## Run the Project with Docker Compose

Make sure Docker Desktop is running.

From the root folder, run:

```bash
docker compose up --build
```

Or run it in the background:

```bash
docker compose up --build -d
```

Check running containers:

```bash
docker ps
```

Stop all containers:

```bash
docker compose down
```

---

## RabbitMQ Management

RabbitMQ Management UI:

```text
http://localhost:15672
```

Login:

```text
Username: guest
Password: guest
```

---

## Run Locally Without Docker

RabbitMQ can be started using Docker:

```bash
docker run -d --name food-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Then each service can be started from its folder:

```bash
cd customer-service
mvn spring-boot:run
```

```bash
cd restaurant-service
mvn spring-boot:run
```

```bash
cd order-service
mvn spring-boot:run
```

```bash
cd payment-service
mvn spring-boot:run
```

```bash
cd delivery-service
mvn spring-boot:run
```

```bash
cd notification-service
mvn spring-boot:run
```

```bash
cd api-gateway
mvn spring-boot:run
```

---

## Test the APIs

### Create Order

Use Postman:

```http
POST http://localhost:8082/api/orders
Content-Type: application/json
```

Request body:

```json
{
  "customerId": 1,
  "restaurantItemId": 1,
  "quantity": 2,
  "deliveryAddress": "Gaza - Al Rimal"
}
```

The Order Service calculates the total price based on the selected restaurant item and quantity.

### Create Order through API Gateway

```http
POST http://localhost:8088/api/orders
Content-Type: application/json
```

Request body:

```json
{
  "customerId": 1,
  "restaurantItemId": 1,
  "quantity": 2,
  "deliveryAddress": "Gaza - Al Rimal"
}
```

### Get All Orders

```http
GET http://localhost:8082/api/orders
```

### Get Order by ID

```http
GET http://localhost:8082/api/orders/1
```

### Cancel Order

```http
DELETE http://localhost:8082/api/orders/1
```

---

## GraphQL Example

GraphQL endpoint:

```text
http://localhost:8082/graphql
```

Example query:

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

---

## SSE Live GPS Tracking

Delivery Service provides a simple endpoint for live GPS updates.

Stream location updates:

```http
GET http://localhost:8084/api/deliveries/1/stream
```

Update location:

```http
POST http://localhost:8084/api/deliveries/1/location?lat=31.5014&lng=34.4668
```

---

## Docker Hub / GitHub Actions

The project includes a GitHub Actions workflow for the Order Service.

Workflow file:

```text
.github/workflows/order-service-ci-cd.yml
```

The workflow does the following:

1. Checks out the code.
2. Sets up Java 17.
3. Builds the Order Service using Maven.
4. Builds a Docker image.
5. Optionally pushes the image to Docker Hub if Docker Hub secrets are configured.

Required GitHub secrets for Docker Hub push:

```text
DOCKER_USERNAME
DOCKER_PASSWORD
```

---

## Notes

- The project uses H2 in-memory databases for easy testing.
- The database tables are created automatically when each service starts.
- RabbitMQ is used to simulate asynchronous communication between services.
- The Docker setup was tested using Docker Desktop.
- This project is built for the final running project in the Advanced Software Engineering course.
