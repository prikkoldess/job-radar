package com.jobscan.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "job.events.exchange";

    public static final String URLS_QUEUE = "job-urls-queue";
    public static final String ALERTS_QUEUE = "job-alerts-queue";
    public static final String DLQ = "job-dlq";

    public static final String ROUTING_KEY_URL_DISCOVERED = "job.url.discovered";
    public static final String ROUTING_KEY_ALERT_HIGH_MATCH = "job.alert.high-match";
    public static final String ROUTING_KEY_DLQ = "job.dlq";

    @Bean
    public TopicExchange jobExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Queue urlsQueue() {
        return QueueBuilder.durable(URLS_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLQ)
                .build();
    }

    @Bean
    public Queue alertsQueue() {
        return QueueBuilder.durable(ALERTS_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLQ)
                .build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlq()).to(jobExchange()).with(ROUTING_KEY_DLQ);
    }

    @Bean
    public Binding urlsBinding() {
        return BindingBuilder.bind(urlsQueue()).to(jobExchange()).with(ROUTING_KEY_URL_DISCOVERED);
    }

    @Bean
    public Binding alertsBinding() {
        return BindingBuilder.bind(alertsQueue()).to(jobExchange()).with(ROUTING_KEY_ALERT_HIGH_MATCH);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
