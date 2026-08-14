package com.paytrack.paytrackpaymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic paymentInitiated(){
        return TopicBuilder.name("payment.initiated")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic paymentProcessed(){
        return TopicBuilder.name("payment.processed")
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic paymentFailed() {
        return TopicBuilder.name("payment.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentDLT() {
        return TopicBuilder.name("payment.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic paymentFraud() {
        return TopicBuilder.name("payment.fraud")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
