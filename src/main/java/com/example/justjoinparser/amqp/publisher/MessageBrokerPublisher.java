package com.example.justjoinparser.amqp.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.ExceptionHandlers;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.OutboundMessageResult;
import reactor.rabbitmq.SendOptions;
import reactor.rabbitmq.Sender;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageBrokerPublisher {

    private SendOptions sendOptions;

    @Value("${message-broker-custom-config.message.expiration-time-ms}")
    private long messageExpirationMs;

    @Value("${message-broker-custom-config.message.retry-on-fail.max-duration-sec}")
    private long retryMaxDurationSeconds;

    @Value("${message-broker-custom-config.message.retry-on-fail.interval-ms}")
    private long retryIntervalMs;

    private final Sender sender;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        sendOptions = new SendOptions()
            .trackReturned(true)
            .exceptionHandler(
                new ExceptionHandlers.RetrySendingExceptionHandler(
                    Duration.ofSeconds(retryMaxDurationSeconds),
                    Duration.ofMillis(retryIntervalMs),
                    ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE
                )
            );
    }

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
    public void sendMessage(String exchange, String routingKey, Publisher<Object> body) {
        sender.sendWithPublishConfirms(
                Flux.from(body).map(obj -> getOutboundMessage(exchange, routingKey, obj)),
                this.sendOptions
            )
            .onErrorContinue((e, obj) -> log.error("Cannot send event due to:\n{}", e.getMessage(), e))
            .subscribe(MessageBrokerPublisher::checkWhetherIsAckOrReturned);
    }

    public void sendMessage(String exchange, String routingKey, Object body) {
        sender.sendWithPublishConfirms(
                Mono.fromCallable(() -> getOutboundMessage(exchange, routingKey, body)), this.sendOptions
            )
            .onErrorContinue((e, obj) -> log.error("Cannot send event due to:\n{}", e.getMessage(), e))
            .subscribe(MessageBrokerPublisher::checkWhetherIsAckOrReturned);
    }

    private OutboundMessage getOutboundMessage(String exchange, String routingKey, Object body) {
        try {
            return new OutboundMessage(
                exchange,
                routingKey,
                new AMQP.BasicProperties.Builder()
                    .timestamp(Date.from(Instant.now()))
                    .contentType(MessageProperties.CONTENT_TYPE_JSON)
                    .messageId(UUID.randomUUID().toString())
                    .expiration(String.valueOf(this.messageExpirationMs))
                    .build(),
                convertObjectToString(body).getBytes());
        } catch (JsonProcessingException e) {
            log.error("Cannot send event due to:\n{}!", e.getMessage(), e);
            throw new CannotSendEventException(e.getMessage());
        }
    }

    private String convertObjectToString(Object body) throws JsonProcessingException {
        return objectMapper.writeValueAsString(body);
    }

    private static void checkWhetherIsAckOrReturned(OutboundMessageResult m) {
        if (m.isReturned()) {
            log.info("Message with id: {} was not routed to any queue!",
                Objects.requireNonNull(m.getOutboundMessage().getProperties()).getMessageId());
        }
        if (m.isAck()) {
            log.debug("Message with id: {} has reached the broker",
                Objects.requireNonNull(m.getOutboundMessage().getProperties()).getMessageId());
        }
    }

    private static class CannotSendEventException extends RuntimeException {
        public CannotSendEventException(String message) {
            super(message);
        }
    }
}
