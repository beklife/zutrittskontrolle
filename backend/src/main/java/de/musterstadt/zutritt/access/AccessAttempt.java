package de.musterstadt.zutritt.access;

import de.musterstadt.zutritt.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "access_attempt")
public class AccessAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "granted", nullable = false)
    private boolean granted;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 20)
    private AccessReason reason;

    /** Only for JPA. */
    protected AccessAttempt() {
    }

    public AccessAttempt(Player player, Instant attemptedAt, AccessReason reason) {
        this.player = player;
        this.attemptedAt = attemptedAt;
        this.reason = reason;
        this.granted = reason == AccessReason.GRANTED;
    }

    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public boolean isGranted() {
        return granted;
    }

    public AccessReason getReason() {
        return reason;
    }
}
