package com.example.chatbot.tool.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    @Column(name = "phone", length = 30)
    private String phone;

    // Required by JPA
    public Driver() {
    }

    public Driver(Long driverId, String driverName, String phone) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.phone = phone;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

