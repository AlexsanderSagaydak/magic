# Пример загрузки изображений с Drag & Drop

## HTML для формы создания поста

```html
<!-- CREATE POST FORM с загрузкой файлов -->
<div class="bg-dark p-4 rounded-xl border border-dark-tertiary mb-6">
    <textarea id="postContent"
              class="w-full bg-dark-tertiary border border-dark-tertiary rounded px-3 py-2 text-sm text-gray-100 resize-none"
              rows="3"
              placeholder="What's on your mind?"></textarea>

    <!-- IMAGE UPLOAD AREA -->
    <div class="mt-3">
        <div id="dropZone"
             class="border-2 border-dashed border-dark-tertiary rounded-lg p-6 text-center cursor-pointer hover:border-accent transition">
            <input type="file"
                   id="imageInput"
                   accept="image/jpeg,image/png,image/webp"
                   class="hidden"
                   onchange="handleFileSelect(event)">

            <div id="dropZoneContent">
                <svg class="w-12 h-12 mx-auto text-gray-400 mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                </svg>
                <p class="text-gray-400 text-sm">
                    Drag & drop image here or <span class="text-accent">click to browse</span>
                </p>
                <p class="text-gray-500 text-xs mt-2">
                    JPEG, PNG, WebP • Max 10MB • Recommended: 1200x900
                </p>
            </div>

            <!-- Preview Area (hidden by default) -->
            <div id="imagePreview" class="hidden">
                <img id="previewImage" src="" alt="Preview" class="max-w-full max-h-64 mx-auto rounded-lg">
                <button onclick="clearImage()"
                        class="mt-3 px-4 py-2 bg-red-600 hover:bg-red-700 rounded text-sm transition">
                    Remove Image
                </button>
            </div>
        </div>

        <!-- Upload Progress -->
        <div id="uploadProgress" class="hidden mt-3">
            <div class="w-full bg-dark-tertiary rounded-full h-2">
                <div id="progressBar" class="bg-accent h-2 rounded-full transition-all" style="width: 0%"></div>
            </div>
            <p id="progressText" class="text-xs text-gray-400 mt-1">Uploading...</p>
        </div>
    </div>

    <div class="flex justify-end mt-3">
        <button onclick="createPost()"
                class="px-6 py-2 bg-accent hover:bg-blue-600 rounded transition text-sm font-medium">
            Post
        </button>
    </div>
</div>
```

## JavaScript для обработки загрузки

```javascript
let selectedFile = null;
let uploadedImageUrl = null;

// Инициализация drag & drop
const dropZone = document.getElementById('dropZone');

dropZone.addEventListener('click', () => {
    document.getElementById('imageInput').click();
});

dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('border-accent', 'bg-dark-tertiary/20');
});

dropZone.addEventListener('dragleave', () => {
    dropZone.classList.remove('border-accent', 'bg-dark-tertiary/20');
});

dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('border-accent', 'bg-dark-tertiary/20');

    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleFile(files[0]);
    }
});

function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) {
        handleFile(file);
    }
}

function handleFile(file) {
    // Валидация формата
    const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
        alert('Invalid file format. Please use JPEG, PNG, or WebP');
        return;
    }

    // Валидация размера (10MB)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
        alert('File is too large. Maximum size is 10MB');
        return;
    }

    selectedFile = file;

    // Показываем preview
    const reader = new FileReader();
    reader.onload = (e) => {
        document.getElementById('previewImage').src = e.target.result;
        document.getElementById('dropZoneContent').classList.add('hidden');
        document.getElementById('imagePreview').classList.remove('hidden');
    };
    reader.readAsDataURL(file);
}

function clearImage() {
    selectedFile = null;
    uploadedImageUrl = null;
    document.getElementById('imageInput').value = '';
    document.getElementById('dropZoneContent').classList.remove('hidden');
    document.getElementById('imagePreview').classList.add('hidden');
}

async function uploadImage() {
    if (!selectedFile) return null;

    const formData = new FormData();
    formData.append('image', selectedFile);

    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Показываем прогресс
    document.getElementById('uploadProgress').classList.remove('hidden');

    try {
        const xhr = new XMLHttpRequest();

        // Отслеживаем прогресс
        xhr.upload.addEventListener('progress', (e) => {
            if (e.lengthComputable) {
                const percent = (e.loaded / e.total) * 100;
                document.getElementById('progressBar').style.width = percent + '%';
                document.getElementById('progressText').textContent =
                    `Uploading... ${Math.round(percent)}%`;
            }
        });

        // Промис для XMLHttpRequest
        const uploadPromise = new Promise((resolve, reject) => {
            xhr.onload = () => {
                if (xhr.status === 200) {
                    resolve(JSON.parse(xhr.responseText));
                } else {
                    reject(new Error('Upload failed'));
                }
            };
            xhr.onerror = () => reject(new Error('Network error'));
        });

        xhr.open('POST', '/api/images/upload');
        xhr.setRequestHeader(header, token);
        xhr.send(formData);

        const result = await uploadPromise;

        if (result.success) {
            // Сохраняем URL загруженной картинки
            uploadedImageUrl = result.urls.medium; // или large для постов
            return uploadedImageUrl;
        } else {
            throw new Error(result.message);
        }

    } catch (error) {
        console.error('Upload error:', error);
        alert('Failed to upload image: ' + error.message);
        return null;
    } finally {
        // Скрываем прогресс
        document.getElementById('uploadProgress').classList.add('hidden');
        document.getElementById('progressBar').style.width = '0%';
    }
}

async function createPost() {
    const content = document.getElementById('postContent').value.trim();

    if (!content) {
        alert('Please enter some content');
        return;
    }

    // Если есть выбранный файл, сначала загружаем его
    if (selectedFile && !uploadedImageUrl) {
        uploadedImageUrl = await uploadImage();
        if (!uploadedImageUrl) {
            return; // Загрузка не удалась
        }
    }

    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    const payload = { content: content };
    if (uploadedImageUrl) {
        payload.imageUrl = uploadedImageUrl;
    }

    fetch('/posts/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify(payload)
    })
    .then(response => response.json())
    .then(result => {
        if (result.success) {
            location.reload();
        } else {
            alert(result.message || 'Error creating post');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error creating post');
    });
}
```

