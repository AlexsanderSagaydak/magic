package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.ProfileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Integer> {

    Optional<ProfileView> findByViewerIdAndViewedWizardId(int viewerId, int viewedWizardId);

    List<ProfileView> findByViewerIdOrderByViewedAtDesc(int viewerId);

    boolean existsByViewerIdAndViewedWizardId(int viewerId, int viewedWizardId);

    long countByViewerId(int viewerId);

    // Get list of viewers for this wizard
    List<ProfileView> findByViewedWizardIdOrderByViewedAtDesc(int viewedWizardId);

    // Count views for this wizard
    long countByViewedWizardId(int viewedWizardId);
}
