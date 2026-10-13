package com.bolao2027.modules.prediction.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_match", columnNames = {"user_id", "match_id"})
})
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private Integer homeScore;

    @Column(nullable = false)
    private Integer awayScore;

    @Column(name = "points_awarded")
    private Integer pointsAwarded;

    @Column(name = "score_reason", length = 100)
    private String scoreReason;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Prediction() {
    }

    public Prediction(Long userId, Long matchId, Integer homeScore, Integer awayScore) {
        this.userId = userId;
        this.matchId = matchId;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.submittedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateScore(Integer homeScore, Integer awayScore) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.updatedAt = LocalDateTime.now();
    }

    public void applyScore(Integer pointsAwarded, String scoreReason) {
        this.pointsAwarded = pointsAwarded;
        this.scoreReason = scoreReason;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getScoreReason() {
        return scoreReason;
    }

    public void setScoreReason(String scoreReason) {
        this.scoreReason = scoreReason;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
