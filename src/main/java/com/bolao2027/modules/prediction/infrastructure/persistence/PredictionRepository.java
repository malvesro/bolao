package com.bolao2027.modules.prediction.infrastructure.persistence;

import com.bolao2027.modules.prediction.domain.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    Optional<Prediction> findByUserIdAndMatchId(Long userId, Long matchId);
    List<Prediction> findByUserId(Long userId);
    List<Prediction> findByMatchId(Long matchId);
    long countByUserId(Long userId);
}
