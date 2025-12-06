package com.magic_fans.wizards.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Favorite entities with computed image URLs.
 * Used to display favorites in the UI with proper avatar and profile images.
 */
public class FavoriteDTO {
    private int id;
    private int wizardId;
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private String avatarUrl;
    private String profileImageUrl;
    private LocalDateTime addedAt;

    public FavoriteDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWizardId() {
        return wizardId;
    }

    public void setWizardId(int wizardId) {
        this.wizardId = wizardId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
