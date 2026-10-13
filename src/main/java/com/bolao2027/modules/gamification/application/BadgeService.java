package com.bolao2027.modules.gamification.application;

import com.bolao2027.modules.gamification.domain.BadgeType;
import com.bolao2027.modules.gamification.domain.UserBadge;
import com.bolao2027.modules.gamification.infrastructure.persistence.UserBadgeRepository;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.infrastructure.persistence.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class BadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final PredictionRepository predictionRepository;

    public BadgeService(UserBadgeRepository userBadgeRepository,
                        PredictionRepository predictionRepository) {
        this.userBadgeRepository = userBadgeRepository;
        this.predictionRepository = predictionRepository;
    }

    /**
     * Avalia e concede badges para um usuário após um novo palpite ou recálculo.
     * Idempotente: não concede badge já existente.
     */
    public List<UserBadge> evaluateAndAward(Long userId) {
        List<Prediction> allPredictions = predictionRepository.findByUserId(userId);

        // Badge: PRIMEIRO_PALPITE
        if (!allPredictions.isEmpty()) {
            award(userId, BadgeType.PRIMEIRO_PALPITE);
        }

        // Palpites pontuados
        List<Prediction> scored = allPredictions.stream()
                .filter(p -> p.getPointsAwarded() != null)
                .toList();

        long exactCount = scored.stream().filter(p -> p.getPointsAwarded() == 6).count();

        // Badge: PLACAR_EXATO (primeiro acerto exato)
        if (exactCount >= 1) {
            award(userId, BadgeType.PLACAR_EXATO);
        }
        // Badge: CINCO_EXATOS
        if (exactCount >= 5) {
            award(userId, BadgeType.CINCO_EXATOS);
        }
        // Badge: DEZ_EXATOS
        if (exactCount >= 10) {
            award(userId, BadgeType.DEZ_EXATOS);
        }

        return userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }

    /**
     * Retorna todos os badges de um usuário, ordenados por data de conquista DESC.
     */
    @Transactional(readOnly = true)
    public List<UserBadge> getUserBadges(Long userId) {
        return userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }

    /**
     * Concede um badge de forma idempotente (ignora se já existir).
     */
    private void award(Long userId, BadgeType badgeType) {
        if (!userBadgeRepository.existsByUserIdAndBadgeType(userId, badgeType)) {
            userBadgeRepository.save(new UserBadge(userId, badgeType));
        }
    }
}
