package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {

    Optional<Subscription> findBySubscriberIdAndWizardId(int subscriberId, int wizardId);

    List<Subscription> findBySubscriberIdOrderBySubscribedAtDesc(int subscriberId);

    boolean existsBySubscriberIdAndWizardId(int subscriberId, int wizardId);

    long countBySubscriberId(int subscriberId);

    void deleteBySubscriberIdAndWizardId(int subscriberId, int wizardId);

    // Get list of subscribers for this wizard
    List<Subscription> findByWizardIdOrderBySubscribedAtDesc(int wizardId);

    // Count subscribers for this wizard
    long countByWizardId(int wizardId);

    // Legacy methods for backward compatibility (regularUserId = subscriberId)
    default Optional<Subscription> findByRegularUserIdAndWizardId(int regularUserId, int wizardId) {
        return findBySubscriberIdAndWizardId(regularUserId, wizardId);
    }

    default boolean existsByRegularUserIdAndWizardId(int regularUserId, int wizardId) {
        return existsBySubscriberIdAndWizardId(regularUserId, wizardId);
    }

    default List<Subscription> findByRegularUserId(int regularUserId) {
        return findBySubscriberIdOrderBySubscribedAtDesc(regularUserId);
    }

    default List<Subscription> findByWizardId(int wizardId) {
        return findByWizardIdOrderBySubscribedAtDesc(wizardId);
    }
}
