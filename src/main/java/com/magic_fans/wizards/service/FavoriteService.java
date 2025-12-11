package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.Favorite;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Transactional
    public Favorite addToFavorites(User user, User wizard) {
        if (favoriteRepository.existsByUserIdAndFavoriteWizardId(user.getId(), wizard.getId())) {
            throw new IllegalStateException("Wizard already in favorites");
        }
        Favorite favorite = new Favorite(user, wizard);
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFromFavorites(int userId, int wizardId) {
        favoriteRepository.deleteByUserIdAndFavoriteWizardId(userId, wizardId);
    }

    public boolean isFavorite(int userId, int wizardId) {
        return favoriteRepository.existsByUserIdAndFavoriteWizardId(userId, wizardId);
    }

    public List<Favorite> getUserFavorites(int userId) {
        return favoriteRepository.findByUserIdOrderByAddedAtDesc(userId);
    }

    public List<Favorite> getWizardFavoredBy(int wizardId) {
        return favoriteRepository.findByFavoriteWizardIdOrderByAddedAtDesc(wizardId);
    }

    public long getFavoritesCount(int userId) {
        return favoriteRepository.countByUserId(userId);
    }

    public long getWizardFavoritesCount(int wizardId) {
        return favoriteRepository.countByFavoriteWizardId(wizardId);
    }
}
