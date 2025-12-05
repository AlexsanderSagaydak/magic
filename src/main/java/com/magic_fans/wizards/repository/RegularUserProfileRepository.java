package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.RegularUserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegularUserProfileRepository extends JpaRepository<RegularUserProfile, Integer> {
    Optional<RegularUserProfile> findByUserId(int userId);
}
