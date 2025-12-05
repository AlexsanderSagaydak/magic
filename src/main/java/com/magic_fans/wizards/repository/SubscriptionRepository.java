package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByRegularUserIdAndWizardId(int regularUserId, int wizardId);
    List<Subscription> findByRegularUserId(int regularUserId);
    List<Subscription> findByWizardId(int wizardId);
    boolean existsByRegularUserIdAndWizardId(int regularUserId, int wizardId);
}
