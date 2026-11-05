package com.example.chatbot.tool.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loads")
public class Load {

    @Id
    @Column(name = "load_id")
    private Long loadId;

    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;

    @Column(name = "origin", nullable = false, length = 100)
    private String origin;

    @Column(name = "destination", nullable = false, length = 100)
    private String destination;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "scheduled_delivery", nullable = false)
    private LocalDateTime scheduledDelivery;

    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    // Required by JPA
    public Load() {
    }

    public Load(
            Long loadId,
            String customerId,
            String origin,
            String destination,
            String status,
            LocalDateTime scheduledDelivery,
            LocalDateTime actualDelivery,
            Driver driver) {

        this.loadId = loadId;
        this.customerId = customerId;
        this.origin = origin;
        this.destination = destination;
        this.status = status;
        this.scheduledDelivery = scheduledDelivery;
        this.actualDelivery = actualDelivery;
        this.driver = driver;
    }

    public Long getLoadId() {
        return loadId;
    }

    public void setLoadId(Long loadId) {
        this.loadId = loadId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getScheduledDelivery() {
        return scheduledDelivery;
    }

    public void setScheduledDelivery(LocalDateTime scheduledDelivery) {
        this.scheduledDelivery = scheduledDelivery;
    }

    public LocalDateTime getActualDelivery() {
        return actualDelivery;
    }

    public void setActualDelivery(LocalDateTime actualDelivery) {
        this.actualDelivery = actualDelivery;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}
