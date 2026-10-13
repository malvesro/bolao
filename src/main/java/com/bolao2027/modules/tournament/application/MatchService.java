package com.bolao2027.modules.tournament.application;

import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.MatchStatus;
import com.bolao2027.modules.tournament.domain.Stage;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import com.bolao2027.modules.scoring.application.ScoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;
    private final ScoringService scoringService;

    public MatchService(MatchRepository matchRepository, ScoringService scoringService) {
        this.matchRepository = matchRepository;
        this.scoringService = scoringService;
    }

    @Transactional(readOnly = true)
    public List<Match> findAllMatches() {
        return matchRepository.findAllByOrderByMatchNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<Match> findByRound(Integer roundNumber) {
        return matchRepository.findByRoundNumberOrderByMatchNumberAsc(roundNumber);
    }

    @Transactional(readOnly = true)
    public List<Match> findByStage(Stage stage) {
        return matchRepository.findByStageOrderByMatchNumberAsc(stage);
    }

    @Transactional(readOnly = true)
    public Optional<Match> findById(Long id) {
        return matchRepository.findById(id);
    }

    public Match updateMatchResult(Long matchId, Integer homeScore120, Integer awayScore120, String penaltyWinner) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + matchId));

        if (homeScore120 < 0 || awayScore120 < 0) {
            throw new IllegalArgumentException("Placar não pode conter valores negativos.");
        }

        match.setHomeScore120(homeScore120);
        match.setAwayScore120(awayScore120);
        match.setPenaltyWinner(penaltyWinner != null && !penaltyWinner.isBlank() ? penaltyWinner.trim() : null);
        match.setStatus(MatchStatus.FINISHED);

        Match saved = matchRepository.save(match);
        if (scoringService != null) {
            scoringService.calculateScoresForMatch(saved);
        }
        return saved;
    }

    public Match updateKnockoutTeams(Long matchId, String homeTeam, String awayTeam) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + matchId));

        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        return matchRepository.save(match);
    }
}
