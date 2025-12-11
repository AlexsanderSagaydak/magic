package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.Post;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public Post createPost(User author, String content) {
        Post post = new Post(author, content);
        return postRepository.save(post);
    }

    @Transactional
    public Post createPost(User author, String content, String imageUrl) {
        Post post = new Post(author, content);
        post.setImageUrl(imageUrl);
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long postId, String content) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setContent(content);
            return postRepository.save(post);
        }
        throw new IllegalArgumentException("Post not found");
    }

    @Transactional
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
    }

    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    public List<Post> getPostsByAuthor(int authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public long getPostsCountByAuthor(int authorId) {
        return postRepository.countByAuthorId(authorId);
    }
}
