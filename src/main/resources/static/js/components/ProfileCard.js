/**
 * Profile Card Component
 * Creates a wizard profile card for the feed
 */

export function createProfileCard(profile) {
    const card = document.createElement('div');
    card.className =
        'bg-dark-secondary border border-dark-tertiary rounded-xl overflow-hidden hover:border-accent hover:shadow-lg cursor-pointer transition flex flex-col';

    const onlineClass = profile.online ? 'bg-green-500' : 'bg-gray-500';

    card.innerHTML = `
        <div class="relative w-full h-64 bg-gradient-to-br from-accent to-blue-700 overflow-hidden">
            <img src="${profile.profileImageUrl}" class="w-full h-full object-cover"
                onerror="this.style.display='none'; this.parentElement.style.background='linear-gradient(to right, #3b82f6, #1e40af)'">
            <div class="absolute top-2 right-2 bg-dark-secondary/80 px-2 py-1 rounded text-xs">🎥 Video</div>
            <button data-wizard-id="${profile.id}" class="favorite-btn absolute top-2 left-2 bg-dark-secondary/90 hover:bg-dark-secondary p-2 rounded-full transition-all hover:scale-110 z-10">
                <svg class="w-5 h-5 text-red-500 favorite-icon transition-all" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
                </svg>
            </button>
        </div>

        <div class="-mt-16 px-4 pb-4">
            <div class="bg-dark-secondary/95 backdrop-blur-sm rounded-3xl p-4 shadow-xl">
                <div class="flex items-start gap-3 mb-3">
                    <div class="w-16 h-16 rounded-full overflow-hidden border-2 border-accent bg-dark-tertiary">
                        <img src="${profile.avatarUrl}" class="w-full h-full object-cover"
                            onerror="this.style.display='none'; this.parentElement.style.background='linear-gradient(135deg, #3b82f6, #1e40af)'">
                    </div>

                    <div class="flex-1">
                        <div class="flex items-center gap-2 mb-1">
                            <div class="font-bold text-sm">${profile.firstName} ${profile.lastName}</div>
                            <span class="w-2 h-2 rounded-full ${onlineClass}"></span>
                        </div>
                        <div class="text-xs text-gray-400 mb-1">@${profile.username}</div>
                        <div class="inline-block bg-dark-tertiary border border-dark-tertiary hover:border-accent px-2 py-1 rounded text-xs">
                            ${profile.specialization}
                        </div>
                    </div>

                    <div>
                        <svg class="w-5 h-5 text-accent" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                        </svg>
                    </div>
                </div>

                ${profile.skills && profile.skills.length > 0 ? `
                    <div class="flex flex-wrap gap-1 mt-2">
                        ${profile.skills.slice(0, profile.skills.length > 10 ? 7 : profile.skills.length).map(skill =>
                            `<span class="inline-block bg-dark-tertiary border border-accent px-3 py-1 rounded-full text-xs">${skill}</span>`
                        ).join('')}
                        ${profile.skills.length > 10 ? '<span class="text-xs text-gray-400">...</span>' : ''}
                    </div>
                ` : ''}

                ${profile.aboutMe && profile.aboutMe.trim() ? `
                    <p class="text-sm text-gray-300 mt-3 line-clamp-2">${profile.aboutMe}</p>
                ` : ''}
            </div>
        </div>
    `;

    // Add click handler for the card (navigate to profile)
    card.querySelector('.rounded-3xl').onclick = () => window.location.href = `/users/${profile.id}`;

    // Add click handler for favorite button (prevent card navigation)
    const favoriteBtn = card.querySelector('.favorite-btn');
    favoriteBtn.onclick = (e) => {
        e.stopPropagation();
        toggleFavorite(e, profile.id);
    };

    // Check and set initial favorite status
    checkFavoriteStatus(profile.id, card);

    return card;
}

/**
 * Checks if a wizard is in the user's favorites and updates the UI accordingly.
 */
async function checkFavoriteStatus(wizardId, cardElement) {
    try {
        const response = await fetch(`/favorites/check/${wizardId}`);

        if (!response.ok) {
            console.error(`Failed to check favorite status: ${response.status}`);
            return;
        }

        const data = await response.json();
        const icon = cardElement.querySelector('.favorite-icon');

        if (data.isFavorite) {
            icon.setAttribute('fill', 'currentColor');
            icon.style.transform = 'scale(1.1)';
        } else {
            icon.removeAttribute('fill');
            icon.style.transform = 'scale(1)';
        }
    } catch (error) {
        console.error('Error checking favorite status:', error);
    }
}

/**
 * Toggles the favorite status of a wizard (add or remove from favorites).
 */
async function toggleFavorite(event, wizardId) {
    event.stopPropagation();
    event.preventDefault();

    const btn = event.target.closest('.favorite-btn');
    const icon = btn.querySelector('.favorite-icon');
    const isFilled = icon.getAttribute('fill') === 'currentColor';
    const url = isFilled ? `/favorites/remove/${wizardId}` : `/favorites/add/${wizardId}`;

    // Get CSRF token from meta tags
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // Optimistic UI update
    btn.disabled = true;
    btn.style.opacity = '0.6';

    try {
        const headers = {
            'Content-Type': 'application/json'
        };

        // Add CSRF token if available
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: headers
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        // Update UI based on response
        if (data.success) {
            if (data.isFavorite) {
                icon.setAttribute('fill', 'currentColor');
                icon.style.transform = 'scale(1.1)';
                showToast('Added to favorites!', 'success');
            } else {
                icon.removeAttribute('fill');
                icon.style.transform = 'scale(1)';
                showToast('Removed from favorites', 'info');
            }

            // Update favorite count in sidebar
            loadFavoriteCount();
        } else {
            throw new Error(data.message || 'Operation failed');
        }

    } catch (error) {
        console.error('Error toggling favorite:', error);
        showToast('Failed to update favorite. Please try again.', 'error');

        // Revert UI on error
        if (isFilled) {
            icon.setAttribute('fill', 'currentColor');
        } else {
            icon.removeAttribute('fill');
        }
    } finally {
        btn.disabled = false;
        btn.style.opacity = '1';
    }
}
