package com.studbuds.repository;

import com.studbuds.model.Preference;
import com.studbuds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Preference entities.
 * Extends JpaRepository to provide CRUD operations.
 */
@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    /**
     * Finds the Preference entity associated with a specific User.
     *
     * @param user The user whose preferences are being retrieved.
     * @return An Optional containing the user's preferences if found.
     */
    Optional<Preference> findByUser(User user);

    /**
     * Retrieves a list of Preference entities for users with the same major and year,
     * excluding the current user (to prevent self-matching).
     *
     * @param major The major field of study to filter by.
     * @param year The academic year to filter by.
     * @param currentUserId The ID of the current user to exclude from results.
     * @return A list of matching Preference entities.
     */
    @Query("SELECT p FROM Preference p " +
           "WHERE p.user.major = :major " +  
           "AND p.user.year = :year " +  
           "AND p.user.id <> :currentUserId")
    List<Preference> findByMajorAndYear(@Param("major") String major, 
                                      @Param("year") int year, 
                                      @Param("currentUserId") Long currentUserId);

    /**
     * Finds preferences of users with the same major and year,
     * excluding a specific user by their ID.
     * 
     * NOTE: The `year` parameter is currently typed as `String`, which may be an error.
     *       Consider changing it to `int` for consistency.
     *
     * @param major The major field of study to match.
     * @param year The academic year to match.
     * @param userId The ID of the user to exclude from the results.
     * @return A list of Preference entities that match the criteria.
     */
    @Query("SELECT p FROM Preference p " +
           "WHERE p.user.major = :major " +
           "AND p.user.year = :year " +
           "AND p.user.id != :userId")
    List<Preference> findSimilarPreferences(@Param("major") String major, 
                                            @Param("year") String year,  // should this be int?
                                            @Param("userId") Long userId);
}
