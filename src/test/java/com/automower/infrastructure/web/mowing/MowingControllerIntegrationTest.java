package com.automower.infrastructure.web.mowing;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MowingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    /**
     * Réception : exemple de référence (pelouse 5 5, 1 2 N / LFLFLFLFF, 3 3 E / FFRFFRFRRF)
     * Scénario : deux tondeuses exécutées l'une après l'autre, dans l'ordre de la requête
     * Résultat : 200, items = [1 3 N, 5 1 E]
     */
    @Test
    void mow_referenceExample_returnsFinalMowersInRequestOrder() {
        String body = """
                {
                  "lawn": {"maxX": 5, "maxY": 5},
                  "mowers": [
                    {"x": 1, "y": 2, "orientation": "N", "instructions": "LFLFLFLFF"},
                    {"x": 3, "y": 3, "orientation": "E", "instructions": "FFRFFRFRRF"}
                  ]
                }
                """;

        post(body)
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("items.x", contains(1, 5))
                .body("items.y", contains(3, 1))
                .body("items.orientation", contains("N", "E"));
    }

    /**
     * Réception : pelouse 5 5, tondeuse 1 en 1 1 N avec L, tondeuse 2 en 0 1 E avec FL
     * Scénario : la tondeuse 1 s'arrête en 1 1, sa case bloque le F de la tondeuse 2 comme un bord
     * Résultat : 200, items = [1 1 W, 0 1 N]
     */
    @Test
    void mow_forwardOntoStoppedMower_ignoresInstructionAndContinues() {
        String body = """
                {
                  "lawn": {"maxX": 5, "maxY": 5},
                  "mowers": [
                    {"x": 1, "y": 1, "orientation": "N", "instructions": "L"},
                    {"x": 0, "y": 1, "orientation": "E", "instructions": "FL"}
                  ]
                }
                """;

        post(body)
                .statusCode(200)
                .body("items.x", contains(1, 0))
                .body("items.y", contains(1, 1))
                .body("items.orientation", contains("W", "N"));
    }

    /**
     * Réception : une requête invalide pour le domaine
     * Scénario : la règle métier violée est traduite en ProblemDetail
     * Résultat : 400, problem+json avec le code machine-readable de la règle
     */
    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            -1       | 5 | 1 | 2 | N | LFF    | 3 | 3 | E | FFRFFR | INVALID_LAWN_DIMENSION
            40075017 | 5 | 1 | 2 | N | LFF    | 3 | 3 | E | FFRFFR | INVALID_LAWN_DIMENSION
            0        | 0 | 0 | 0 | N | LFF    | 0 | 0 | E | FFRFFR | SINGLE_CELL_LAWN
            5        | 5 | 6 | 2 | N | LFF    | 3 | 3 | E | FFRFFR | START_OUTSIDE_LAWN
            5        | 5 | 1 | 2 | N | LFF    | 1 | 2 | E | FFRFFR | DUPLICATE_START
            5        | 5 | 1 | 2 | N | LFF    | 3 | 3 | X | FFRFFR | INVALID_ORIENTATION
            5        | 5 | 1 | 2 | N | LFF    | 3 | 3 | E | FFRFFA | INVALID_INSTRUCTION
            5        | 5 | 1 | 2 | N | ''     | 3 | 3 | E | FFRFFR | EMPTY_INSTRUCTIONS
            """)
    void mow_businessRuleViolated_returnsProblemWithCode(
            int maxX,
            int maxY,
            int x1,
            int y1,
            String orientation1,
            String instructions1,
            int x2,
            int y2,
            String orientation2,
            String instructions2,
            String expectedCode
    ) {
        String body = """
                {
                  "lawn": {"maxX": %d, "maxY": %d},
                  "mowers": [
                    {"x": %d, "y": %d, "orientation": "%s", "instructions": "%s"},
                    {"x": %d, "y": %d, "orientation": "%s", "instructions": "%s"}
                  ]
                }
                """.formatted(maxX, maxY, x1, y1, orientation1, instructions1, x2, y2, orientation2, instructions2);

        post(body)
                .statusCode(400)
                .contentType("application/problem+json")
                .body("code", equalTo(expectedCode))
                .body("status", equalTo(400));
    }

    /**
     * Réception : pelouse valide, liste de tondeuses vide
     * Scénario : un plan sans tondeuse n'a rien à tondre
     * Résultat : 400, code MISSING_MOWER
     */
    @Test
    void mow_noMower_returnsMissingMower() {
        post("""
                {"lawn": {"maxX": 5, "maxY": 5}, "mowers": []}
                """)
                .statusCode(400)
                .body("code", equalTo("MISSING_MOWER"));
    }

    /**
     * Réception : corps illisible (JSON invalide, champ absent, null, mal typé, tondeuse null)
     * Scénario : la désérialisation échoue avant tout appel au domaine
     * Résultat : 400, code MALFORMED_REQUEST
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "not json",
            "",
            "{\"mowers\": [{\"x\": 1, \"y\": 2, \"orientation\": \"N\", \"instructions\": \"F\"}]}",
            "{\"lawn\": {\"maxX\": 5}, \"mowers\": [{\"x\": 1, \"y\": 2, \"orientation\": \"N\", \"instructions\": \"F\"}]}",
            "{\"lawn\": {\"maxX\": 5, \"maxY\": null}, \"mowers\": [{\"x\": 1, \"y\": 2, \"orientation\": \"N\", \"instructions\": \"F\"}]}",
            "{\"lawn\": {\"maxX\": 5, \"maxY\": 5}, \"mowers\": [{\"x\": 1, \"y\": 2, \"instructions\": \"F\"}]}",
            "{\"lawn\": {\"maxX\": 5, \"maxY\": 5}, \"mowers\": [{\"x\": 1, \"y\": 2, \"orientation\": \"N\", \"instructions\": null}]}",
            "{\"lawn\": {\"maxX\": 5, \"maxY\": 5}, \"mowers\": [{\"x\": \"a\", \"y\": 2, \"orientation\": \"N\", \"instructions\": \"F\"}]}",
            "{\"lawn\": {\"maxX\": 5, \"maxY\": 5}, \"mowers\": null}",
            "{\"lawn\": {\"maxX\": 5, \"maxY\": 5}, \"mowers\": [null]}"
    })
    void mow_unreadableBody_returnsMalformedRequest(String body) {
        post(body)
                .statusCode(400)
                .contentType("application/problem+json")
                .body("code", equalTo("MALFORMED_REQUEST"));
    }

    private static ValidatableResponse post(String body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/mowings")
                .then();
    }
}
