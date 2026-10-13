package com.bolao2027.modules.gamification.domain;

/**
 * Tipos de badges conquistáveis no Bolão 2027.
 * Cada badge tem um emoji, nome e descrição para exibição.
 */
public enum BadgeType {

    PRIMEIRO_PALPITE("🎯", "Primeiro Palpite", "Registrou o primeiro palpite no bolão"),
    PLACAR_EXATO("💎", "Cravinhou!", "Acertou o placar exato de uma partida"),
    CINCO_EXATOS("🏆", "Sniper", "Acertou 5 ou mais placares exatos"),
    DEZ_EXATOS("👑", "Lenda", "Acertou 10 ou mais placares exatos"),
    PALPITOU_TUDO("📋", "Dedicado", "Palpitou em todas as partidas de uma rodada"),
    LIDER_RODADA("🥇", "Líder da Rodada", "Terminou uma rodada em 1º lugar no ranking"),
    COMEBACK("⚡", "Virada!", "Subiu 5 ou mais posições no ranking em uma rodada");

    private final String emoji;
    private final String name;
    private final String description;

    BadgeType(String emoji, String name, String description) {
        this.emoji = emoji;
        this.name = name;
        this.description = description;
    }

    public String getEmoji() { return emoji; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDisplayName() { return emoji + " " + name; }
}
