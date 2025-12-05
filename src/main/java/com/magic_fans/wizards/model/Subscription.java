package com.magic_fans.wizards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"regular_user_id", "wizard_id"}))
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regular_user_id", nullable = false)
    private User regularUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wizard_id", nullable = false)
    private User wizard;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    public Subscription() {
    }

    public Subscription(User regularUser, User wizard) {
        this.regularUser = regularUser;
        this.wizard = wizard;
        this.subscribedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getRegularUser() {
        return regularUser;
    }

    public void setRegularUser(User regularUser) {
        this.regularUser = regularUser;
    }

    public User getWizard() {
        return wizard;
    }

    public void setWizard(User wizard) {
        this.wizard = wizard;
    }

    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(LocalDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }
}
