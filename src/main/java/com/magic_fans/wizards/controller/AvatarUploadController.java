package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class AvatarUploadController {

    @Autowired
    private UserRepository userRepository;

    // Локальное хранилище (позже можно заменить на S3)
    // Сохраняем в корень проекта, чтобы Spring мог отдавать файлы
    private static final String UPLOAD_DIR = "uploads/avatars/";

    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Находим пользователя
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Валидация файла
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Проверка типа файла
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Invalid file type. Only images are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Проверка размера (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "File size exceeds 5MB limit");
                return ResponseEntity.badRequest().body(response);
            }

            // Создаем директорию если не существует
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Генерируем уникальное имя файла
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = user.getId() + "_" + UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Сохраняем файл
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Удаляем старый аватар если есть
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()
                && !user.getAvatarUrl().equals("/images/default-avatar.svg")) {
                deleteOldAvatar(user.getAvatarUrl());
            }

            // Обновляем URL аватара в базе
            String avatarUrl = "/uploads/avatars/" + fileName;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            response.put("success", true);
            response.put("avatarUrl", avatarUrl);
            response.put("message", "Avatar uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return ".jpg";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? ".jpg" : filename.substring(dotIndex);
    }

    private void deleteOldAvatar(String avatarUrl) {
        try {
            String fileName = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
            Path oldFilePath = Paths.get(UPLOAD_DIR + fileName);
            if (Files.exists(oldFilePath)) {
                Files.delete(oldFilePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete old avatar: " + e.getMessage());
        }
    }
}
