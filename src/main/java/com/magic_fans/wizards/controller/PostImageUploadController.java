package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostImageUploadController {

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/posts/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadPostImage(
            @RequestParam("image") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find user
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate file is not empty
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select an image file");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
                response.put("success", false);
                response.put("message", "Invalid file format. Allowed: JPEG, PNG, WebP, GIF");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("success", false);
                response.put("message", "File size exceeds 5MB limit. Please choose a smaller image.");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate it's actually an image
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    response.put("success", false);
                    response.put("message", "File is not a valid image");
                    return ResponseEntity.badRequest().body(response);
                }
            } catch (IOException e) {
                response.put("success", false);
                response.put("message", "Failed to read image file");
                return ResponseEntity.badRequest().body(response);
            }

            // Create directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = user.getId() + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return image URL
            String imageUrl = "/uploads/posts/" + fileName;
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");

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
}
