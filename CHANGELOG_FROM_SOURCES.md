# What Was Updated from Ch5 + Ch6 Sources

1. Kept Ch5 communication ideas: REST, gRPC client/server, GraphQL.
2. Merged Ch6 Choreographed Saga concepts into the final Order Service.
3. Added missing runnable microservices: Restaurant, Payment, Delivery, Notification, and API Gateway.
4. Added `sagaStatus` and `correlationId` to orders.
5. Added RabbitMQ configuration with queues, topics, and DLQ.
6. Added compensating transactions: cancel order, release reserved item, refund payment, cancel delivery, send cancellation email.
7. Added Dockerfiles and `docker-compose.yml` for local deployment.
8. Added GitHub Actions workflow for CI/CD on Order Service.
