package com.magic_fans.wizards.repository;

import com.magic_fans.wizards.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthorIdOrderByCreatedAtDesc(int authorId);

    long countByAuthorId(int authorId);

    List<Post> findAllByOrderByCreatedAtDesc();
}
