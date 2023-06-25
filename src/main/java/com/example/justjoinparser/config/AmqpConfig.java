package com.example.justjoinparser.config;

import static reactor.rabbitmq.ResourcesSpecification.binding;
import static reactor.rabbitmq.ResourcesSpecification.exchange;
import static reactor.rabbitmq.ResourcesSpecification.queue;

import com.example.justjoinparser.QueueBindingConfig;
import com.rabbitmq.client.ConnectionFactory;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
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
        Mono<?> declaration = sender.declare(exchange("offers.exchange").type("topic"));
        for (Map.Entry<String, Pair<String, String>> entry : QueueBindingConfig.QUEUE_BINDING.entrySet()) {
            declaration = declaration.then(sender.declare(queue(entry.getValue().getValue1())
                    .durable(true)
                    .exclusive(false)
                    .autoDelete(false)
                ))
                .then(sender.bind(binding(entry.getValue().getValue0(), entry.getKey(), entry.getValue().getValue1())));
        }

        declaration.subscribe(r -> log.info("Exchange, queues, and bindings declared and bound"));

        //sender.declare(exchange("offers.exchange").type("topic"))
        //    .then(sender.declare(queue("offers.warszawa")))
        //    .then(sender.bind(binding("offers.exchange", "offers.warszawa.*", "offers.warszawa")))
        //    .subscribe(r -> System.out.println("Exchange and queue declared and bound"));
    }
}
