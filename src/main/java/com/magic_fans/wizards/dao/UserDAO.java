package com.magic_fans.wizards.dao;

import com.magic_fans.wizards.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    void save(User user);
    void update(User user);
    void delete(int userId);
    Optional<User> findById(int userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}