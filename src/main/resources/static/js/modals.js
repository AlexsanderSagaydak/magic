/**
 * Modal windows functionality
 */

/**
 * Open image modal for full-size view
 */
function openImageModal(imageSrc) {
    const modal = document.getElementById('imageModal');
    const modalImage = document.getElementById('modalImage');
    if (modal && modalImage) {
        modalImage.src = imageSrc;
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

/**
 * Close image modal
 */
function closeImageModal() {
    const modal = document.getElementById('imageModal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

/**
 * Show custom alert modal
 */
function showAlert(message, type = 'info') {
    const modal = document.getElementById('customAlertModal');
    const messageEl = document.getElementById('customAlertMessage');
    const iconEl = document.getElementById('customAlertIcon');

    if (!modal || !messageEl || !iconEl) return;

    messageEl.textContent = message;

    // Set icon based on type
    if (type === 'error') {
        iconEl.innerHTML = `
            <svg class="w-12 h-12 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
        `;
    } else if (type === 'success') {
        iconEl.innerHTML = `
            <svg class="w-12 h-12 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
        `;
    } else {
        iconEl.innerHTML = `
            <svg class="w-12 h-12 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
        `;
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

/**
 * Close custom alert modal
 */
function closeAlert() {
    const modal = document.getElementById('customAlertModal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

/**
 * Show custom confirm modal
 */
function showConfirm(message, onConfirm, onCancel = null) {
    const modal = document.getElementById('customConfirmModal');
    const messageEl = document.getElementById('customConfirmMessage');

    if (!modal || !messageEl) return;

    messageEl.textContent = message;

    // Set up confirm button
    const confirmBtn = document.getElementById('customConfirmButton');
    const cancelBtn = document.getElementById('customCancelButton');

    // Remove old event listeners by cloning
    const newConfirmBtn = confirmBtn.cloneNode(true);
    const newCancelBtn = cancelBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
    cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);

    // Add new event listeners
    newConfirmBtn.addEventListener('click', () => {
        closeConfirm();
        if (onConfirm) onConfirm();
    });

    newCancelBtn.addEventListener('click', () => {
        closeConfirm();
        if (onCancel) onCancel();
    });

    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

/**
 * Close custom confirm modal
 */
function closeConfirm() {
    const modal = document.getElementById('customConfirmModal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}
