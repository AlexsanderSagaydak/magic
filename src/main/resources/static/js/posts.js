/**
 * Post management functionality
 */

let selectedPostImageFile = null;

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];

function handlePostImageSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
        alert('Invalid file format. Allowed formats: JPEG, PNG, WebP, GIF');
        clearPostImage();
        return;
    }

    // Validate file size
    if (file.size > MAX_FILE_SIZE) {
        const sizeMB = (file.size / 1024 / 1024).toFixed(2);
        alert(`File size (${sizeMB}MB) exceeds 5MB limit. Please choose a smaller image.`);
        clearPostImage();
        return;
    }

    selectedPostImageFile = file;

    // Show preview
    const reader = new FileReader();
    reader.onload = (e) => {
        const preview = document.getElementById('postImagePreview');
        const previewImg = document.getElementById('postImagePreviewImg');
        const previewInfo = document.getElementById('postImageInfo');

        previewImg.src = e.target.result;
        previewInfo.textContent = `${file.name} (${(file.size / 1024 / 1024).toFixed(2)}MB)`;
        preview.classList.remove('hidden');

        // Clear URL input
        document.getElementById('postImageUrl').value = '';
    };
    reader.readAsDataURL(file);
}

function clearPostImage() {
    selectedPostImageFile = null;
    document.getElementById('postImageFile').value = '';
    document.getElementById('postImageUrl').value = '';
    document.getElementById('postImagePreview').classList.add('hidden');
    document.getElementById('postImagePreviewImg').src = '';
    document.getElementById('postImageInfo').textContent = '';
}

async function createPost() {
    const content = document.getElementById('postContent').value.trim();
    const imageUrl = document.getElementById('postImageUrl').value.trim();

    if (!content) {
        alert('Please enter some content');
        return;
    }

    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    try {
        let finalImageUrl = imageUrl;

        // Upload file if selected
        if (selectedPostImageFile) {
            const formData = new FormData();
            formData.append('image', selectedPostImageFile);

            const uploadResponse = await fetch('/api/posts/upload-image', {
                method: 'POST',
                headers: {
                    [header]: token
                },
                body: formData
            });

            const uploadResult = await uploadResponse.json();

            if (!uploadResult.success) {
                alert(uploadResult.message || 'Failed to upload image');
                return;
            }

            finalImageUrl = uploadResult.imageUrl;
        }

        // Create post with image URL
        const payload = { content: content };
        if (finalImageUrl) {
            payload.imageUrl = finalImageUrl;
        }

        const response = await fetch('/posts/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (result.success) {
            // Save posts tab as active before reload
            localStorage.setItem('activeTab', 'posts');
            localStorage.setItem('postActionReload', 'true');
            location.reload();
        } else {
            alert(result.message || 'Error creating post');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error creating post');
    }
}

function deletePost(button) {
    if (!confirm('Are you sure you want to delete this post?')) {
        return;
    }

    const postId = button.getAttribute('data-post-id');
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/posts/${postId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        }
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            // Save posts tab as active before reload
            localStorage.setItem('activeTab', 'posts');
            localStorage.setItem('postActionReload', 'true');
            location.reload();
        } else {
            alert(result.message || 'Error deleting post');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error deleting post');
    });
}
