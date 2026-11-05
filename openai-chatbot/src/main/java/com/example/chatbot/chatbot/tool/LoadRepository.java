package com.example.chatbot.tool;


import com.example.chatbot.tool.entity.Load;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoadRepository extends JpaRepository<Load, Long> {

    @Query("SELECT l FROM Load l WHERE l.customerId = :customerId AND l.status <> 'DELIVERED' AND l.scheduledDelivery < :now ORDER BY l.scheduledDelivery ASC ")
    List<Load> findDelayedLoads(@Param("customerId") String customerId, @Param("now") LocalDateTime now);



}
