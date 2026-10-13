package com.bolao2027.modules.gamification.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_user_badge_type",
           columnNames = {"user_id", "badge_type"}))
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 50)
    private BadgeType badgeType;

    @Column(nullable = false)
    private LocalDateTime earnedAt;

    public UserBadge() {}

    public UserBadge(Long userId, BadgeType badgeType) {
        this.userId = userId;
        this.badgeType = badgeType;
        this.earnedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BadgeType getBadgeType() { return badgeType; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
}
