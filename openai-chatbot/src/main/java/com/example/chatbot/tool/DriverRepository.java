package com.example.chatbot.tool;

import com.example.chatbot.tool.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, Long> {
}
