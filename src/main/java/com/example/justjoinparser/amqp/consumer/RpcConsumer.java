package com.example.justjoinparser.amqp.consumer;

import com.example.justjoinparser.dto.OfferDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Delivery;
import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Component
@Slf4j
@RequiredArgsConstructor
public class RpcConsumer {

    private final Receiver receiver;
    private final Sender sender;
    protected final ObjectMapper objectMapper;

    @PostConstruct
    private void subscribe() {
        start().subscribe();
    }

    public Flux<Void> start() {
        return receiver.consumeAutoAck("offers.wroclaw") // replace with your request queue name
            .flatMap(this::processAndReply);
    }

    private Mono<Void> processAndReply(Delivery request) {
        try {
            OfferDto requestPayload = objectMapper.readValue(request.getBody(), OfferDto.class);
            String s = objectMapper.writeValueAsString(requestPayload);
            log.info("Has got RPC message");
            return sendReply(request.getProperties(), s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<Void> sendReply(AMQP.BasicProperties requestProperties, String responsePayload) {
        AMQP.BasicProperties replyProperties = new AMQP.BasicProperties.Builder()
            .correlationId(requestProperties.getCorrelationId())
            .build();

        return sender.send(Mono.just(
            new OutboundMessage("", requestProperties.getReplyTo(), replyProperties, responsePayload.getBytes())));
    }
}
