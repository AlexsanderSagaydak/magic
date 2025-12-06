package com.magic_fans.wizards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wizard_profile_skills",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wizard_skill",
                columnNames = {"wizard_profile_id", "skill_name"}
        ))
public class WizardSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wizard_profile_id", nullable = false)
    private WizardProfile wizardProfile;

    @Column(name = "section", nullable = false, length = 50)
    private String section;

    @Column(name = "subsection", length = 50)
    private String subsection;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public WizardSkill() {
        this.createdAt = LocalDateTime.now();
    }

    public WizardSkill(WizardProfile wizardProfile, String section, String subsection, String skillName) {
        this.wizardProfile = wizardProfile;
        this.section = section;
        this.subsection = subsection;
        this.skillName = skillName;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WizardProfile getWizardProfile() {
        return wizardProfile;
    }

    public void setWizardProfile(WizardProfile wizardProfile) {
        this.wizardProfile = wizardProfile;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubsection() {
        return subsection;
    }

    public void setSubsection(String subsection) {
        this.subsection = subsection;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "WizardSkill{" +
                "id=" + id +
                ", section='" + section + '\'' +
                ", subsection='" + subsection + '\'' +
                ", skillName='" + skillName + '\'' +
                '}';
    }
}
