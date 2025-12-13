# Система аватаров

## Обзор

Реализована профессиональная система загрузки и управления аватарами пользователей с использованием **Cropper.js** для обрезки изображений.

## Основные функции

### ✅ Что реализовано:

1. **SVG заглушка** - `/images/default-avatar.svg`
   - Автоматически отображается для пользователей без аватара
   - Простая иконка пользователя в серых тонах

2. **Поле avatarUrl в User**
   - Хранится в БД (колонка `avatar_url`)
   - Null-безопасное отображение

3. **Модальное окно с Cropper.js**
   - Drag & drop загрузка
   - Обрезка в квадрат (1:1)
   - Поворот на 90° влево/вправо
   - Preview в реальном времени
   - Zoom и перемещение

4. **Валидация**
   - Форматы: JPEG, PNG, WebP
   - Максимальный размер: 5 MB
   - Проверка на клиенте и сервере

5. **Локальное хранилище**
   - Путь: `uploads/avatars/` (в корне проекта)
   - Формат имени: `{userId}_{uuid}.jpg`
   - Автоматическое удаление старого аватара
   - Отдаётся через `/uploads/**` URL

6. **Замена dicebear**
   - Убраны все вызовы к dicebear API
   - Везде используется avatarUrl или default-avatar.svg

## Требования к аватару

### Рекомендуемые параметры:

```
Размер файла: до 5 MB
Формат: JPEG, PNG, WebP
Соотношение: 1:1 (квадрат)
Разрешение: 400x400 px (после обрезки)
Качество: 90% JPEG
```

## Как использовать

### Для пользователя:

1. Зайти в "My Profile"
2. Нажать кнопку камеры на аватаре
3. Выбрать файл или перетащить
4. Обрезать и повернуть при необходимости
5. Нажать "Upload Avatar"

### Для разработчика:

#### Отображение аватара в HTML:

```html
<!-- Thymeleaf -->
<img th:src="${user.avatarUrl != null ? user.avatarUrl : '/images/default-avatar.svg'}"
     class="w-32 h-32 rounded-full object-cover"
     onerror="this.src='/images/default-avatar.svg'">
```

#### Отображение аватара в Java:

```java
// В DTO
dto.setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "/images/default-avatar.svg");
```

## API Endpoint

### POST `/api/users/avatar`

Загрузка аватара для текущего пользователя.

**Request:**
```
Content-Type: multipart/form-data
Parameter: avatar (file)
Headers: X-CSRF-TOKEN
```

**Response:**
```json
{
  "success": true,
  "avatarUrl": "/uploads/avatars/1_uuid.jpg",
  "message": "Avatar uploaded successfully"
}
```

**Ошибки:**
- 400: Invalid file type / File too large
- 500: Server error

## Структура файлов

```
wizards/
├── uploads/                         # Загруженные файлы (в .gitignore)
│   ├── .gitkeep                     # Для git
│   └── avatars/
│       ├── 1_abc123.jpg
│       └── 2_def456.jpg
├── src/main/
│   ├── java/.../config/
│   │   └── WebConfig.java           # Конфигурация для отдачи uploads/
│   ├── resources/
│   │   ├── static/images/
│   │   │   └── default-avatar.svg   # Заглушка
│   │   └── templates/
│   │       └── my-profile.html      # Модальное окно
└── .gitignore                       # uploads/ добавлено
```

## Cropper.js настройки

```javascript
new Cropper(img, {
    aspectRatio: 1,          // Квадрат 1:1
    viewMode: 2,             // Ограничение canvas размером контейнера
    dragMode: 'move',        # Перемещение изображения
    autoCropArea: 1,         // 100% области для обрезки
    cropBoxMovable: true,    // Можно двигать рамку
    cropBoxResizable: true,  // Можно менять размер рамки
});
```

## Переход на S3

Когда будете готовы перейти на S3, нужно:

### 1. Добавить зависимость в `pom.xml`:

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.0</version>
</dependency>
```

### 2. Настроить credentials в `application.yaml`:

```yaml
aws:
  s3:
    bucket-name: your-bucket-name
    region: us-east-1
  credentials:
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
```

### 3. Обновить `AvatarUploadController`:

```java
@Autowired
private S3Client s3Client;

@Value("${aws.s3.bucket-name}")
private String bucketName;

// Заменить Files.copy на:
String fileName = user.getId() + "_" + UUID.randomUUID().toString() + ".jpg";
PutObjectRequest putRequest = PutObjectRequest.builder()
    .bucket(bucketName)
    .key("avatars/" + fileName)
    .contentType("image/jpeg")
    .build();

s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
String avatarUrl = "https://" + bucketName + ".s3.amazonaws.com/avatars/" + fileName;
```

### 4. Настроить S3 Bucket:

```
- Создать bucket
- Настроить CORS
- Настроить public read access (или CloudFront)
- Настроить lifecycle rules для удаления старых файлов
```

## Лучшие практики

### ✅ Что делает система:

1. **Автоматическое удаление старого аватара** при загрузке нового
2. **Валидация на клиенте и сервере**
3. **Обрезка до квадрата** для единообразия
4. **Оптимизация размера** (400x400, 90% quality)
5. **Fallback на default** при ошибке загрузки
6. **CSRF защита**

### ⚠️ Что можно улучшить:

1. **CDN** - для быстрой загрузки аватаров
2. **Thumbnail** - создать маленькую версию 100x100
3. **Lazy loading** - для списков пользователей
4. **WebP conversion** - для меньшего размера
5. **Image optimization** - сжатие без потери качества
6. **Rate limiting** - ограничить частоту загрузки
7. **Virus scanning** - проверка файлов на вирусы

## CSS классы для аватаров

### Круглый аватар:

```html
<!-- Small (32px) -->
<img class="w-8 h-8 rounded-full object-cover">

<!-- Medium (64px) -->
<img class="w-16 h-16 rounded-full object-cover border-2 border-accent">

<!-- Large (128px) -->
<img class="w-32 h-32 rounded-full object-cover border-4 border-accent">
```

### С тенью:

```html
<img class="w-16 h-16 rounded-full object-cover shadow-lg hover:shadow-xl transition">
```

### С онлайн индикатором:

```html
<div class="relative inline-block">
    <img class="w-16 h-16 rounded-full object-cover">
    <span class="absolute bottom-0 right-0 w-4 h-4 bg-green-500 rounded-full border-2 border-dark"></span>
</div>
```

## Тестирование

### Тестовые изображения:

```
Маленькое: 100x100, <100KB
Среднее: 800x600, 1MB
Большое: 2000x2000, 4MB
Огромное: 5000x5000, 10MB (должно отклониться)

Форматы: JPEG, PNG, WebP
Неправильные: GIF, BMP, PDF
```

### Проверить:

- ✅ Загрузка нового аватара
- ✅ Замена существующего
- ✅ Поворот изображения
- ✅ Обрезка
- ✅ Отображение в профиле
- ✅ Отображение в постах
- ✅ Отображение в ленте
- ✅ Fallback на default
- ✅ Drag & drop
- ✅ Валидация формата
- ✅ Валидация размера

## Безопасность

### Реализовано:

- ✅ CSRF токен
- ✅ Валидация типа файла (MIME)
- ✅ Ограничение размера файла
- ✅ Проверка авторизации
- ✅ Уникальные имена файлов (UUID)
- ✅ Ограничение директории загрузки

### Рекомендуется добавить:

- ⚠️ Проверка содержимого файла (не только MIME)
- ⚠️ Virus scanning
- ⚠️ Rate limiting
- ⚠️ Content Security Policy headers
