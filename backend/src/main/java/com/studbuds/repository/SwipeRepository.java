package com.studbuds.repository;

import com.studbuds.model.Swipe;
import com.studbuds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    // Find all pending swipes initiated by the given user.
    List<Swipe> findByFromUser(User fromUser); // ⚠️ we should distinguish between pending, accepted, or rejected swipes.
}
