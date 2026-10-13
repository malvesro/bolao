package com.bolao2027.modules.gamification.infrastructure.persistence;

import com.bolao2027.modules.gamification.domain.BadgeType;
import com.bolao2027.modules.gamification.domain.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserIdOrderByEarnedAtDesc(Long userId);
    boolean existsByUserIdAndBadgeType(Long userId, BadgeType badgeType);
    Optional<UserBadge> findByUserIdAndBadgeType(Long userId, BadgeType badgeType);
}
