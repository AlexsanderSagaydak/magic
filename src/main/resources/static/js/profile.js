/**
 * Profile page common functionality
 */

/**
 * Toggle edit mode for profile card
 */
function toggleEditMode() {
    const card = document.getElementById('profileCard');
    card.classList.toggle('edit-mode');
}

/**
 * Initialize character counter for About Me field
 */
function initCharacterCounter(fieldId, counterId) {
    const aboutMeField = document.getElementById(fieldId);
    const charCount = document.getElementById(counterId);

    if (aboutMeField && charCount) {
        // Update counter on page load
        updateCharCount();

        // Update counter on input
        aboutMeField.addEventListener('input', updateCharCount);

        function updateCharCount() {
            const length = aboutMeField.value.length;
            charCount.textContent = `${length} / 200`;

            // Change color based on remaining characters
            if (length > 180) {
                charCount.classList.add('text-yellow-500');
                charCount.classList.remove('text-gray-500');
            } else {
                charCount.classList.add('text-gray-500');
                charCount.classList.remove('text-yellow-500');
            }
        }
    }
}
