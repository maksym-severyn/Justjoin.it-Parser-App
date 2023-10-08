package com.example.justjoinparser.amqp.consumer;

import com.example.justjoinparser.exception.CannotParseMessageException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.AcknowledgableDelivery;
import reactor.rabbitmq.ConsumeOptions;
import reactor.rabbitmq.ExceptionHandlers;
import reactor.rabbitmq.Receiver;

@Slf4j
abstract class MessageBrokerAbstractConsumer<T> {

    private final Class<T> targetClass;

    @Value("${message-broker-custom-config.message.retry-on-fail.max-duration-sec}")
    private long retryMaxDurationSeconds;

    @Value("${message-broker-custom-config.message.retry-on-fail.interval-ms}")
    private long retryIntervalMs;

    private final String queueToListen;
    private final Receiver receiver;
    protected final ObjectMapper objectMapper;

    protected MessageBrokerAbstractConsumer(String queueToListen, Receiver receiver, ObjectMapper objectMapper,
                                            Class<T> targetClass) {
        this.queueToListen = queueToListen;
        this.receiver = receiver;
        this.objectMapper = objectMapper;
        this.targetClass = targetClass;
    }

    @PreDestroy
    protected void closeSender() {
        receiver.close();
    }

    @PostConstruct
    private void subscribe() {
        listen().subscribe();
    }

    /**
     * Should be override to implement message handling.
     *
     * @param body expected inbound message body
     * @return
     */
    protected abstract Mono<Void> handleMessage(T body);

    private Mono<Void> listen() {
        return receiver.consumeManualAck(
                this.queueToListen,
                new ConsumeOptions().exceptionHandler(
                    new ExceptionHandlers.RetryAcknowledgmentExceptionHandler(
                        Duration.ofSeconds(retryMaxDurationSeconds), Duration.ofMillis(retryIntervalMs),
                        ExceptionHandlers.CONNECTION_RECOVERY_PREDICATE
                    ))
            )
            .flatMap(acknowledgableDelivery -> handleDelivery(acknowledgableDelivery, this.targetClass))
            .onErrorContinue((throwable, o) ->
                log.error("Fail occurred during consuming queue %s. Message caused an error will be omitted"
                    .formatted(this.queueToListen), throwable))
            .flatMap(this::handleMessage)
            .then();
    }

    private Flux<T> handleDelivery(AcknowledgableDelivery delivery, Class<T> targetClass) {
        T data = convertBytesToObject(delivery.getBody(), targetClass);
        delivery.ack();
        return Flux.just(data);
    }

    private T convertBytesToObject(byte[] object, Class<T> elementClass) {
        try {
            return objectMapper.readValue(object, elementClass);
        } catch (IOException e) {
            log.error("Cannot parse inbound message!");
            throw new CannotParseMessageException(e);
        }
    }
}
