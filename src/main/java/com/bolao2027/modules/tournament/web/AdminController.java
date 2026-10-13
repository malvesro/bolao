package com.bolao2027.modules.tournament.web;

import com.bolao2027.modules.scoring.application.ScoringService;
import com.bolao2027.modules.tournament.application.KnockoutAdvanceService;
import com.bolao2027.modules.tournament.application.MatchService;
import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.Stage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Painel administrativo para lançamento e correção de resultados oficiais das partidas.
 * Acesso restrito a usuários com ROLE_ADMIN (HU-08).
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final MatchService matchService;
    private final ScoringService scoringService;
    private final KnockoutAdvanceService knockoutAdvanceService;

    public AdminController(MatchService matchService, ScoringService scoringService,
                           KnockoutAdvanceService knockoutAdvanceService) {
        this.matchService = matchService;
        this.scoringService = scoringService;
        this.knockoutAdvanceService = knockoutAdvanceService;
    }

    /**
     * Redirect da raiz /admin para /admin/matches.
     */
    @GetMapping
    public String adminHome() {
        return "redirect:/admin/matches";
    }

    /**
     * Listagem de partidas para lançamento de resultado.
     * GET /admin/matches
     */
    @GetMapping("/matches")
    public String adminMatches(Model model) {
        List<Match> matches = matchService.findAllMatches();
        model.addAttribute("matches", matches);
        return "admin/matches";
    }

    /**
     * Lança ou corrige o resultado oficial de uma partida e dispara recálculo de pontuação.
     * POST /admin/matches/{id}/result
     * Suporta HTMX: retorna fragmento de feedback se requisição HTMX, redirect se normal.
     */
    @PostMapping("/matches/{id}/result")
    public String updateMatchResult(@PathVariable Long id,
                                    @RequestParam Integer homeScore120,
                                    @RequestParam Integer awayScore120,
                                    @RequestParam(required = false) String penaltyWinner,
                                    @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            Match updated = matchService.updateMatchResult(id, homeScore120, awayScore120, penaltyWinner);
            model.addAttribute("match", updated);
            model.addAttribute("successMessage",
                    "✅ Resultado salvo: " + updated.getHomeTeam() + " " + homeScore120
                    + " × " + awayScore120 + " " + updated.getAwayTeam());

            if (htmxRequest != null) {
                return "admin/fragments :: result-saved";
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    "Resultado da partida #" + id + " salvo com sucesso!");
        } catch (Exception e) {
            model.addAttribute("match", matchService.findById(id).orElse(null));
            model.addAttribute("errorMessage", "Erro ao salvar resultado: " + e.getMessage());

            if (htmxRequest != null) {
                return "admin/fragments :: result-error";
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/matches";
    }

    /**
     * Recalcula todas as pontuações de partidas já encerradas (operação idempotente).
     * POST /admin/recalculate
     */
    @PostMapping("/recalculate")
    public String recalculateAll(RedirectAttributes redirectAttributes) {
        try {
            scoringService.recalculateAllScores();
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Recálculo completo executado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Erro no recálculo: " + e.getMessage());
        }
        return "redirect:/admin/matches";
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mata-Mata: Avanço automático de oitavas e vencedores
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Tela para classificar grupos e popular as oitavas de final.
     * GET /admin/knockout
     */
    @GetMapping("/knockout")
    public String knockoutAdmin(Model model) {
        List<Match> roundOf16 = matchService.findByStage(Stage.ROUND_OF_16);
        model.addAttribute("roundOf16", roundOf16);
        model.addAttribute("groups", List.of("A", "B", "C", "D", "E", "F", "G", "H"));
        return "admin/knockout";
    }

    /**
     * Recebe os classificados de cada grupo e popula as oitavas de final.
     * POST /admin/knockout/advance-groups
     */
    @PostMapping("/knockout/advance-groups")
    public String advanceGroups(@RequestParam Map<String, String> params,
                                RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> classified = new HashMap<>();
            for (String group : List.of("A", "B", "C", "D", "E", "F", "G", "H")) {
                String first  = params.get("group" + group + "1");
                String second = params.get("group" + group + "2");
                if (first  != null && !first.isBlank())  classified.put(group + "1", first.trim());
                if (second != null && !second.isBlank()) classified.put(group + "2", second.trim());
            }
            knockoutAdvanceService.advanceGroupsToRoundOf16(classified);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Oitavas populadas com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro: " + e.getMessage());
        }
        return "redirect:/admin/knockout";
    }

    /**
     * Avança vencedores para a fase seguinte automaticamente.
     * POST /admin/knockout/advance-winners
     */
    @PostMapping("/knockout/advance-winners")
    public String advanceWinners(RedirectAttributes redirectAttributes) {
        try {
            int count = knockoutAdvanceService.advanceKnockoutWinners();
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ " + count + " partidas atualizadas com vencedores!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro: " + e.getMessage());
        }
        return "redirect:/admin/knockout";
    }
}
