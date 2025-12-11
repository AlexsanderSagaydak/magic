package com.magic_fans.wizards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_views",
       uniqueConstraints = @UniqueConstraint(columnNames = {"viewer_id", "viewed_wizard_id"}))
public class ProfileView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viewer_id", nullable = false)
    private User viewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viewed_wizard_id", nullable = false)
    private User viewedWizard;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public ProfileView() {
        this.viewedAt = LocalDateTime.now();
    }

    public ProfileView(User viewer, User viewedWizard) {
        this.viewer = viewer;
        this.viewedWizard = viewedWizard;
        this.viewedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getViewer() {
        return viewer;
    }

    public void setViewer(User viewer) {
        this.viewer = viewer;
    }

    public User getViewedWizard() {
        return viewedWizard;
    }

    public void setViewedWizard(User viewedWizard) {
        this.viewedWizard = viewedWizard;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (viewedAt == null) {
            viewedAt = LocalDateTime.now();
        }
    }
}
