package com.magic_fans.wizards.dto;

import java.util.List;

/**
 * Data Transfer Object for user profile in feed/listing view.
 * Used to send user profile data to frontend for infinite scroll feed.
 *
 * @author Magic Fans Team
 * @version 1.0
 */
public class UserProfileDTO {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean online;
    private String avatarUrl;
    private String profileImageUrl;
    private String videoUrl;
    private List<String> skills;

    /**
     * Default constructor for UserProfileDTO.
     */
    public UserProfileDTO() {
    }

    /**
     * Constructor with all fields.
     *
     * @param id the user ID
     * @param username the username
     * @param firstName the first name
     * @param lastName the last name
     * @param specialization the magical specialization
     * @param online whether user is online
     * @param avatarUrl the avatar image URL
     * @param profileImageUrl the profile cover image URL
     * @param videoUrl the video URL
     */
    public UserProfileDTO(int id, String username, String firstName, String lastName,
                         String specialization, boolean online, String avatarUrl,
                         String profileImageUrl, String videoUrl) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.online = online;
        this.avatarUrl = avatarUrl;
        this.profileImageUrl = profileImageUrl;
        this.videoUrl = videoUrl;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id the user ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     *
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     *
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the magical specialization.
     *
     * @return the specialization
     */
    public String getSpecialization() {
        return specialization;
    }

    /**
     * Sets the magical specialization.
     *
     * @param specialization the specialization to set
     */
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    /**
     * Checks if user is online.
     *
     * @return true if user is online, false otherwise
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Sets the online status.
     *
     * @param online true if user is online, false otherwise
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Gets the avatar image URL.
     *
     * @return the avatar URL
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Sets the avatar image URL.
     *
     * @param avatarUrl the avatar URL to set
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Gets the profile cover image URL.
     *
     * @return the profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the profile cover image URL.
     *
     * @param profileImageUrl the profile image URL to set
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Gets the video URL.
     *
     * @return the video URL
     */
    public String getVideoUrl() {
        return videoUrl;
    }

    /**
     * Sets the video URL.
     *
     * @param videoUrl the video URL to set
     */
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    /**
     * Gets the list of wizard skills.
     *
     * @return the list of skills
     */
    public List<String> getSkills() {
        return skills;
    }

    /**
     * Sets the list of wizard skills.
     *
     * @param skills the list of skills to set
     */
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}