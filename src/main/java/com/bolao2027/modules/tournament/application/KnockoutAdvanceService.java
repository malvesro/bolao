package com.bolao2027.modules.tournament.application;

import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.MatchStatus;
import com.bolao2027.modules.tournament.domain.Stage;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Serviço responsável pelo avanço automático do mata-mata.
 *
 * Fluxo:
 *  1. Após o admin definir os classificados de cada grupo (A-H), chamar
 *     {@link #advanceGroupsToRoundOf16} para popular as oitavas de final.
 *  2. Após encerrar cada partida do mata-mata, chamar
 *     {@link #advanceKnockoutWinners} para propagar os vencedores para a
 *     rodada seguinte automaticamente.
 */
@Service
@Transactional
public class KnockoutAdvanceService {

    private final MatchRepository matchRepository;

    public KnockoutAdvanceService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * Tabela oficial de confrontos das oitavas de final (FIFA 2027):
     * As oitavas seguem o cruzamento oficial da FIFA.
     * Identifica partidas da Stage ROUND_OF_16 que ainda têm times TBD
     * e atualiza com base no mapeamento grupo → classificados.
     *
     * Estrutura: Map<String, String> groupClassified onde
     *   key = "A1" (1º do grupo A), "A2" (2º do grupo A), etc.
     *   value = nome da seleção
     */
    public void advanceGroupsToRoundOf16(Map<String, String> groupClassified) {
        List<Match> roundOf16 = matchRepository.findByStageOrderByMatchNumberAsc(Stage.ROUND_OF_16);

        for (Match match : roundOf16) {
            String home = resolveTeam(match.getHomeTeam(), groupClassified);
            String away = resolveTeam(match.getAwayTeam(), groupClassified);
            if (!home.equals(match.getHomeTeam()) || !away.equals(match.getAwayTeam())) {
                match.setHomeTeam(home);
                match.setAwayTeam(away);
                matchRepository.save(match);
            }
        }
    }

    /**
     * Avança vencedores de um round para o próximo round (quartas, semis, final).
     * Procura partidas FINISHED e propaga o vencedor (ou time penaltyWinner) para a partida seguinte.
     *
     * @return número de partidas atualizadas
     */
    public int advanceKnockoutWinners() {
        int updated = 0;
        List<Match> finished = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchStatus.FINISHED)
                .filter(m -> isKnockoutStage(m.getStage()))
                .toList();

        for (Match finishedMatch : finished) {
            String winner = determineWinner(finishedMatch);
            if (winner == null) continue;

            // Encontra partidas seguintes que referenciam esta partida pelo número
            List<Match> nextMatches = matchRepository.findAll().stream()
                    .filter(m -> m.getHomeTeam().contains("Jogo " + finishedMatch.getMatchNumber())
                            || m.getAwayTeam().contains("Jogo " + finishedMatch.getMatchNumber()))
                    .toList();

            for (Match next : nextMatches) {
                if (next.getHomeTeam().contains("Jogo " + finishedMatch.getMatchNumber())) {
                    next.setHomeTeam(winner);
                    matchRepository.save(next);
                    updated++;
                } else if (next.getAwayTeam().contains("Jogo " + finishedMatch.getMatchNumber())) {
                    next.setAwayTeam(winner);
                    matchRepository.save(next);
                    updated++;
                }
            }
        }
        return updated;
    }

    /**
     * Resolve um placeholder de time (ex: "1º Grupo A") para o nome real da seleção,
     * usando o mapa de classificados.
     *
     * @param placeholder string original do campo homeTeam/awayTeam
     * @param classified  mapa "A1" → nome da seleção
     * @return nome real se encontrado, ou o próprio placeholder se não mapeado
     */
    private String resolveTeam(String placeholder, Map<String, String> classified) {
        for (Map.Entry<String, String> entry : classified.entrySet()) {
            if (placeholder.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return placeholder; // mantém placeholder se não encontrado
    }

    /**
     * Retorna true se o stage for uma fase de mata-mata.
     */
    private boolean isKnockoutStage(Stage stage) {
        return stage == Stage.ROUND_OF_16
                || stage == Stage.QUARTER_FINALS
                || stage == Stage.SEMI_FINALS
                || stage == Stage.FINAL
                || stage == Stage.THIRD_PLACE;
    }

    /**
     * Determina o vencedor de uma partida encerrada.
     * Prioriza penaltyWinner; caso contrário, compara placar em 120 minutos.
     *
     * @return nome do vencedor, ou null se não determinável (empate sem pênaltis — não deve ocorrer)
     */
    private String determineWinner(Match match) {
        if (match.getPenaltyWinner() != null && !match.getPenaltyWinner().isBlank()) {
            return match.getPenaltyWinner();
        }
        if (match.getHomeScore120() == null || match.getAwayScore120() == null) return null;
        if (match.getHomeScore120() > match.getAwayScore120()) return match.getHomeTeam();
        if (match.getAwayScore120() > match.getHomeScore120()) return match.getAwayTeam();
        return null; // empate sem penaltyWinner não deveria acontecer em mata-mata
    }
}
