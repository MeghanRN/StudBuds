package com.studbuds.repository;

import com.studbuds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // ⚠️ we should add existsByEmail(string Email) for faster email existence checks, rather than fetching the entire user object.
    // ⚠️ we should also make sure emails aren't case sensitive
    // ⚠️ should we also add a method to search users by their name? 
}