## CSS классы для масштабирования изображений

### Текущая реализация (в постах):
```css
/* Основное изображение в посте */
.w-full          /* ширина 100% контейнера */
.max-w-2xl       /* максимум 672px (Tailwind) */
.h-auto          /* высота авто (сохраняем пропорции) */
.max-h-96        /* максимум 384px */
.object-cover    /* обрезает, если не влезает */

/* Альтернатива с сохранением пропорций */
.object-contain  /* вписывает целиком, не обрезает */
```

### Объяснение:

1. **`object-cover`** - обрезает края, чтобы заполнить всю область
   - Плюсы: красивый единообразный вид
   - Минусы: может обрезать важные части

2. **`object-contain`** - вписывает целиком
   - Плюсы: показывает все изображение
   - Минусы: могут быть черные полосы по краям

### Рекомендации для разных случаев:

```html
<!-- Для аватаров/миниатюр - квадрат с обрезкой -->
<img class="w-16 h-16 object-cover rounded-full">

<!-- Для постов - ограничиваем высоту, ширина 100% -->
<img class="w-full max-w-2xl h-auto max-h-96 object-cover rounded-lg">

<!-- Для модального окна - показываем все -->
<img class="max-w-full max-h-screen object-contain">

<!-- Для галереи - фиксированная высота -->
<img class="w-full h-64 object-cover">
```

## Итоговые требования для S3:

### Валидация на клиенте:
```javascript
- Формат: JPEG, PNG, WebP
- Размер файла: до 10 MB
- Рекомендуемый размер: до 5 MB
- Показывать предупреждение, если файл > 5 MB
```

### Обработка на сервере:
```java
- Создать 3 версии:
  1. thumbnail: 150x150 (квадрат, для превью)
  2. medium: 800px ширина (для ленты)
  3. large: 1200px ширина (для детального просмотра)

- Оригинал не сохранять (экономия места)
- Конвертировать все в JPEG (лучшее сжатие)
- Качество: 85% (хороший баланс размер/качество)
```

### Структура в S3:
```
bucket-name/
  posts/
    {userId}/
      thumbnail_{uuid}.jpg
      medium_{uuid}.jpg
      large_{uuid}.jpg
  avatars/
    {userId}/
      avatar_{uuid}.jpg
```

### В базе данных хранить:
```java
- imageUrl (medium) - для отображения в постах
- thumbnailUrl - для превью
- largeUrl - для модального окна (опционально)
```
