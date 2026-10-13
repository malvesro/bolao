package com.bolao2027.modules.identity.web;

import com.bolao2027.modules.gamification.application.BadgeService;
import com.bolao2027.modules.gamification.domain.UserBadge;
import com.bolao2027.modules.identity.application.UserService;
import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.prediction.application.PredictionService;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.ranking.application.RankingService;
import com.bolao2027.modules.ranking.domain.RankingEntry;
import com.bolao2027.modules.tournament.application.MatchService;
import com.bolao2027.modules.tournament.domain.Match;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Controller para a página de perfil do participante (HU-profile).
 * Exibe histórico de palpites agrupados por rodada, badges de performance
 * e posição atual no ranking.
 */
@Controller
public class ProfileController {

    /**
     * Agrega um palpite com sua partida correspondente para exibição no perfil.
     */
    public record PredictionWithMatch(Prediction prediction, Match match) {}

    private final UserService userService;
    private final PredictionService predictionService;
    private final RankingService rankingService;
    private final MatchService matchService;
    private final BadgeService badgeService;

    public ProfileController(UserService userService,
                             PredictionService predictionService,
                             RankingService rankingService,
                             MatchService matchService,
                             BadgeService badgeService) {
        this.userService = userService;
        this.predictionService = predictionService;
        this.rankingService = rankingService;
        this.matchService = matchService;
        this.badgeService = badgeService;
    }

    /**
     * GET /profile — Página de perfil do participante autenticado.
     * Requer autenticação; redireciona para /login se não logado.
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        // Busca o User pelo username do principal
        User user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + principal.getUsername()));

        // Busca todos os palpites do usuário
        List<Prediction> allPredictions = predictionService.getUserPredictions(user.getId());

        // Agrupa palpites por rodada usando TreeMap (mantém ordem crescente)
        Map<Integer, List<PredictionWithMatch>> predsByRound = new TreeMap<>();
        for (Prediction p : allPredictions) {
            matchService.findById(p.getMatchId()).ifPresent(match -> {
                predsByRound.computeIfAbsent(match.getRoundNumber(), k -> new ArrayList<>())
                            .add(new PredictionWithMatch(p, match));
            });
        }

        // Busca posição atual no ranking (pode ser null se não tem pontos)
        Optional<RankingEntry> rankingEntryOpt = rankingService.getUserRanking(user.getId());
        RankingEntry rankingEntry = rankingEntryOpt.orElse(null);

        // Calcula estatísticas de performance
        ProfileStats stats = buildStats(allPredictions);

        // Pré-calcula pontos por rodada para o template (evita SpEL complexo)
        Map<Integer, Integer> pointsByRound = new TreeMap<>();
        predsByRound.forEach((round, preds) -> {
            int roundPts = preds.stream()
                    .map(pwm -> pwm.prediction().getPointsAwarded())
                    .filter(pts -> pts != null)
                    .mapToInt(Integer::intValue)
                    .sum();
            pointsByRound.put(round, roundPts);
        });

        model.addAttribute("user", user);
        model.addAttribute("rankingEntry", rankingEntry);
        model.addAttribute("predictionsByRound", predsByRound);
        model.addAttribute("pointsByRound", pointsByRound);
        model.addAttribute("stats", stats);
        model.addAttribute("activePage", "profile");
        model.addAttribute("pageTitle", "Meu Perfil — Bolão 2027");

        // Badges conquistados persistidos no banco
        List<UserBadge> badges = badgeService.getUserBadges(user.getId());
        model.addAttribute("badges", badges);

        return "profile/index";
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    /**
     * Calcula as estatísticas de performance a partir da lista de palpites.
     * Classifica por pontuação: exato=6, parcial=3, só resultado=2, gol certo=1, zero=0.
     */
    private ProfileStats buildStats(List<Prediction> predictions) {
        int totalPredicted = predictions.size();
        int scoredCount  = 0;
        int exactCount   = 0;  // 6 pts
        int partialCount = 0;  // 3 pts
        int resultCount  = 0;  // 2 pts
        int oneTeamCount = 0;  // 1 pt
        int zeroCount    = 0;  // 0 pts

        for (Prediction p : predictions) {
            if (p.getPointsAwarded() == null) continue;
            scoredCount++;
            int pts = p.getPointsAwarded();
            if      (pts == 6) exactCount++;
            else if (pts == 3) partialCount++;
            else if (pts == 2) resultCount++;
            else if (pts == 1) oneTeamCount++;
            else               zeroCount++;
        }

        return new ProfileStats(totalPredicted, scoredCount, exactCount, partialCount,
                                resultCount, oneTeamCount, zeroCount);
    }
}
