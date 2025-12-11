package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.ProfileView;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.ProfileViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileViewService {

    @Autowired
    private ProfileViewRepository profileViewRepository;

    @Transactional
    public ProfileView recordView(User viewer, User viewedWizard) {
        // Check if view already exists
        Optional<ProfileView> existingView = profileViewRepository.findByViewerIdAndViewedWizardId(
            viewer.getId(), viewedWizard.getId()
        );

        if (existingView.isPresent()) {
            // Update existing view timestamp
            ProfileView view = existingView.get();
            view.setViewedAt(LocalDateTime.now());
            return profileViewRepository.save(view);
        } else {
            // Create new view
            ProfileView view = new ProfileView(viewer, viewedWizard);
            return profileViewRepository.save(view);
        }
    }

    public boolean hasViewed(int viewerId, int viewedWizardId) {
        return profileViewRepository.existsByViewerIdAndViewedWizardId(viewerId, viewedWizardId);
    }

    public List<ProfileView> getViewerHistory(int viewerId) {
        return profileViewRepository.findByViewerIdOrderByViewedAtDesc(viewerId);
    }

    public List<ProfileView> getWizardViewers(int viewedWizardId) {
        return profileViewRepository.findByViewedWizardIdOrderByViewedAtDesc(viewedWizardId);
    }

    public long getViewerHistoryCount(int viewerId) {
        return profileViewRepository.countByViewerId(viewerId);
    }

    public long getWizardViewsCount(int viewedWizardId) {
        return profileViewRepository.countByViewedWizardId(viewedWizardId);
    }
}
