package com.bolao2027.modules.tournament.domain;

import com.bolao2027.shared.utils.DateUtils;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer matchNumber;

    @Column(nullable = false)
    private Integer roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Stage stage;

    @Column(length = 5)
    private String groupLetter;

    @Column(nullable = false, length = 60)
    private String homeTeam;

    @Column(nullable = false, length = 60)
    private String awayTeam;

    @Column(nullable = false)
    private LocalDateTime matchDateTime;

    @Column(nullable = false, length = 80)
    private String stadium;

    @Column(nullable = false, length = 60)
    private String city;

    private Integer homeScore120;
    private Integer awayScore120;

    @Column(length = 60)
    private String penaltyWinner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status;

    public Match() {
    }

    public Match(Integer matchNumber, Integer roundNumber, Stage stage, String groupLetter,
                 String homeTeam, String awayTeam, LocalDateTime matchDateTime,
                 String stadium, String city) {
        this.matchNumber = matchNumber;
        this.roundNumber = roundNumber;
        this.stage = stage;
        this.groupLetter = groupLetter;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDateTime = matchDateTime;
        this.stadium = stadium;
        this.city = city;
        this.status = MatchStatus.SCHEDULED;
    }

    public boolean isLocked() {
        return DateUtils.isMatchLocked(this.matchDateTime);
    }

    public boolean isFinished() {
        return this.status == MatchStatus.FINISHED;
    }

    public String getFormattedDateTime() {
        return DateUtils.formatBrt(this.matchDateTime);
    }

    public String getFormattedDate() {
        return DateUtils.formatDateOnly(this.matchDateTime);
    }

    public String getFormattedTime() {
        return DateUtils.formatTimeOnly(this.matchDateTime);
    }

    public String getFormattedCutoffTime() {
        return DateUtils.formatBrt(DateUtils.calculateCutoffTime(this.matchDateTime));
    }

    public long getMinutesUntilCutoff() {
        return DateUtils.minutesUntilCutoff(this.matchDateTime);
    }

    /**
     * Unix epoch second (UTC) when the prediction window closes for this match.
     * Used by the JS countdown: data-cutoff-epoch="${match.cutoffEpoch}"
     */
    public long getCutoffEpoch() {
        return DateUtils.cutoffEpoch(this.matchDateTime);
    }

    public boolean isTeamsTbd() {
        return homeTeam.contains("1º") || homeTeam.contains("2º") || homeTeam.contains("Vencedor")
                || awayTeam.contains("1º") || awayTeam.contains("2º") || awayTeam.contains("Vencedor");
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Integer getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getGroupLetter() {
        return groupLetter;
    }

    public void setGroupLetter(String groupLetter) {
        this.groupLetter = groupLetter;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public void setMatchDateTime(LocalDateTime matchDateTime) {
        this.matchDateTime = matchDateTime;
    }

    public String getStadium() {
        return stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getHomeScore120() {
        return homeScore120;
    }

    public void setHomeScore120(Integer homeScore120) {
        this.homeScore120 = homeScore120;
    }

    public Integer getAwayScore120() {
        return awayScore120;
    }

    public void setAwayScore120(Integer awayScore120) {
        this.awayScore120 = awayScore120;
    }

    public String getPenaltyWinner() {
        return penaltyWinner;
    }

    public void setPenaltyWinner(String penaltyWinner) {
        this.penaltyWinner = penaltyWinner;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }
}
