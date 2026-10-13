package com.bolao2027.modules.tournament.infrastructure;

import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.Stage;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(2)
public class MatchDataSeeder implements CommandLineRunner {

    private final MatchRepository matchRepository;

    public MatchDataSeeder(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public void run(String... args) {
        if (matchRepository.count() == 0) {
            seedOfficialFifa2027Matches();
        }
    }

    private void seedOfficialFifa2027Matches() {
        List<Match> matches = new ArrayList<>();

        // Cidades e estádios principais no Brasil
        String maracana = "Estádio do Maracanã";
        String rio = "Rio de Janeiro";
        String neoQuimica = "Neo Química Arena";
        String sp = "São Paulo";
        String mineirao = "Mineirão";
        String bh = "Belo Horizonte";
        String maneGarrincha = "Mané Garrincha";
        String brasilia = "Brasília";
        String beiraRio = "Beira-Rio";
        String portoAlegre = "Porto Alegre";
        String fonteNova = "Arena Fonte Nova";
        String salvador = "Salvador";
        String arenaPernambuco = "Arena de Pernambuco";
        String recife = "Recife";
        String castelao = "Arena Castelão";
        String fortaleza = "Fortaleza";

        // ==========================================
        // RODADA 1: Fase de Grupos (Jogos 1 a 16)
        // ==========================================
        // Grupo A
        matches.add(new Match(1, 1, Stage.GROUP, "A", "Brasil", "Nigéria",
                LocalDateTime.of(2027, 6, 24, 16, 0), maracana, rio));
        matches.add(new Match(2, 1, Stage.GROUP, "A", "França", "Coreia do Sul",
                LocalDateTime.of(2027, 6, 24, 19, 0), neoQuimica, sp));

        // Grupo B
        matches.add(new Match(3, 1, Stage.GROUP, "B", "Espanha", "Colômbia",
                LocalDateTime.of(2027, 6, 25, 13, 0), mineirao, bh));
        matches.add(new Match(4, 1, Stage.GROUP, "B", "Japão", "Zâmbia",
                LocalDateTime.of(2027, 6, 25, 16, 0), maneGarrincha, brasilia));

        // Grupo C
        matches.add(new Match(5, 1, Stage.GROUP, "C", "Inglaterra", "Dinamarca",
                LocalDateTime.of(2027, 6, 25, 19, 0), fonteNova, salvador));
        matches.add(new Match(6, 1, Stage.GROUP, "C", "China", "Haiti",
                LocalDateTime.of(2027, 6, 26, 13, 0), beiraRio, portoAlegre));

        // Grupo D
        matches.add(new Match(7, 1, Stage.GROUP, "D", "Estados Unidos", "Países Baixos",
                LocalDateTime.of(2027, 6, 26, 16, 0), neoQuimica, sp));
        matches.add(new Match(8, 1, Stage.GROUP, "D", "Portugal", "Vietnã",
                LocalDateTime.of(2027, 6, 26, 19, 0), arenaPernambuco, recife));

        // Grupo E
        matches.add(new Match(9, 1, Stage.GROUP, "E", "Suécia", "Itália",
                LocalDateTime.of(2027, 6, 27, 13, 0), castelao, fortaleza));
        matches.add(new Match(10, 1, Stage.GROUP, "E", "África do Sul", "Argentina",
                LocalDateTime.of(2027, 6, 27, 16, 0), maneGarrincha, brasilia));

        // Grupo F
        matches.add(new Match(11, 1, Stage.GROUP, "F", "Alemanha", "Canadá",
                LocalDateTime.of(2027, 6, 27, 19, 0), maracana, rio));
        matches.add(new Match(12, 1, Stage.GROUP, "F", "Austrália", "Marrocos",
                LocalDateTime.of(2027, 6, 28, 13, 0), mineirao, bh));

        // Grupo G
        matches.add(new Match(13, 1, Stage.GROUP, "G", "Noruega", "Suíça",
                LocalDateTime.of(2027, 6, 28, 16, 0), fonteNova, salvador));
        matches.add(new Match(14, 1, Stage.GROUP, "G", "Nova Zelândia", "Filipinas",
                LocalDateTime.of(2027, 6, 28, 19, 0), beiraRio, portoAlegre));

        // Grupo H
        matches.add(new Match(15, 1, Stage.GROUP, "H", "México", "Irlanda",
                LocalDateTime.of(2027, 6, 29, 15, 0), arenaPernambuco, recife));
        matches.add(new Match(16, 1, Stage.GROUP, "H", "Jamaica", "Costa Rica",
                LocalDateTime.of(2027, 6, 29, 18, 0), castelao, fortaleza));

        // ==========================================
        // RODADA 2: Fase de Grupos (Jogos 17 a 32)
        // ==========================================
        matches.add(new Match(17, 2, Stage.GROUP, "A", "Brasil", "França",
                LocalDateTime.of(2027, 6, 30, 16, 0), maracana, rio));
        matches.add(new Match(18, 2, Stage.GROUP, "A", "Coreia do Sul", "Nigéria",
                LocalDateTime.of(2027, 6, 30, 19, 0), neoQuimica, sp));

        matches.add(new Match(19, 2, Stage.GROUP, "B", "Espanha", "Japão",
                LocalDateTime.of(2027, 7, 1, 13, 0), maneGarrincha, brasilia));
        matches.add(new Match(20, 2, Stage.GROUP, "B", "Zâmbia", "Colômbia",
                LocalDateTime.of(2027, 7, 1, 16, 0), mineirao, bh));

        matches.add(new Match(21, 2, Stage.GROUP, "C", "Inglaterra", "China",
                LocalDateTime.of(2027, 7, 1, 19, 0), fonteNova, salvador));
        matches.add(new Match(22, 2, Stage.GROUP, "C", "Haiti", "Dinamarca",
                LocalDateTime.of(2027, 7, 2, 13, 0), beiraRio, portoAlegre));

        matches.add(new Match(23, 2, Stage.GROUP, "D", "Estados Unidos", "Portugal",
                LocalDateTime.of(2027, 7, 2, 16, 0), neoQuimica, sp));
        matches.add(new Match(24, 2, Stage.GROUP, "D", "Vietnã", "Países Baixos",
                LocalDateTime.of(2027, 7, 2, 19, 0), arenaPernambuco, recife));

        matches.add(new Match(25, 2, Stage.GROUP, "E", "Suécia", "África do Sul",
                LocalDateTime.of(2027, 7, 3, 13, 0), castelao, fortaleza));
        matches.add(new Match(26, 2, Stage.GROUP, "E", "Argentina", "Itália",
                LocalDateTime.of(2027, 7, 3, 16, 0), maneGarrincha, brasilia));

        matches.add(new Match(27, 2, Stage.GROUP, "F", "Alemanha", "Austrália",
                LocalDateTime.of(2027, 7, 3, 19, 0), maracana, rio));
        matches.add(new Match(28, 2, Stage.GROUP, "F", "Marrocos", "Canadá",
                LocalDateTime.of(2027, 7, 4, 13, 0), mineirao, bh));

        matches.add(new Match(29, 2, Stage.GROUP, "G", "Noruega", "Nova Zelândia",
                LocalDateTime.of(2027, 7, 4, 16, 0), fonteNova, salvador));
        matches.add(new Match(30, 2, Stage.GROUP, "G", "Filipinas", "Suíça",
                LocalDateTime.of(2027, 7, 4, 19, 0), beiraRio, portoAlegre));

        matches.add(new Match(31, 2, Stage.GROUP, "H", "México", "Jamaica",
                LocalDateTime.of(2027, 7, 5, 15, 0), arenaPernambuco, recife));
        matches.add(new Match(32, 2, Stage.GROUP, "H", "Costa Rica", "Irlanda",
                LocalDateTime.of(2027, 7, 5, 18, 0), castelao, fortaleza));

        // ==========================================
        // RODADA 3: Fase de Grupos (Jogos 33 a 48)
        // ==========================================
        matches.add(new Match(33, 3, Stage.GROUP, "A", "Coreia do Sul", "Brasil",
                LocalDateTime.of(2027, 7, 6, 16, 0), maneGarrincha, brasilia));
        matches.add(new Match(34, 3, Stage.GROUP, "A", "Nigéria", "França",
                LocalDateTime.of(2027, 7, 6, 16, 0), neoQuimica, sp));

        matches.add(new Match(35, 3, Stage.GROUP, "B", "Zâmbia", "Espanha",
                LocalDateTime.of(2027, 7, 6, 19, 0), mineirao, bh));
        matches.add(new Match(36, 3, Stage.GROUP, "B", "Colômbia", "Japão",
                LocalDateTime.of(2027, 7, 6, 19, 0), beiraRio, portoAlegre));

        matches.add(new Match(37, 3, Stage.GROUP, "C", "Haiti", "Inglaterra",
                LocalDateTime.of(2027, 7, 7, 16, 0), fonteNova, salvador));
        matches.add(new Match(38, 3, Stage.GROUP, "C", "Dinamarca", "China",
                LocalDateTime.of(2027, 7, 7, 16, 0), arenaPernambuco, recife));

        matches.add(new Match(39, 3, Stage.GROUP, "D", "Vietnã", "Estados Unidos",
                LocalDateTime.of(2027, 7, 7, 19, 0), neoQuimica, sp));
        matches.add(new Match(40, 3, Stage.GROUP, "D", "Países Baixos", "Portugal",
                LocalDateTime.of(2027, 7, 7, 19, 0), maracana, rio));

        matches.add(new Match(41, 3, Stage.GROUP, "E", "Argentina", "Suécia",
                LocalDateTime.of(2027, 7, 8, 16, 0), castelao, fortaleza));
        matches.add(new Match(42, 3, Stage.GROUP, "E", "Itália", "África do Sul",
                LocalDateTime.of(2027, 7, 8, 16, 0), maneGarrincha, brasilia));

        matches.add(new Match(43, 3, Stage.GROUP, "F", "Marrocos", "Alemanha",
                LocalDateTime.of(2027, 7, 8, 19, 0), mineirao, bh));
        matches.add(new Match(44, 3, Stage.GROUP, "F", "Canadá", "Austrália",
                LocalDateTime.of(2027, 7, 8, 19, 0), fonteNova, salvador));

        matches.add(new Match(45, 3, Stage.GROUP, "G", "Filipinas", "Noruega",
                LocalDateTime.of(2027, 7, 9, 16, 0), beiraRio, portoAlegre));
        matches.add(new Match(46, 3, Stage.GROUP, "G", "Suíça", "Nova Zelândia",
                LocalDateTime.of(2027, 7, 9, 16, 0), arenaPernambuco, recife));

        matches.add(new Match(47, 3, Stage.GROUP, "H", "Costa Rica", "México",
                LocalDateTime.of(2027, 7, 9, 19, 0), castelao, fortaleza));
        matches.add(new Match(48, 3, Stage.GROUP, "H", "Irlanda", "Jamaica",
                LocalDateTime.of(2027, 7, 9, 19, 0), neoQuimica, sp));

        // ==========================================
        // RODADA 4: Oitavas de Final (Jogos 49 a 56)
        // ==========================================
        matches.add(new Match(49, 4, Stage.ROUND_OF_16, null, "1º Grupo A", "2º Grupo C",
                LocalDateTime.of(2027, 7, 11, 14, 0), maracana, rio));
        matches.add(new Match(50, 4, Stage.ROUND_OF_16, null, "1º Grupo C", "2º Grupo A",
                LocalDateTime.of(2027, 7, 11, 18, 0), neoQuimica, sp));

        matches.add(new Match(51, 4, Stage.ROUND_OF_16, null, "1º Grupo E", "2º Grupo G",
                LocalDateTime.of(2027, 7, 12, 14, 0), mineirao, bh));
        matches.add(new Match(52, 4, Stage.ROUND_OF_16, null, "1º Grupo G", "2º Grupo E",
                LocalDateTime.of(2027, 7, 12, 18, 0), maneGarrincha, brasilia));

        matches.add(new Match(53, 4, Stage.ROUND_OF_16, null, "1º Grupo B", "2º Grupo D",
                LocalDateTime.of(2027, 7, 13, 14, 0), fonteNova, salvador));
        matches.add(new Match(54, 4, Stage.ROUND_OF_16, null, "1º Grupo D", "2º Grupo B",
                LocalDateTime.of(2027, 7, 13, 18, 0), beiraRio, portoAlegre));

        matches.add(new Match(55, 4, Stage.ROUND_OF_16, null, "1º Grupo F", "2º Grupo H",
                LocalDateTime.of(2027, 7, 14, 14, 0), castelao, fortaleza));
        matches.add(new Match(56, 4, Stage.ROUND_OF_16, null, "1º Grupo H", "2º Grupo F",
                LocalDateTime.of(2027, 7, 14, 18, 0), arenaPernambuco, recife));

        // ==========================================
        // RODADA 5: Quartas de Final (Jogos 57 a 60)
        // ==========================================
        matches.add(new Match(57, 5, Stage.QUARTER_FINALS, null, "Vencedor Jogo 49", "Vencedor Jogo 51",
                LocalDateTime.of(2027, 7, 16, 14, 0), neoQuimica, sp));
        matches.add(new Match(58, 5, Stage.QUARTER_FINALS, null, "Vencedor Jogo 50", "Vencedor Jogo 52",
                LocalDateTime.of(2027, 7, 16, 18, 0), mineirao, bh));

        matches.add(new Match(59, 5, Stage.QUARTER_FINALS, null, "Vencedor Jogo 53", "Vencedor Jogo 55",
                LocalDateTime.of(2027, 7, 17, 14, 0), maneGarrincha, brasilia));
        matches.add(new Match(60, 5, Stage.QUARTER_FINALS, null, "Vencedor Jogo 54", "Vencedor Jogo 56",
                LocalDateTime.of(2027, 7, 17, 18, 0), maracana, rio));

        // ==========================================
        // RODADA 6: Semifinais (Jogos 61 e 62)
        // ==========================================
        matches.add(new Match(61, 6, Stage.SEMI_FINALS, null, "Vencedor Jogo 57", "Vencedor Jogo 59",
                LocalDateTime.of(2027, 7, 20, 16, 0), neoQuimica, sp));
        matches.add(new Match(62, 6, Stage.SEMI_FINALS, null, "Vencedor Jogo 58", "Vencedor Jogo 60",
                LocalDateTime.of(2027, 7, 21, 16, 0), maneGarrincha, brasilia));

        // ==========================================
        // RODADA 7: Disputa 3º Lugar e Final (Jogos 63 e 64)
        // ==========================================
        matches.add(new Match(63, 7, Stage.THIRD_PLACE, null, "Perdedor Jogo 61", "Perdedor Jogo 62",
                LocalDateTime.of(2027, 7, 24, 16, 0), mineirao, bh));
        matches.add(new Match(64, 7, Stage.FINAL, null, "Vencedor Jogo 61", "Vencedor Jogo 62",
                LocalDateTime.of(2027, 7, 25, 16, 0), maracana, rio));

        matchRepository.saveAll(matches);
    }
}
