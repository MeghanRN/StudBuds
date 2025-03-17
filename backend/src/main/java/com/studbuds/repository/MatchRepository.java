package com.studbuds.repository;

import com.studbuds.model.Match;
import com.studbuds.model.User;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for managing Match entities.
 * Extends JpaRepository to provide CRUD operations for Match objects.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    /**
     * Checks if a match already exists between two users.
     * This ensures that duplicate matches are not created.
     *
     * @param user1 The first user in the potential match.
     * @param user2 The second user in the potential match.
     * @return An Optional containing the Match if it exists, otherwise empty.
     */
    @Query("SELECT m FROM Match m " +
           "WHERE (m.user1 = :user1 AND m.user2 = :user2) " +
           "OR (m.user1 = :user2 AND m.user2 = :user1)")
    Optional<Match> findExistingMatch(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Retrieves all matches where the given user is involved.
     * This includes cases where the user is either user1 or user2 in the match.
     *
     * @param user1 The user to search for in user1 field.
     * @param user2 The user to search for in user2 field.
     * @return A list of matches where the user is involved.
     */
    List<Match> findByUser1OrUser2(User user1, User user2);
    
    /**
     * Alternative way to find all matches where the given user is involved.
     * This method uses a custom query and is functionally equivalent to findByUser1OrUser2.
     *
     * @param user The user whose matches should be retrieved.
     * @return A list of matches where the user is either user1 or user2.
     */
    @Query("SELECT m FROM Match m WHERE m.user1 = :user OR m.user2 = :user")
    List<Match> findAllByUser(@Param("user") User user);
}
