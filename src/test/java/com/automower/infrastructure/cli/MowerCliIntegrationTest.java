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
    private final MowerCli cli = newCli();

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

    /**
     * Entrée : pelouse 5 5, tondeuse 1 en 1 1 N avec L, tondeuse 2 en 0 1 E avec FL
     * Scénario : la tondeuse 1 s'arrête en 1 1, sa case bloque le F de la tondeuse 2 comme un bord
     * Résultat : "1 1 W\n0 1 N\n", code de sortie 0
     */
    @Test
    void run_forwardOntoStoppedMower_ignoresInstructionAndContinues() throws Exception {
        int exitCode = cli.run(new String[]{fixture("blocked-by-stopped-mower.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("1 1 W\n0 1 N\n");
    }

    /**
     * Entrée : pelouse 5 5, tondeuse 1 en 0 0 E avec FF, tondeuse 2 en 2 0 N avec L
     * Scénario : la tondeuse 2 n'a pas encore démarré, sa case de départ bloque le second F de la tondeuse 1
     * Résultat : "1 0 E\n2 0 W\n", code de sortie 0
     */
    @Test
    void run_forwardOntoMowerNotStartedYet_ignoresInstructionAndContinues() throws Exception {
        int exitCode = cli.run(new String[]{fixture("blocked-by-waiting-mower.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("1 0 E\n2 0 W\n");
    }

    /**
     * Entrée : pelouse aux bornes maximales 40075016 40075016, trois tondeuses dont une au coin supérieur droit
     * Scénario : la tondeuse 2 est bloquée par la tondeuse 1, la tondeuse 3 par les bords maximaux ; seules les cases occupées sont suivies
     * Résultat : positions finales attendues, code de sortie 0, sans erreur mémoire
     */
    @Test
    void run_lawnAtMaximumBounds_completesWithoutMaterializingLawn() throws Exception {
        int exitCode = cli.run(new String[]{fixture("maximum-bounds-lawn.txt")});

        assertThat(exitCode).isZero();
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("0 2 N\n0 3 S\n40075016 40075016 N\n");
    }

    /**
     * Entrée : exemple de référence dont la seconde tondeuse a l'orientation X, en ligne 4
     * Scénario : la première tondeuse est valide, mais le fichier est rejeté en entier avant toute exécution
     * Résultat : stdout vaut exactement "ERROR line 4: invalid orientation 'X'\n", aucune position, code de sortie 1
     */
    @Test
    void run_invalidOrientationAfterValidMower_printsOnlyErrorWithLineNumber() throws Exception {
        int exitCode = cli.run(new String[]{fixture("invalid-orientation.txt")});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toByteArray()).isEqualTo("ERROR line 4: invalid orientation 'X'\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Entrée : exemple de référence dont la séquence de la seconde tondeuse contient A, en ligne 5
     * Scénario : la dernière ligne est non conforme, aucune tondeuse n'est exécutée
     * Résultat : "ERROR line 5: invalid instruction 'A'\n", code de sortie 1
     */
    @Test
    void run_invalidInstructionOnLastLine_printsOnlyErrorWithLineNumber() throws Exception {
        int exitCode = cli.run(new String[]{fixture("invalid-instruction.txt")});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("ERROR line 5: invalid instruction 'A'\n");
    }

    /**
     * Entrée : fichier de 2 lignes, 5 5 / 1 2 N
     * Scénario : la tondeuse n'a pas de ligne d'instructions, l'erreur porte sur la ligne attendue
     * Résultat : "ERROR line 3: missing instruction line\n", code de sortie 1
     */
    @Test
    void run_positionWithoutInstructionLine_reportsExpectedLine() throws Exception {
        int exitCode = cli.run(new String[]{fixture("missing-instruction-line.txt")});

        assertThat(exitCode).isEqualTo(1);
        assertThat(stdout.toString(StandardCharsets.UTF_8)).isEqualTo("ERROR line 3: missing instruction line\n");
    }

    private MowerCli newCli() {
        MowerOutputWriter mowerOutputWriter = new MowerOutputWriter(stdout);
        return new MowerCli(new MowLawnUseCase(new MowingPlanFileReader(), mowerOutputWriter), mowerOutputWriter);
    }

    private static String fixture(String name) throws URISyntaxException, IOException {
        URL resource = MowerCliIntegrationTest.class.getResource("/inputs/" + name);
        if (resource == null) {
            throw new IOException("Missing fixture " + name);
        }
        return Path.of(resource.toURI()).toString();
    }
}
