package com.bolao2027.modules.tournament.infrastructure.persistence;

import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByOrderByMatchNumberAsc();
    List<Match> findByRoundNumberOrderByMatchNumberAsc(Integer roundNumber);
    List<Match> findByStageOrderByMatchNumberAsc(Stage stage);
    List<Match> findByGroupLetterOrderByMatchNumberAsc(String groupLetter);
    Optional<Match> findByMatchNumber(Integer matchNumber);
}
