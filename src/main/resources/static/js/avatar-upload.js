/**
 * Avatar upload functionality with cropper
 */

let cropper = null;
let selectedAvatarFile = null;

function openAvatarUpload() {
    document.getElementById('avatarModal').classList.remove('hidden');
}

function closeAvatarUpload() {
    document.getElementById('avatarModal').classList.add('hidden');
    resetAvatarCrop();
}

function handleAvatarSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
        alert('Invalid file format. Please use JPEG, PNG, or WebP');
        return;
    }

    // Validate file size (5MB)
    const maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
        alert('File is too large. Maximum size is 5MB');
        return;
    }

    selectedAvatarFile = file;

    // Show crop area
    const reader = new FileReader();
    reader.onload = (e) => {
        const img = document.getElementById('avatarCropImage');
        img.src = e.target.result;

        document.getElementById('avatarUploadArea').classList.add('hidden');
        document.getElementById('avatarCropArea').classList.remove('hidden');

        // Initialize Cropper.js
        if (cropper) {
            cropper.destroy();
        }
        cropper = new Cropper(img, {
            aspectRatio: 1, // Square
            viewMode: 2,
            dragMode: 'move',
            autoCropArea: 1,
            restore: false,
            guides: true,
            center: true,
            highlight: false,
            cropBoxMovable: true,
            cropBoxResizable: true,
            toggleDragModeOnDblclick: false,
        });
    };
    reader.readAsDataURL(file);
}

function rotateAvatar(degree) {
    if (cropper) {
        cropper.rotate(degree);
    }
}

function resetAvatarCrop() {
    if (cropper) {
        cropper.destroy();
        cropper = null;
    }
    document.getElementById('avatarInput').value = '';
    document.getElementById('avatarUploadArea').classList.remove('hidden');
    document.getElementById('avatarCropArea').classList.add('hidden');
    selectedAvatarFile = null;
}

function cancelAvatarUpload() {
    resetAvatarCrop();
    closeAvatarUpload();
}

async function uploadAvatar(avatarImageId = 'profileAvatar') {
    if (!cropper) return;

    // Get cropped canvas (400x400 for avatar)
    const canvas = cropper.getCroppedCanvas({
        width: 400,
        height: 400,
        imageSmoothingEnabled: true,
        imageSmoothingQuality: 'high',
    });

    // Convert to blob
    canvas.toBlob(async (blob) => {
        const formData = new FormData();
        formData.append('avatar', blob, 'avatar.jpg');

        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        try {
            const response = await fetch('/api/users/avatar', {
                method: 'POST',
                headers: {
                    [header]: token
                },
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                // Update avatar image
                document.getElementById(avatarImageId).src = result.avatarUrl + '?t=' + Date.now();
                showToast('Avatar updated successfully!', 'success');
                closeAvatarUpload();
            } else {
                alert(result.message || 'Failed to upload avatar');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Failed to upload avatar');
        }
    }, 'image/jpeg', 0.9);
}

/**
 * Initialize drag & drop for avatar upload
 */
function initAvatarDragDrop() {
    const dropZone = document.getElementById('avatarDropZone');
    if (dropZone) {
        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('border-accent');
        });

        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('border-accent');
        });

        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('border-accent');
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                document.getElementById('avatarInput').files = files;
                handleAvatarSelect({ target: { files: files } });
            }
        });
    }
}

// Initialize on load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAvatarDragDrop);
} else {
    initAvatarDragDrop();
}
