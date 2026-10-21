package de.musterstadt.zutritt.access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface AccessAttemptRepository extends JpaRepository<AccessAttempt, Long> {

    /**
     * Attempts in [from, to), newest first. "join fetch" loads the player in the same query
     * (avoids the N+1 problem when we later read the player name).
     */
    @Query("""
            select a from AccessAttempt a
            join fetch a.player
            where a.attemptedAt >= :from and a.attemptedAt < :to
            order by a.attemptedAt desc
            """)
    List<AccessAttempt> findInRange(Instant from, Instant to);
}
