package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.WizardService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WizardServiceRepository extends JpaRepository<WizardService, Long> {
    List<WizardService> findByUserId(Integer userId);
}
