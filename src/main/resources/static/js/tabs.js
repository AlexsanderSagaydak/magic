/**
 * Tab switching functionality
 */

function switchToTab(tabName) {
    // Hide all tab contents
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.add('hidden');
    });

    // Remove active styles from all buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('border-accent', 'text-accent');
        btn.classList.add('border-transparent', 'text-gray-400');
    });

    // Show selected tab content
    const tabContent = document.getElementById(`tab-${tabName}`);
    if (tabContent) {
        tabContent.classList.remove('hidden');
    }

    // Add active styles to clicked button
    const activeButton = document.querySelector(`.tab-btn[data-tab="${tabName}"]`);
    if (activeButton) {
        activeButton.classList.remove('border-transparent', 'text-gray-400');
        activeButton.classList.add('border-accent', 'text-accent');
    }

    // Save active tab to localStorage
    localStorage.setItem('activeTab', tabName);
}

function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabName = button.getAttribute('data-tab');
            switchToTab(tabName);
        });
    });

    // Restore active tab from localStorage only if it was a post action reload
    const isPostActionReload = localStorage.getItem('postActionReload');
    const savedTab = localStorage.getItem('activeTab');

    if (isPostActionReload === 'true' && savedTab) {
        switchToTab(savedTab);
        // Clear the flag after using it
        localStorage.removeItem('postActionReload');
    }
    // Otherwise default tab 'profile' will be shown (already active in HTML)
}

// Initialize on DOMContentLoaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTabs);
} else {
    initTabs();
}
