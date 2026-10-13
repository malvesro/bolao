package com.bolao2027.modules.ranking.web;

import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.ranking.application.RankingService;
import com.bolao2027.modules.ranking.domain.RankingEntry;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Controller para o leaderboard (ranking geral) do bolão.
 * Suporta paginação, filtro por rodada e HTMX para atualização automática a cada 60 segundos.
 */
@Controller
public class LeaderboardController {

    private final RankingService rankingService;
    private final UserRepository userRepository;

    public LeaderboardController(RankingService rankingService, UserRepository userRepository) {
        this.rankingService = rankingService;
        this.userRepository = userRepository;
    }

    /**
     * Tela principal do leaderboard com ranking geral ou filtrado por rodada.
     * GET /leaderboard
     *
     * @param page      Página atual (0-indexed, default 0)
     * @param size      Tamanho da página (default 20)
     * @param round     Filtro por rodada (null = geral, 1-7 = filtro por rodada)
     * @param principal Usuário autenticado
     * @param model     Model do Thymeleaf
     */
    @GetMapping("/leaderboard")
    public String leaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer round,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {

        List<RankingEntry> allEntries = (round != null)
                ? rankingService.buildLeaderboardUpToRound(round)
                : rankingService.buildLeaderboard();

        // Paginação manual sobre a lista já ordenada
        int totalEntries = allEntries.size();
        int totalPages = (int) Math.ceil((double) totalEntries / size);
        int safePage = Math.max(0, Math.min(page, totalPages > 0 ? totalPages - 1 : 0));
        int fromIndex = safePage * size;
        int toIndex = Math.min(fromIndex + size, totalEntries);
        List<RankingEntry> pageEntries = allEntries.subList(fromIndex, toIndex);

        model.addAttribute("entries", pageEntries);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalEntries", totalEntries);
        model.addAttribute("selectedRound", round);
        model.addAttribute("rounds", List.of(1, 2, 3, 4, 5, 6, 7));
        model.addAttribute("activePage", "leaderboard");

        // Entrada do usuário logado (sempre no leaderboard completo, não na página)
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
            if (user != null) {
                Optional<RankingEntry> currentUserEntry = rankingService.getUserRanking(user.getId());
                model.addAttribute("currentUserEntry", currentUserEntry.orElse(null));
                model.addAttribute("currentUserId", user.getId());
            }
        }

        return "leaderboard/index";
    }

    /**
     * Fragmento HTMX para atualização parcial da tabela de ranking.
     * Suporta os mesmos parâmetros de paginação e filtro do endpoint principal.
     * GET /leaderboard/fragment
     *
     * @param page      Página atual (0-indexed, default 0)
     * @param size      Tamanho da página (default 20)
     * @param round     Filtro por rodada (null = geral, 1-7 = filtro por rodada)
     * @param principal Usuário autenticado
     * @param model     Model do Thymeleaf
     */
    @GetMapping("/leaderboard/fragment")
    public String leaderboardFragment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer round,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {

        List<RankingEntry> allEntries = (round != null)
                ? rankingService.buildLeaderboardUpToRound(round)
                : rankingService.buildLeaderboard();

        // Paginação manual sobre a lista já ordenada
        int totalEntries = allEntries.size();
        int totalPages = (int) Math.ceil((double) totalEntries / size);
        int safePage = Math.max(0, Math.min(page, totalPages > 0 ? totalPages - 1 : 0));
        int fromIndex = safePage * size;
        int toIndex = Math.min(fromIndex + size, totalEntries);
        List<RankingEntry> pageEntries = allEntries.subList(fromIndex, toIndex);

        model.addAttribute("entries", pageEntries);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalEntries", totalEntries);
        model.addAttribute("selectedRound", round);
        model.addAttribute("rounds", List.of(1, 2, 3, 4, 5, 6, 7));

        if (principal != null) {
            User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
            if (user != null) {
                Optional<RankingEntry> currentUserEntry = rankingService.getUserRanking(user.getId());
                model.addAttribute("currentUserEntry", currentUserEntry.orElse(null));
                model.addAttribute("currentUserId", user.getId());
            }
        }

        return "leaderboard/fragments :: table";
    }
}
