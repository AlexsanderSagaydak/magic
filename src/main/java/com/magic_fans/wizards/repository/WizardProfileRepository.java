package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.WizardProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WizardProfileRepository extends JpaRepository<WizardProfile, Integer> {
    Optional<WizardProfile> findByUserId(int userId);
}
