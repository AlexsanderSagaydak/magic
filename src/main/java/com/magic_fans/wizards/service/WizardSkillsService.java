package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.WizardProfile;
import com.magic_fans.wizards.model.WizardSkill;
import com.magic_fans.wizards.repository.WizardProfileRepository;
import com.magic_fans.wizards.repository.WizardSkillRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WizardSkillsService {

    @Autowired
    private WizardSkillRepository wizardSkillRepository;

    @Autowired
    private WizardProfileRepository wizardProfileRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get skills for a wizard in structured format
     * Returns a map with section1 containing subsections, and section2-7 as simple lists
     */
    public Map<String, Object> getSkillsForWizard(int wizardProfileId) {
        List<WizardSkill> skills = wizardSkillRepository.findByWizardProfileId(wizardProfileId);

        Map<String, Object> result = new HashMap<>();

        // Section 1 with subsections
        Map<String, List<String>> section1 = new HashMap<>();
        section1.put("subsection1_1", new ArrayList<>());
        section1.put("subsection1_2", new ArrayList<>());
        section1.put("subsection1_3", new ArrayList<>());
        section1.put("subsection1_4", new ArrayList<>());
        section1.put("subsection1_5", new ArrayList<>());

        // Sections 2-7 (simple lists)
        result.put("section2", new ArrayList<String>());
        result.put("section3", new ArrayList<String>());
        result.put("section4", new ArrayList<String>());
        result.put("section5", new ArrayList<String>());
        result.put("section7", new ArrayList<String>());

        // Group skills by section and subsection
        for (WizardSkill skill : skills) {
            if ("section1".equals(skill.getSection())) {
                if (skill.getSubsection() != null && section1.containsKey(skill.getSubsection())) {
                    section1.get(skill.getSubsection()).add(skill.getSkillName());
                }
            } else {
                Object sectionList = result.get(skill.getSection());
                if (sectionList instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) sectionList;
                    list.add(skill.getSkillName());
                }
            }
        }

        result.put("section1", section1);
        return result;
    }

    /**
     * Save skills for a wizard (replaces all existing skills)
     * Expects skillsData in format:
     * {
     *   "section1": {
     *     "subsection1_1": ["Таро", "Руны"],
     *     "subsection1_2": ["Травы"]
     *   },
     *   "section2": ["Судьба", "Отношения"],
     *   ...
     * }
     */
    @Transactional
    public void saveSkillsForWizard(int wizardProfileId, Map<String, Object> skillsData) {
        // Find wizard profile
        WizardProfile wizardProfile = wizardProfileRepository.findById(wizardProfileId)
                .orElseThrow(() -> new RuntimeException("Wizard profile not found: " + wizardProfileId));

        // Delete existing skills
        wizardSkillRepository.deleteByWizardProfileId(wizardProfileId);

        // Force flush to execute DELETE before INSERT
        entityManager.flush();
        entityManager.clear();

        List<WizardSkill> newSkills = new ArrayList<>();

        // Process section1 (with subsections)
        if (skillsData.containsKey("section1")) {
            Object section1Data = skillsData.get("section1");
            if (section1Data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> section1 = (Map<String, List<String>>) section1Data;

                for (Map.Entry<String, List<String>> entry : section1.entrySet()) {
                    String subsection = entry.getKey();
                    List<String> skills = entry.getValue();

                    if (skills != null) {
                        for (String skillName : skills) {
                            if (skillName != null && !skillName.trim().isEmpty()) {
                                WizardSkill skill = new WizardSkill(
                                        wizardProfile,
                                        "section1",
                                        subsection,
                                        skillName.trim()
                                );
                                newSkills.add(skill);
                            }
                        }
                    }
                }
            }
        }

        // Process sections 2-7 (simple lists)
        for (String section : Arrays.asList("section2", "section3", "section4", "section5", "section7")) {
            if (skillsData.containsKey(section)) {
                Object sectionData = skillsData.get(section);
                if (sectionData instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> skills = (List<String>) sectionData;

                    for (String skillName : skills) {
                        if (skillName != null && !skillName.trim().isEmpty()) {
                            WizardSkill skill = new WizardSkill(
                                    wizardProfile,
                                    section,
                                    null,  // no subsection for sections 2-7
                                    skillName.trim()
                            );
                            newSkills.add(skill);
                        }
                    }
                }
            }
        }

        // Save all skills in batch
        if (!newSkills.isEmpty()) {
            wizardSkillRepository.saveAll(newSkills);
        }
    }

    /**
     * Filter wizard profile IDs by skills (OR logic - any skill matches)
     */
    public List<Integer> filterWizardsBySkills(List<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return Collections.emptyList();
        }
        return wizardSkillRepository.findWizardProfileIdsBySkillNames(skillNames);
    }

    /**
     * Filter wizard profile IDs by skills (AND logic - all skills must match)
     */
    public List<Integer> filterWizardsByAllSkills(List<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return Collections.emptyList();
        }
        return wizardSkillRepository.findWizardProfileIdsByAllSkills(skillNames, skillNames.size());
    }

    /**
     * Get count of skills for a wizard
     */
    public long getSkillCount(int wizardProfileId) {
        return wizardSkillRepository.countByWizardProfileId(wizardProfileId);
    }

    /**
     * Check if wizard has a specific skill
     */
    public boolean hasSkill(int wizardProfileId, String skillName) {
        return wizardSkillRepository.existsByWizardProfileIdAndSkillName(wizardProfileId, skillName);
    }

    /**
     * Get all skills for a wizard as a flat list (for displaying as tags)
     */
    public List<String> getAllSkillsForWizard(int wizardProfileId) {
        List<WizardSkill> skills = wizardSkillRepository.findByWizardProfileId(wizardProfileId);
        List<String> skillNames = new ArrayList<>();
        for (WizardSkill skill : skills) {
            skillNames.add(skill.getSkillName());
        }
        return skillNames;
    }
}
