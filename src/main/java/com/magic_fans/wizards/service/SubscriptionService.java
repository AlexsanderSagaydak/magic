package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.Subscription;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Transactional
    public Subscription subscribe(User subscriber, User wizard) {
        if (subscriptionRepository.existsBySubscriberIdAndWizardId(subscriber.getId(), wizard.getId())) {
            throw new IllegalStateException("Already subscribed");
        }
        Subscription subscription = new Subscription(subscriber, wizard);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(int subscriberId, int wizardId) {
        subscriptionRepository.deleteBySubscriberIdAndWizardId(subscriberId, wizardId);
    }

    public boolean isSubscribed(int subscriberId, int wizardId) {
        return subscriptionRepository.existsBySubscriberIdAndWizardId(subscriberId, wizardId);
    }

    public List<Subscription> getUserSubscriptions(int subscriberId) {
        return subscriptionRepository.findBySubscriberIdOrderBySubscribedAtDesc(subscriberId);
    }

    public List<Subscription> getWizardSubscribers(int wizardId) {
        return subscriptionRepository.findByWizardIdOrderBySubscribedAtDesc(wizardId);
    }

    public long getSubscriptionsCount(int subscriberId) {
        return subscriptionRepository.countBySubscriberId(subscriberId);
    }

    public long getWizardSubscribersCount(int wizardId) {
        return subscriptionRepository.countByWizardId(wizardId);
    }
}
