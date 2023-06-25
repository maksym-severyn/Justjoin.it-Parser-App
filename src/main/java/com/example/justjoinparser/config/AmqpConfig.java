package com.example.justjoinparser.config;

import static reactor.rabbitmq.ResourcesSpecification.binding;
import static reactor.rabbitmq.ResourcesSpecification.exchange;
import static reactor.rabbitmq.ResourcesSpecification.queue;

import com.example.justjoinparser.amqp.QueueBindingVariables;
import com.rabbitmq.client.ConnectionFactory;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;
import reactor.util.retry.Retry;

@Configuration
@Slf4j
public class AmqpConfig {

    @Value("${message-broker-custom-config.broker.retry-on-fail-connection.max-attempts}")
    private long retryMaxAttempts;

    @Value("${message-broker-custom-config.broker.retry-on-fail-connection.exponential-backoff-ms}")
    private long exponentialBackoffMs;

    @Value("${message-broker-custom-config.broker.retry-on-fail-connection.jitter-factor}")
    private double jitterFactor;

    @Bean
    ConnectionFactory createConnectionMono(RabbitProperties rabbitProperties) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.useNio();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setAutomaticRecoveryEnabled(true);
        return connectionFactory;
    }

    @Bean
    SenderOptions senderOptions(ConnectionFactory connectionFactory) {
        return new SenderOptions()
            .connectionFactory(connectionFactory)
            .connectionMonoConfigurator(cm -> {
                log.info(
                    "In case of Connection lost, there will be retrying mechanism with {} attempts and {} ms backoff",
                    this.retryMaxAttempts, this.exponentialBackoffMs);
                return cm.retryWhen(
                    Retry.backoff(this.retryMaxAttempts, Duration.ofMillis(this.exponentialBackoffMs))
                        .jitter(this.jitterFactor)
                        .doAfterRetry(rs -> log.info("Retry to connect with message broker, attempt {}",
                            rs.totalRetries()))
                        .onRetryExhaustedThrow((spec, rs) -> rs.failure())
                );
            })
            .resourceManagementScheduler(Schedulers.boundedElastic());
    }

    @Bean
    public Sender sender(SenderOptions senderOptions) {
        Sender sender = RabbitFlux.createSender(senderOptions);
        bindQueues(sender);
        return sender;
    }

    private void bindQueues(Sender sender) {
        // Declare all the exchanges
        Flux<Void> declareExchanges = Flux.fromIterable(QueueBindingVariables.EXCHANGES.entrySet())
            .flatMap(entry -> {
                String exchangeName = entry.getKey();
                String exchangeType = entry.getValue().getName();
                return sender.declare(exchange(exchangeName).type(exchangeType)).then();
            });

        // Declare all the queues
        Flux<Void> declareQueues = Flux.fromIterable(QueueBindingVariables.QUEUES)
            .flatMap(queueName -> sender.declare(queue(queueName)).then());

        // Bind the queues
        Flux<Void> bindQueues = Flux.fromIterable(QueueBindingVariables.BINDINGS)
            .flatMap(bindingTriplet -> {
                String exchange = bindingTriplet.getValue0();
                String queue = bindingTriplet.getValue1();
                String routingKey = bindingTriplet.getValue2();
                return sender.bind(binding(exchange, routingKey, queue)).then();
            });

        // Combine all the operations
        Flux.concat(declareExchanges, declareQueues, bindQueues)
            .doOnTerminate(() -> log.info("Exchanges and queues declared and bound"))
            .subscribe();
    }
}
