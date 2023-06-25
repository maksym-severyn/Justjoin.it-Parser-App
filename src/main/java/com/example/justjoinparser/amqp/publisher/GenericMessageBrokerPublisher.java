package com.example.justjoinparser.amqp.publisher;

import com.example.justjoinparser.dto.OfferDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RpcClient;
import reactor.rabbitmq.Sender;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenericMessageBrokerPublisher<T> {

    private final Sender sender;
    private final ObjectMapper objectMapper;

    @PreDestroy
    public void closeSender() {
        sender.close();
    }

    /**
     * Publish message via RabbitMQ.
     *
     * @param exchange   exchange to message should be published
     * @param routingKey used for forward message to one or more queues
     * @param body       flux or mono with body of message
     */
    public void sendMessage(String exchange, String routingKey, T body) {
        RpcClient rpcClient = sender.rpcClient(exchange, routingKey, () -> UUID.randomUUID().toString());

        rpcClient
            .rpc(Mono.just(new RpcClient.RpcRequest(convertObjectToString(body).getBytes())))
            .subscribe(delivery -> {
                OfferDto response = convertBytesToObject(delivery.getBody());
                log.info("RESPONSE HAVE GOT!!!!! {}", response.id());
            });
        //        rpcClient.close();
    }

    private String convertObjectToString(T body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private OfferDto convertBytesToObject(byte[] body) {
        try {
            return objectMapper.readValue(body, OfferDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
