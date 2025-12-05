package com.magic_fans.wizards.config;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.model.WizardProfile;
import com.magic_fans.wizards.service.UserService;
import com.magic_fans.wizards.repository.WizardProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "magic-fans.data-loader.enabled", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WizardProfileRepository wizardProfileRepository;

    @Override
    public void run(String... args) throws Exception {
        // Load fake wizard users if they don't exist
        loadFakeWizards();
    }

    private void loadFakeWizards() {
        // Check if wizards already exist
        if (userService.getUserByUsername("merlin").isPresent()) {
            return; // Already loaded
        }

        String[][] wizards = {
            {"merlin", "merlin@wizards.com", "Merlin", "The Great", "White Magic"},
            {"morgana", "morgana@wizards.com", "Morgana", "The Wise", "Black Magic"},
            {"gandalf", "gandalf@wizards.com", "Gandalf", "The Grey", "Gray Magic"},
            {"dumbledore", "dumbledore@wizards.com", "Albus", "Dumbledore", "Elemental Magic"},
            {"circe", "circe@wizards.com", "Circe", "The Enchantress", "Illusion Magic"},
            {"merlyn", "merlyn@wizards.com", "Merlyn", "The Mystic", "Divination"},
            {"nimue", "nimue@wizards.com", "Nimue", "The Lady", "Healing Magic"},
            {"radagast", "radagast@wizards.com", "Radagast", "The Brown", "Transmutation"}
        };

        for (String[] wizardData : wizards) {
            try {
                User wizard = new User();
                wizard.setUsername(wizardData[0]);
                wizard.setEmail(wizardData[1]);
                wizard.setFirstName(wizardData[2]);
                wizard.setLastName(wizardData[3]);
                wizard.setSpecialization(wizardData[4]);
                wizard.setRole("wizard");
                wizard.setActive(true);

                // Encode password: all wizard accounts have password "wizard123"
                String encodedPassword = passwordEncoder.encode("wizard123");
                wizard.setPassword(encodedPassword);

                // Create wizard profile
                WizardProfile wizardProfile = new WizardProfile(wizard);

                // Add some random years of experience (5-30 years)
                int yearsOfExp = 5 + (int)(Math.random() * 25);
                wizardProfile.setYearsOfExperience(yearsOfExp);

                // Add some random skills
                Set<String> skills = new HashSet<>();
                String[] availableSkills = {
                    "Spell Casting", "Potion Making", "Divination",
                    "Transmutation", "Defense Magic", "Conjuration"
                };

                int skillCount = 2 + (int)(Math.random() * 3); // 2-4 skills per wizard
                for (int i = 0; i < skillCount; i++) {
                    int randomIdx = (int)(Math.random() * availableSkills.length);
                    skills.add(availableSkills[randomIdx]);
                }
                wizardProfile.setSkills(skills);

                wizard.setWizardProfile(wizardProfile);

                // Save wizard and profile
                userService.saveUser(wizard);

                System.out.println("✓ Loaded fake wizard: " + wizardData[0]);
            } catch (Exception e) {
                System.err.println("✗ Error loading wizard " + wizardData[0] + ": " + e.getMessage());
            }
        }
    }
}
