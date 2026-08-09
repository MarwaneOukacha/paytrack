package com.paytrack.paytrackpaymentservice.repository;

import com.paytrack.paytrackpaymentservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findBySentFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.sent = true AND o.sentAt < :before")
    void deleteOldSentEvents(@Param("before") LocalDateTime before);

    boolean existsByKafkaKeyAndTopic(String kafkaKey, String topic);
}
