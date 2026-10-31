package com.jobscan.queue.consumer;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.jobscan.config.RabbitMQConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DlqConsumer {
    @RabbitListener(queues = RabbitMQConfig.DLQ)
    public void processFailedMessage(Message message) {
        log.error("Received failed message in DLQ: {}", new String(message.getBody()));
    }
}
