package com.example.justjoinparser.config;

import static reactor.rabbitmq.ResourcesSpecification.binding;
import static reactor.rabbitmq.ResourcesSpecification.exchange;
import static reactor.rabbitmq.ResourcesSpecification.queue;

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
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
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
            .connectionSupplier(cf -> cf.newConnection("sender"))
            .connectionMonoConfigurator(cm -> {
                log.info(
                    "Sender: in case of Connection lost, there will be retrying mechanism with {} attempts and {}" +
                        "ms backoff", this.retryMaxAttempts, this.exponentialBackoffMs);
                return cm.retryWhen(
                    Retry.backoff(this.retryMaxAttempts, Duration.ofMillis(this.exponentialBackoffMs))
                        .jitter(this.jitterFactor)
                        .doAfterRetry(rs -> log.info("Sender: retry to connect with message broker, attempt {}",
                            rs.totalRetries()))
                        .onRetryExhaustedThrow((spec, rs) -> rs.failure())
                );
            })
            .resourceManagementScheduler(Schedulers.boundedElastic());
    }

    @Bean
    ReceiverOptions receiverOptionsOptions(ConnectionFactory connectionFactory) {
        return new ReceiverOptions()
            .connectionFactory(connectionFactory)
            .connectionSupplier(cf -> cf.newConnection("receiver"))
            .connectionMonoConfigurator(cm -> {
                log.info(
                    "Receiver: in case of Connection lost, there will be retrying mechanism with {} attempts and {}" +
                        "ms backoff", this.retryMaxAttempts, this.exponentialBackoffMs);
                return cm.retryWhen(
                    Retry.backoff(this.retryMaxAttempts, Duration.ofMillis(this.exponentialBackoffMs))
                        .jitter(this.jitterFactor)
                        .doAfterRetry(rs -> log.info("Receiver: retry to connect with message broker, attempt {}",
                            rs.totalRetries()))
                        .onRetryExhaustedThrow((spec, rs) -> rs.failure())
                );
            })
            .connectionSubscriptionScheduler(Schedulers.boundedElastic());
    }

    @Bean
    public Receiver receiver(ReceiverOptions receiverOptions) {
        return RabbitFlux.createReceiver(receiverOptions);
    }

    @Bean
    public Sender sender(SenderOptions senderOptions) {
        Sender sender = RabbitFlux.createSender(senderOptions);
        // declareAndBindQueuesAndExchanges(sender);
        return sender;
    }

    /**
     * Binding has been moved to a rabbitmq_definitions.json file, that is loaded
     * together with the launch of the RabbitMQ instance in Docker.
     * The binding code below leaved as an example.
     */
    private void declareAndBindQueuesAndExchanges(Sender sender) {
        // Declare all exchanges
        Flux<Void> declareExchanges = Flux.fromIterable(AmqpQueueBindingVariables.EXCHANGES.entrySet())
            .flatMap(entry -> {
                String exchangeName = entry.getKey();
                String exchangeType = entry.getValue().getName();
                return sender.declare(exchange(exchangeName).type(exchangeType)).then();
            });

        // Declare all queues
        Flux<Void> declareQueues = Flux.fromIterable(AmqpQueueBindingVariables.QUEUES)
            .flatMap(queueName -> sender.declare(queue(queueName)).then());

        // Bind queues
        Flux<Void> bindQueues = Flux.fromIterable(AmqpQueueBindingVariables.BINDINGS)
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
