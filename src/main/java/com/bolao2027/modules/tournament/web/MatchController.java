package com.bolao2027.modules.tournament.web;

import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.prediction.application.PredictionService;
import com.bolao2027.modules.prediction.application.PredictionView;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.domain.PredictionLockedException;
import com.bolao2027.modules.tournament.application.MatchService;
import com.bolao2027.modules.tournament.domain.Match;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller para visualização e interação com as partidas do torneio.
 * Suporta HTMX para atualizações parciais sem recarregar a página.
 */
@Controller
public class MatchController {

    private final MatchService matchService;
    private final PredictionService predictionService;
    private final UserRepository userRepository;

    public MatchController(MatchService matchService,
                           PredictionService predictionService,
                           UserRepository userRepository) {
        this.matchService = matchService;
        this.predictionService = predictionService;
        this.userRepository = userRepository;
    }

    /**
     * Tela principal de partidas agrupadas por rodada.
     * GET /matches
     */
    @GetMapping("/matches")
    public String listMatches(@AuthenticationPrincipal UserDetails principal, Model model) {
        List<Match> allMatches = matchService.findAllMatches();

        // Agrupar partidas por rodada (mantendo ordem)
        Map<Integer, List<Match>> byRound = allMatches.stream()
                .collect(Collectors.groupingBy(Match::getRoundNumber,
                        LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("matchesByRound", byRound);

        // Mapa de palpites do usuário logado
        if (principal != null) {
            User user = resolveUser(principal);
            if (user != null) {
                List<Prediction> predictions = predictionService.getUserPredictions(user.getId());
                Map<Long, Prediction> predictionByMatchId = predictions.stream()
                        .collect(Collectors.toMap(Prediction::getMatchId, p -> p));
                model.addAttribute("userPredictions", predictionByMatchId);
                model.addAttribute("currentUser", user);
            }
        }

        return "matches/list";
    }

    /**
     * Detalhe de uma partida com formulário de palpite e palpites públicos pós-bloqueio.
     * GET /matches/{id}
     */
    @GetMapping("/matches/{id}")
    public String matchDetail(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails principal,
                              Model model) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + id));

        model.addAttribute("match", match);

        if (principal != null) {
            User user = resolveUser(principal);
            if (user != null) {
                Optional<Prediction> userPrediction = predictionService.getOrCreatePrediction(user.getId(), id);
                model.addAttribute("userPrediction", userPrediction.orElse(null));
                model.addAttribute("currentUser", user);
            }
        }

        // Palpites públicos: carregados via HTMX após bloqueio
        if (match.isLocked()) {
            List<Prediction> publicPredictions = predictionService.getPublicPredictionsForMatch(id);
            model.addAttribute("publicPredictions", publicPredictions);
        }

        return "matches/detail";
    }

    /**
     * Salva ou atualiza o palpite via HTMX.
     * POST /matches/{id}/predict
     * Retorna fragmento Thymeleaf com feedback.
     */
    @PostMapping("/matches/{id}/predict")
    public String savePrediction(@PathVariable Long id,
                                 @RequestParam int homeScore,
                                 @RequestParam int awayScore,
                                 @AuthenticationPrincipal UserDetails principal,
                                 Model model) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + id));
        model.addAttribute("match", match);

        try {
            User user = resolveUser(principal);
            if (user == null) {
                model.addAttribute("errorMessage", "Usuário não autenticado.");
                return "matches/fragments :: prediction-error";
            }

            Prediction saved = predictionService.savePrediction(user.getId(), id, homeScore, awayScore);
            model.addAttribute("userPrediction", saved);
            model.addAttribute("successMessage", "Palpite salvo! " + homeScore + " × " + awayScore);
            return "matches/fragments :: prediction-saved";

        } catch (PredictionLockedException e) {
            model.addAttribute("errorMessage", "⏱️ Janela de palpites encerrada para esta partida.");
            return "matches/fragments :: prediction-error";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "matches/fragments :: prediction-error";
        }
    }

    /**
     * Retorna fragmento HTMX com os palpites públicos de uma partida bloqueada.
     * GET /matches/{id}/public-predictions
     */
    @GetMapping("/matches/{id}/public-predictions")
    public String publicPredictions(@PathVariable Long id, Model model) {
        Match match = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + id));

        List<PredictionView> publicPredictionViews = predictionService.getPublicPredictionViewsForMatch(id);
        model.addAttribute("match", match);
        model.addAttribute("publicPredictionViews", publicPredictionViews);

        return "matches/fragments :: public-predictions-table";
    }

    // ─── Aux ──────────────────────────────────────────────────────────────────

    private User resolveUser(UserDetails principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getUsername()).orElse(null);
    }
}
