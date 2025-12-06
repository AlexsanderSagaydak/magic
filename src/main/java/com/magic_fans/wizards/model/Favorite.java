package com.magic_fans.wizards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "favorite_wizard_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "favorite_wizard_id", nullable = false)
    private User favoriteWizard;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public Favorite() {
        this.addedAt = LocalDateTime.now();
    }

    public Favorite(User user, User favoriteWizard) {
        this.user = user;
        this.favoriteWizard = favoriteWizard;
        this.addedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFavoriteWizard() {
        return favoriteWizard;
    }

    public void setFavoriteWizard(User favoriteWizard) {
        this.favoriteWizard = favoriteWizard;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
}
