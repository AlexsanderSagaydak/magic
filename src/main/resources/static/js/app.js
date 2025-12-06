/**
 * Loads the favorite count badge in the sidebar
 */
async function loadFavoriteCount() {
    try {
        const response = await fetch('/favorites/count');

        if (!response.ok) {
            console.error(`Failed to load favorite count: ${response.status}`);
            return;
        }

        const data = await response.json();
        const countElement = document.getElementById('favoriteCount');

        if (countElement) {
            countElement.textContent = data.count;

            // Show/hide badge based on count
            if (data.count > 0) {
                countElement.style.display = 'inline-block';
            } else {
                countElement.style.display = 'none';
            }
        }
    } catch (error) {
        console.error('Error loading favorite count:', error);
    }
}

/**
 * Shows a toast notification to the user.
 * @param {string} message - The message to display
 * @param {string} type - The type of toast ('success', 'error', 'info')
 */
function showToast(message, type = 'info') {
    // Remove existing toast if any
    const existingToast = document.querySelector('.toast-notification');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = 'toast-notification fixed bottom-4 right-4 px-6 py-3 rounded-lg shadow-lg z-50 transition-all transform translate-y-0 opacity-100';

    // Set color based on type
    const colors = {
        success: 'bg-green-600 text-white',
        error: 'bg-red-600 text-white',
        info: 'bg-blue-600 text-white'
    };

    toast.className += ' ' + (colors[type] || colors.info);
    toast.textContent = message;

    document.body.appendChild(toast);

    // Animate in
    setTimeout(() => {
        toast.style.transform = 'translateY(0)';
    }, 10);

    // Remove after 3 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Auto-load favorite count on all pages
document.addEventListener('DOMContentLoaded', () => {
    loadFavoriteCount();
});
