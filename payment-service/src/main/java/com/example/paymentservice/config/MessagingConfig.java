package com.example.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    public static final String ORDER_EVENTS_EXCHANGE = "order.events.topic";
    public static final String DELIVERY_EVENTS_EXCHANGE = "delivery.topic";
    public static final String DLX = "food-ordering.dlx";
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String PAYMENT_RESULT_QUEUE = "payment.result.queue";
    public static final String PAYMENT_REFUND_QUEUE = "payment.refund.queue";
    public static final String DELIVERY_QUEUE = "delivery.queue";
    public static final String DELIVERY_STATUS_QUEUE = "delivery.status.queue";
    public static final String DELIVERY_CANCEL_QUEUE = "delivery.cancel.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_RESULT_QUEUE = "notification.result.queue";
    public static final String NOTIFICATION_CANCEL_QUEUE = "notification.cancel.queue";
    public static final String DLQ = "food-ordering.dlq";

    @Bean public TopicExchange orderEventsExchange() { return new TopicExchange(ORDER_EVENTS_EXCHANGE); }
    @Bean public TopicExchange deliveryEventsExchange() { return new TopicExchange(DELIVERY_EVENTS_EXCHANGE); }
    @Bean public TopicExchange deadLetterExchange() { return new TopicExchange(DLX); }
    @Bean public Queue paymentQueue() { return durableWithDlx(PAYMENT_QUEUE); }
    @Bean public Queue paymentResultQueue() { return QueueBuilder.durable(PAYMENT_RESULT_QUEUE).build(); }
    @Bean public Queue paymentRefundQueue() { return QueueBuilder.durable(PAYMENT_REFUND_QUEUE).build(); }
    @Bean public Queue deliveryQueue() { return durableWithDlx(DELIVERY_QUEUE); }
    @Bean public Queue deliveryStatusQueue() { return QueueBuilder.durable(DELIVERY_STATUS_QUEUE).build(); }
    @Bean public Queue deliveryCancelQueue() { return QueueBuilder.durable(DELIVERY_CANCEL_QUEUE).build(); }
    @Bean public Queue notificationQueue() { return QueueBuilder.durable(NOTIFICATION_QUEUE).build(); }
    @Bean public Queue notificationResultQueue() { return QueueBuilder.durable(NOTIFICATION_RESULT_QUEUE).build(); }
    @Bean public Queue notificationCancelQueue() { return QueueBuilder.durable(NOTIFICATION_CANCEL_QUEUE).build(); }
    @Bean public Queue deadLetterQueue() { return QueueBuilder.durable(DLQ).build(); }

    @Bean public Binding paymentBinding(Queue paymentQueue, TopicExchange orderEventsExchange) { return BindingBuilder.bind(paymentQueue).to(orderEventsExchange).with("order.placed"); }
    @Bean public Binding deliveryBinding(Queue deliveryQueue, TopicExchange orderEventsExchange) { return BindingBuilder.bind(deliveryQueue).to(orderEventsExchange).with("order.ready.delivery"); }
    @Bean public Binding paymentRefundBinding(Queue paymentRefundQueue, TopicExchange orderEventsExchange) { return BindingBuilder.bind(paymentRefundQueue).to(orderEventsExchange).with("order.cancelled"); }
    @Bean public Binding deliveryCancelBinding(Queue deliveryCancelQueue, TopicExchange orderEventsExchange) { return BindingBuilder.bind(deliveryCancelQueue).to(orderEventsExchange).with("order.cancelled"); }
    @Bean public Binding notificationCancelBinding(Queue notificationCancelQueue, TopicExchange orderEventsExchange) { return BindingBuilder.bind(notificationCancelQueue).to(orderEventsExchange).with("order.cancelled"); }
    @Bean public Binding notificationBinding(Queue notificationQueue, TopicExchange deliveryEventsExchange) { return BindingBuilder.bind(notificationQueue).to(deliveryEventsExchange).with("delivery.assigned"); }
    @Bean public Binding dlqBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) { return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#"); }

    private Queue durableWithDlx(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", "dlq." + name)
                .build();
    }

    @Bean public Jackson2JsonMessageConverter jsonMessageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
    @Bean public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
