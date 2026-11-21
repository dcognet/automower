package com.automower.infrastructure.cli;

import com.automower.application.MowLawnUseCase;
import com.automower.infrastructure.file.MowingPlanFileReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class MowerCliLoadIntegrationTest {

    private static final int MAX_FILE_SIZE = 10_000_000;
    // « de l'ordre de la seconde » : marge pour une machine de CI chargée, mesuré autour de 0,2 s et 0,9 s en local
    private static final Duration MAX_DURATION = Duration.ofSeconds(3);
    private static final String CYCLE = "FFRFFL";
    private static final int ROW_WIDTH = 1000;

    @TempDir
    private Path tempDir;

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();

    /**
     * Entrée : fichier généré de près de 10 Mo, pelouse maximale, une seule tondeuse au centre avec ~10⁷ instructions (cycles FFRFFL)
     * Scénario : chaque cycle avance de 2 cases vers le nord puis de 2 vers l'est, sans jamais atteindre un bord
     * Résultat : une ligne de sortie correcte, code de sortie 0, en un temps de l'ordre de la seconde avec la heap par défaut
     */
    @Test
    void run_singleMowerWithTenMillionInstructions_completesInAboutOneSecond() throws Exception {
        String header = "40075016 40075016\n20000000 20000000 N\n";
        int cycles = (MAX_FILE_SIZE - header.length() - 1) / CYCLE.length();
        Path input = write(header + CYCLE.repeat(cycles) + "\n");

        Duration duration = timedRun(input);

        int arrival = 20_000_000 + 2 * cycles;
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo(arrival + " " + arrival + " N\n");
        assertThat(duration).isLessThan(MAX_DURATION);
    }

    /**
     * Entrée : fichier généré de près de 10 Mo, ~7.10⁵ tondeuses rangées par lignes de 1000, chacune avec la séquence LF
     * Scénario : chaque tondeuse tourne vers l'ouest, son F est bloqué par le bord ou par sa voisine déjà arrêtée
     * Résultat : une ligne "x y W" par tondeuse, dans l'ordre du fichier, code de sortie 0, en un temps de l'ordre de la seconde
     */
    @Test
    void run_severalHundredThousandMowers_completesInAboutOneSecond() throws Exception {
        StringBuilder content = new StringBuilder("40075016 40075016\n");
        StringBuilder expected = new StringBuilder();
        for (int i = 0; content.length() + "999 999 N\nLF\n".length() <= MAX_FILE_SIZE; i++) {
            String position = (i % ROW_WIDTH) + " " + (i / ROW_WIDTH);
            content.append(position).append(" N\nLF\n");
            expected.append(position).append(" W\n");
        }
        Path input = write(content.toString());

        Duration duration = timedRun(input);

        assertThat(expected.length()).isGreaterThan(5_000_000);
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo(expected.toString());
        assertThat(duration).isLessThan(MAX_DURATION);
    }

    private Duration timedRun(Path input) throws IOException {
        MowerOutputWriter mowerOutputWriter = new MowerOutputWriter(stdout);
        MowerCli cli = new MowerCli(new MowLawnUseCase(new MowingPlanFileReader(), mowerOutputWriter), mowerOutputWriter);
        long start = System.nanoTime();
        int exitCode = cli.run(new String[]{input.toString()});
        Duration duration = Duration.ofNanos(System.nanoTime() - start);
        assertThat(exitCode).isZero();
        return duration;
    }

    private Path write(String content) throws IOException {
        Path file = tempDir.resolve("input.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        assertThat(Files.size(file)).isLessThanOrEqualTo(MAX_FILE_SIZE).isGreaterThan(MAX_FILE_SIZE - 20L);
        return file;
    }
}
