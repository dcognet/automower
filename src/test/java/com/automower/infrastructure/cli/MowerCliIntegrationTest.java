package com.automower.infrastructure.cli;

import com.automower.application.MowLawnUseCase;
import com.automower.infrastructure.file.MowingPlanFileReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MowerCliIntegrationTest {

    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final MowerCli cli = new MowerCli(new MowLawnUseCase(new MowingPlanFileReader(), new MowerOutputWriter(stdout)));

    /**
     * Entrée : exemple de référence complet (5 5 / 1 2 N / LFLFLFLFF / 3 3 E / FFRFFRFRRF)
     * Scénario : deux tondeuses exécutées l'une après l'autre, dans l'ordre du fichier
     * Résultat : stdout vaut exactement "1 3 N\n5 1 E\n", code de sortie 0
     */
    @Test
    void run_referenceExample_printsOneLinePerMowerInFileOrder() throws Exception {
        int exitCode = cli.run(new String[]{fixture("reference-example.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toByteArray()).isEqualTo("1 3 N\n5 1 E\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Entrée : pelouse 5 5, tondeuse 5 3 E, séquence FL
     * Scénario : le F ferait sortir la tondeuse par le bord est, il est ignoré sans erreur
     * Résultat : "5 3 N\n", code de sortie 0
     */
    @Test
    void run_forwardBlockedByEdge_ignoresInstructionAndContinues() throws Exception {
        int exitCode = cli.run(new String[]{fixture("blocked-by-edge.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("5 3 N\n");
    }

    /**
     * Entrée : pelouse rectangulaire 5 3, tondeuse 0 0 E, séquence FFFFFFL
     * Scénario : la tondeuse traverse la largeur, le 6e F est bloqué par le bord
     * Résultat : "5 0 N\n", code de sortie 0
     */
    @Test
    void run_rectangularLawn_usesIndependentBounds() throws Exception {
        int exitCode = cli.run(new String[]{fixture("rectangular-lawn.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("5 0 N\n");
    }

    private static String fixture(String name) throws URISyntaxException, IOException {
        URL resource = MowerCliIntegrationTest.class.getResource("/inputs/" + name);
        if (resource == null) {
            throw new IOException("Missing fixture " + name);
        }
        return Path.of(resource.toURI()).toString();
    }
}
