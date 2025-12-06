package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.WizardSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WizardSkillRepository extends JpaRepository<WizardSkill, Long> {

    /**
     * Find all skills for a specific wizard profile
     */
    List<WizardSkill> findByWizardProfileId(int wizardProfileId);

    /**
     * Delete all skills for a specific wizard profile (used when re-saving)
     */
    void deleteByWizardProfileId(int wizardProfileId);

    /**
     * Find wizard profile IDs that have ANY of the specified skills (OR logic)
     * Used for filtering wizards by skills on the feed page
     */
    @Query("SELECT DISTINCT ws.wizardProfile.id FROM WizardSkill ws WHERE ws.skillName IN :skillNames")
    List<Integer> findWizardProfileIdsBySkillNames(@Param("skillNames") List<String> skillNames);

    /**
     * Find wizard profile IDs that have ALL of the specified skills (AND logic)
     * Alternative filtering approach - more strict
     */
    @Query("SELECT ws.wizardProfile.id FROM WizardSkill ws " +
           "WHERE ws.skillName IN :skillNames " +
           "GROUP BY ws.wizardProfile.id " +
           "HAVING COUNT(DISTINCT ws.skillName) = :count")
    List<Integer> findWizardProfileIdsByAllSkills(
            @Param("skillNames") List<String> skillNames,
            @Param("count") long count
    );

    /**
     * Count skills for a wizard profile
     */
    long countByWizardProfileId(int wizardProfileId);

    /**
     * Check if a wizard has a specific skill
     */
    boolean existsByWizardProfileIdAndSkillName(int wizardProfileId, String skillName);

    /**
     * Find wizard profile IDs by section (e.g., all wizards with any skill in section1)
     */
    @Query("SELECT DISTINCT ws.wizardProfile.id FROM WizardSkill ws WHERE ws.section = :section")
    List<Integer> findWizardProfileIdsBySection(@Param("section") String section);

    /**
     * Find wizard profile IDs by subsection
     */
    @Query("SELECT DISTINCT ws.wizardProfile.id FROM WizardSkill ws WHERE ws.subsection = :subsection")
    List<Integer> findWizardProfileIdsBySubsection(@Param("subsection") String subsection);
}
