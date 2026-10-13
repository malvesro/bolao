package com.bolao2027.modules.tournament.domain;

public enum Stage {
    GROUP("Fase de Grupos"),
    ROUND_OF_16("Oitavas de Final"),
    QUARTER_FINALS("Quartas de Final"),
    SEMI_FINALS("Semifinais"),
    THIRD_PLACE("Disputa de 3º Lugar"),
    FINAL("Grande Final");

    private final String description;

    Stage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isKnockout() {
        return this != GROUP;
    }
}
