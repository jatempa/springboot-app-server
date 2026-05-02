package com.mystore.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue}")
    private String queue;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange(exchange, true, false);
    }

    @Bean
    public Queue orderReportQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public Binding orderReportBinding(Queue orderReportQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(orderReportQueue).to(ordersExchange).with(routingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
