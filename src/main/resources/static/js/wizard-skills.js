/**
 * Wizard skills management
 */

/**
 * Toggles accordion section open/closed
 */
function toggleAccordion(button) {
    const content = button.nextElementSibling;
    const chevron = button.querySelector('.chevron');

    if (content.classList.contains('collapsed')) {
        content.classList.remove('collapsed');
        content.classList.add('expanded');
        chevron.classList.add('rotate-180');
    } else {
        content.classList.remove('expanded');
        content.classList.add('collapsed');
        chevron.classList.remove('rotate-180');
    }
}

/**
 * Load wizard skills from backend
 */
async function loadWizardSkills() {
    try {
        const response = await fetch('/api/wizard/skills');

        if (!response.ok) {
            console.error(`Failed to load skills: ${response.status}`);
            return;
        }

        const data = await response.json();

        if (!data.skills) {
            console.log('No skills data returned');
            return;
        }

        const skills = data.skills;

        // Apply section1 (with subsections)
        if (skills.section1) {
            Object.entries(skills.section1).forEach(([subsection, skillList]) => {
                if (Array.isArray(skillList)) {
                    skillList.forEach(skillName => {
                        const checkbox = document.querySelector(
                            `input[data-subsection="${subsection}"][value="${skillName}"]`
                        );
                        if (checkbox) {
                            checkbox.checked = true;
                        }
                    });
                }
            });
        }

        // Apply sections 2-7 (flat lists)
        ['section2', 'section3', 'section4', 'section5', 'section7'].forEach(section => {
            if (skills[section] && Array.isArray(skills[section])) {
                skills[section].forEach(skillName => {
                    const checkbox = document.querySelector(
                        `input[data-section="${section}"][value="${skillName}"]`
                    );
                    if (checkbox) {
                        checkbox.checked = true;
                    }
                });
            }
        });

        console.log('Skills loaded successfully');

    } catch (error) {
        console.error('Error loading wizard skills:', error);
    }
}

/**
 * Save wizard skills to backend
 */
async function saveWizardSkills() {
    const skills = {
        section1: {
            subsection1_1: [],
            subsection1_2: [],
            subsection1_3: [],
            subsection1_4: [],
            subsection1_5: []
        },
        section2: [],
        section3: [],
        section4: [],
        section5: [],
        section7: []
    };

    // Collect Section 1 (with subsections)
    ['subsection1_1', 'subsection1_2', 'subsection1_3', 'subsection1_4', 'subsection1_5'].forEach(subsection => {
        const checkboxes = document.querySelectorAll(`input[data-subsection="${subsection}"]:checked`);
        skills.section1[subsection] = Array.from(checkboxes).map(cb => cb.value);
    });

    // Collect Sections 2-7
    ['section2', 'section3', 'section4', 'section5', 'section7'].forEach(section => {
        const checkboxes = document.querySelectorAll(`input[data-section="${section}"]:checked`);
        skills[section] = Array.from(checkboxes).map(cb => cb.value);
    });

    // Get CSRF token
    const csrfToken = document.querySelector('input[name="_csrf"]')?.value;

    if (!csrfToken) {
        console.error('CSRF token not found');
        showToast('Ошибка: CSRF token не найден', 'error');
        return;
    }

    try {
        const response = await fetch('/api/wizard/skills', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ skills })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        if (data.success) {
            showToast('Навыки сохранены!', 'success');
            console.log('Skills saved successfully');

            // Reload page after 500ms to show updated skills
            setTimeout(() => {
                window.location.reload();
            }, 500);
        } else {
            throw new Error(data.message || 'Failed to save');
        }

    } catch (error) {
        console.error('Error saving wizard skills:', error);
        showToast('Ошибка сохранения навыков', 'error');
    }
}

/**
 * Initialize wizard skills on page load
 */
function initWizardSkills() {
    const accordion = document.querySelector('.wizard-skills-accordion');
    if (accordion) {
        loadWizardSkills();
    }
}

// Initialize on DOMContentLoaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initWizardSkills);
} else {
    initWizardSkills();
}
