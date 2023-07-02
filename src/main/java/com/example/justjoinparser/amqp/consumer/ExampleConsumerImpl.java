package com.example.justjoinparser.amqp.consumer;

import com.example.justjoinparser.dto.OfferDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

/**
 * Example of MessageBrokerAbstractConsumer implementation. For view only.
 */
@Slf4j
@Component
class ExampleConsumerImpl extends MessageBrokerAbstractConsumer<OfferDto> {

    protected ExampleConsumerImpl(Receiver receiver, ObjectMapper objectMapper) {
        super("offers.trojmiasto", receiver, objectMapper, OfferDto.class);
    }

    @Override
    protected Mono<Void> handleMessage(OfferDto body) {
//        log.info("Sample reactive consumer. Should provider here a message handling");
        return Mono.empty();
    }
}
