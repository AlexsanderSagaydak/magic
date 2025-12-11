package com.magic_fans.wizards.service;

import com.magic_fans.wizards.model.Post;
import com.magic_fans.wizards.model.PostLike;
import com.magic_fans.wizards.model.User;
import com.magic_fans.wizards.repository.PostLikeRepository;
import com.magic_fans.wizards.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostLikeService {

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostRepository postRepository;

    @Transactional
    public PostLike likePost(Long postId, User user) {
        // Check if already liked
        if (postLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            throw new IllegalStateException("Post already liked");
        }

        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new IllegalArgumentException("Post not found");
        }

        Post post = postOpt.get();
        PostLike like = new PostLike(post, user);

        // Increment likes count
        post.incrementLikes();
        postRepository.save(post);

        return postLikeRepository.save(like);
    }

    @Transactional
    public void unlikePost(Long postId, int userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.decrementLikes();
            postRepository.save(post);
        }

        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    public boolean isLiked(Long postId, int userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    public List<PostLike> getPostLikes(Long postId) {
        return postLikeRepository.findByPostIdOrderByLikedAtDesc(postId);
    }

    public long getPostLikesCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public List<PostLike> getAllLikesByAuthor(int authorId) {
        return postLikeRepository.findAllLikesByAuthorId(authorId);
    }

    public long getTotalLikesByAuthor(int authorId) {
        return postLikeRepository.countLikesByAuthorId(authorId);
    }
}
