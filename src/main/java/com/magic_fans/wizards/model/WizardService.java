package com.magic_fans.wizards.model;

import jakarta.persistence.*;

@Entity
@Table(name = "wizard_services")
public class WizardService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private Double price;

    public WizardService() {
    }

    public WizardService(Integer userId, String serviceName, Double price) {
        this.userId = userId;
        this.serviceName = serviceName;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
