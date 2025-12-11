package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.WizardService;
import com.magic_fans.wizards.repository.WizardServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WizardServiceService {

    @Autowired
    private WizardServiceRepository wizardServiceRepository;

    public List<WizardService> getServicesByUserId(Integer userId) {
        return wizardServiceRepository.findByUserId(userId);
    }

    public WizardService saveService(WizardService service) {
        return wizardServiceRepository.save(service);
    }

    public void deleteService(Long serviceId) {
        wizardServiceRepository.deleteById(serviceId);
    }

    public Optional<WizardService> getServiceById(Long id) {
        return wizardServiceRepository.findById(id);
    }
}
