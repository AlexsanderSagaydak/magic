package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    Optional<Favorite> findByUserIdAndFavoriteWizardId(int userId, int favoriteWizardId);

    List<Favorite> findByUserIdOrderByAddedAtDesc(int userId);

    boolean existsByUserIdAndFavoriteWizardId(int userId, int favoriteWizardId);

    long countByUserId(int userId);

    void deleteByUserIdAndFavoriteWizardId(int userId, int favoriteWizardId);
}
