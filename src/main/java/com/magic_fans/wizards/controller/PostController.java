package com.magic_fans.wizards.controller;

import com.magic_fans.wizards.model.Post;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.service.PostLikeService;
import com.magic_fans.wizards.service.PostService;
import com.magic_fans.wizards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Not authenticated");
                return ResponseEntity.ok(response);
            }

            String username = auth.getName();
            var userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();

            // Only wizards can create posts
            if (!"wizard".equals(user.getRole())) {
                response.put("success", false);
                response.put("message", "Only wizards can create posts");
                return ResponseEntity.ok(response);
            }

            String content = payload.get("content");
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Content cannot be empty");
                return ResponseEntity.ok(response);
            }

            String imageUrl = payload.get("imageUrl");
            Post post;
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                post = postService.createPost(user, content, imageUrl);
            } else {
                post = postService.createPost(user, content);
            }

            response.put("success", true);
            response.put("postId", post.getId());
            response.put("message", "Post created successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Not authenticated");
                return ResponseEntity.ok(response);
            }

            String username = auth.getName();
            var userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            postLikeService.likePost(postId, user);

            var postOpt = postService.getPostById(postId);
            int likesCount = postOpt.map(Post::getLikesCount).orElse(0);

            response.put("success", true);
            response.put("likesCount", likesCount);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error liking post");
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlikePost(@PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Not authenticated");
                return ResponseEntity.ok(response);
            }

            String username = auth.getName();
            var userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            postLikeService.unlikePost(postId, user.getId());

            var postOpt = postService.getPostById(postId);
            int likesCount = postOpt.map(Post::getLikesCount).orElse(0);

            response.put("success", true);
            response.put("likesCount", likesCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error unliking post");
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                response.put("success", false);
                response.put("message", "Not authenticated");
                return ResponseEntity.ok(response);
            }

            String username = auth.getName();
            var userOpt = userService.getUserByUsername(username);

            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();

            // Check if post belongs to user
            var postOpt = postService.getPostById(postId);
            if (postOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Post not found");
                return ResponseEntity.ok(response);
            }

            Post post = postOpt.get();
            if (post.getAuthor().getId() != user.getId()) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return ResponseEntity.ok(response);
            }

            postService.deletePost(postId);

            response.put("success", true);
            response.put("message", "Post deleted successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting post");
        }

        return ResponseEntity.ok(response);
    }
}
