package com.backend.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQPublisher {
        private final RabbitTemplate rabbitTemplate;

        public void publish(Object event) {

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    "booking.created",
                    event
            );
        }

}