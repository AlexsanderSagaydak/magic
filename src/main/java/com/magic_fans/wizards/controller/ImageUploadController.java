package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling image uploads
 *
 * EXAMPLE ENDPOINTS - раскомментируйте когда подключите S3
 */
@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    @Autowired
    private ImageUploadService imageUploadService;

    /**
     * Загрузка изображения для поста
     *
     * Пример использования:
     * const formData = new FormData();
     * formData.append('image', fileInput.files[0]);
     *
     * fetch('/api/images/upload', {
     *   method: 'POST',
     *   body: formData,
     *   headers: {
     *     'X-CSRF-TOKEN': csrfToken
     *   }
     * })
     */
    /*
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
        @RequestParam("image") MultipartFile file,
        @RequestParam(value = "userId", required = false) Long userId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Валидация
            imageUploadService.validateImage(file);

            // Загружаем все версии
            String thumbnailUrl = imageUploadService.uploadToS3(file, userId, "thumbnail");
            String mediumUrl = imageUploadService.uploadToS3(file, userId, "medium");
            String largeUrl = imageUploadService.uploadToS3(file, userId, "large");

            response.put("success", true);
            response.put("urls", Map.of(
                "thumbnail", thumbnailUrl,
                "medium", mediumUrl,
                "large", largeUrl
            ));
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    */

    /**
     * Предварительная проверка файла без загрузки
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateImage(
        @RequestParam("image") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            imageUploadService.validateImage(file);

            response.put("success", true);
            response.put("message", "Image is valid");
            response.put("fileSize", file.getSize());
            response.put("fileName", file.getOriginalFilename());
            response.put("contentType", file.getContentType());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
