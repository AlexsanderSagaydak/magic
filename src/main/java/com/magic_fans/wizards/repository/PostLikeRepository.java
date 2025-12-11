package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, int userId);

    boolean existsByPostIdAndUserId(Long postId, int userId);

    long countByPostId(Long postId);

    List<PostLike> findByPostIdOrderByLikedAtDesc(Long postId);

    void deleteByPostIdAndUserId(Long postId, int userId);

    // Get all likes for posts by specific author (wizard)
    @Query("SELECT pl FROM PostLike pl WHERE pl.post.author.id = :authorId ORDER BY pl.likedAt DESC")
    List<PostLike> findAllLikesByAuthorId(@Param("authorId") int authorId);

    // Count total likes for all posts by author
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.author.id = :authorId")
    long countLikesByAuthorId(@Param("authorId") int authorId);
}
