package com.paytrack.fraudservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentFraudEvaluated() {
        return TopicBuilder.name("fraud.fraud-evaluated")
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic paymentFraud() {
        return TopicBuilder.name("fraud.fraud-check")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
