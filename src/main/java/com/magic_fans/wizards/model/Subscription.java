package com.magic_fans.wizards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"subscriber_id", "wizard_id"}))
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wizard_id", nullable = false)
    private User wizard;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    public Subscription() {
        this.subscribedAt = LocalDateTime.now();
    }

    public Subscription(User subscriber, User wizard) {
        this.subscriber = subscriber;
        this.wizard = wizard;
        this.subscribedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
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

    @PrePersist
    protected void onCreate() {
        if (subscribedAt == null) {
            subscribedAt = LocalDateTime.now();
        }
    }
}
