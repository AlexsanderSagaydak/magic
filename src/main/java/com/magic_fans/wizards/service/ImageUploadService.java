package com.magic_fans.wizards.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Service for handling image uploads to S3
 *
 * IMPORTANT: This is a template/example service showing how to handle image uploads.
 * To actually use S3, you need to:
 * 1. Add AWS SDK dependency to pom.xml:
 *    <dependency>
 *      <groupId>software.amazon.awssdk</groupId>
 *      <artifactId>s3</artifactId>
 *      <version>2.20.0</version>
 *    </dependency>
 * 2. Configure AWS credentials in application.yaml
 * 3. Uncomment the S3 upload code below
 */
@Service
public class ImageUploadService {

    // ============= CONFIGURATION =============

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long RECOMMENDED_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_FORMATS = Set.of(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp"
    );

    // Размеры для разных версий изображений
    private static final int THUMBNAIL_SIZE = 150;      // 150x150
    private static final int MEDIUM_WIDTH = 800;        // 800px ширина
    private static final int LARGE_WIDTH = 1200;        // 1200px ширина
    private static final int MAX_WIDTH = 2048;          // Максимум 2048px
    private static final int MAX_HEIGHT = 2048;

    // ============= VALIDATION =============

    /**
     * Валидация загружаемого файла
     */
    public void validateImage(MultipartFile file) throws IllegalArgumentException {
        // Проверка размера
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d MB",
                    MAX_FILE_SIZE / 1024 / 1024)
            );
        }

        // Проверка формата
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FORMATS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid file format. Allowed: JPEG, PNG, WebP"
            );
        }

        // Проверка что файл действительно изображение
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("File is not a valid image");
            }

            // Проверка разрешения
            if (image.getWidth() > MAX_WIDTH || image.getHeight() > MAX_HEIGHT) {
                throw new IllegalArgumentException(
                    String.format("Image dimensions exceed maximum allowed size of %dx%d",
                        MAX_WIDTH, MAX_HEIGHT)
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read image file", e);
        }
    }

    // ============= IMAGE PROCESSING =============

    /**
     * Создание миниатюры (квадратная, обрезанная по центру)
     */
    public byte[] createThumbnail(MultipartFile file) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());

        // Вычисляем размер для обрезки (квадрат по центру)
        int size = Math.min(original.getWidth(), original.getHeight());
        int x = (original.getWidth() - size) / 2;
        int y = (original.getHeight() - size) / 2;

        // Обрезаем по центру
        BufferedImage cropped = original.getSubimage(x, y, size, size);

        // Масштабируем до нужного размера
        return resizeImage(cropped, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
    }

    /**
     * Создание средней версии (сохраняем пропорции)
     */
    public byte[] createMediumVersion(MultipartFile file) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        return resizeImageKeepAspectRatio(original, MEDIUM_WIDTH);
    }

    /**
     * Создание большой версии (сохраняем пропорции)
     */
    public byte[] createLargeVersion(MultipartFile file) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        return resizeImageKeepAspectRatio(original, LARGE_WIDTH);
    }

    /**
     * Масштабирование изображения до указанных размеров
     */
    private byte[] resizeImage(BufferedImage original, int width, int height) throws IOException {
        Image scaled = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = buffered.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(scaled, 0, 0, null);
        g.dispose();

        return imageToBytes(buffered);
    }

    /**
     * Масштабирование с сохранением пропорций
     */
    private byte[] resizeImageKeepAspectRatio(BufferedImage original, int maxWidth) throws IOException {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // Если изображение меньше maxWidth, не масштабируем
        if (originalWidth <= maxWidth) {
            return imageToBytes(original);
        }

        // Вычисляем новые размеры с сохранением пропорций
        int newWidth = maxWidth;
        int newHeight = (originalHeight * maxWidth) / originalWidth;

        return resizeImage(original, newWidth, newHeight);
    }

    /**
     * Конвертация BufferedImage в byte array
     */
    private byte[] imageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    // ============= S3 UPLOAD (EXAMPLE) =============

    /**
     * Загрузка изображения в S3
     *
     * Это пример кода. Чтобы использовать:
     * 1. Раскомментируйте код ниже
     * 2. Добавьте AWS SDK в pom.xml
     * 3. Настройте credentials в application.yaml
     */
    /*
    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadToS3(MultipartFile file, Long userId, String imageType) throws IOException {
        // Валидация
        validateImage(file);

        // Генерируем уникальное имя файла
        String fileName = String.format("posts/%d/%s_%s.jpg",
            userId,
            imageType,
            UUID.randomUUID().toString()
        );

        // Получаем байты изображения в зависимости от типа
        byte[] imageBytes;
        switch (imageType) {
            case "thumbnail":
                imageBytes = createThumbnail(file);
                break;
            case "medium":
                imageBytes = createMediumVersion(file);
                break;
            case "large":
                imageBytes = createLargeVersion(file);
                break;
            default:
                imageBytes = file.getBytes();
        }

        // Загружаем в S3
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType("image/jpeg")
            .contentLength((long) imageBytes.length)
            .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));

        // Возвращаем публичный URL
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
    }
    */
}
