# Gates qualité — Surcharge projet (Java 25 + Maven)

Le skill `self-review` ne fournit pas de surcharge Java : sans ce fichier, il ne retient que les gates de base, sans commande. Ce fichier donne les commandes concrètes pour ce dépôt. L'intention et les seuils restent ceux de `quality-gates-base.md`.

La branche de référence est `origin/main`, pas `origin/master` : les diffs se calculent avec `git diff origin/main...HEAD`.

## Fichiers générés — à exclure

- `target/**` : sortie de build Maven, rapports JaCoCo et PIT compris
- `.self-review/**` : rapports de self-review
- `.idea/**` : configuration IDE

---

## Gate 1 — Build

```bash
mvn -B -q clean test-compile
```

---

## Gate 2 — Tests

```bash
mvn -B clean verify
```

`verify` lance les tests unitaires et d'intégration (Surefire), puis le mutation testing PIT sur `com.automower.domain`, avec un seuil à 100 % (voir Gate 5). Ce gate échoue si un test échoue ou si un mutant du domaine survit.

---

## Gate 3 — Couverture du code nouveau

```bash
mvn -B clean org.jacoco:jacoco-maven-plugin:0.8.16:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.16:report
```

Le rapport est produit dans `target/site/jacoco/` (`jacoco.csv` pour la lecture outillée, `index.html` pour la lecture humaine). Mesurer les lignes nouvelles ou modifiées par la branche. Lignes non couvrables connues, à ne pas compter comme un écart : constructeurs privés `// vide` des classes utilitaires, `MowerCli.main` (qui appelle `System.exit`).

---

## Gate 4 — Audit des dépendances (CVE)

Le jar n'embarque aucune dépendance runtime : toutes les dépendances sont en scope `test`. Vérifier que c'est toujours le cas :

```bash
mvn -B dependency:list -DincludeScope=runtime
```

Résultat attendu : `The following files have been resolved: none`.

Si une dépendance runtime apparaît, lancer l'audit OWASP. Sa première exécution télécharge la base NVD, ce qui prend plusieurs minutes et demande de préférence une clé d'API NVD :

```bash
mvn -B org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7
```

Sans dépendance runtime, marquer le gate `⬜ N/A` en le justifiant.

---

## Gate 5 — Mutation testing du domaine *(prérequis, propre au projet)*

Ce gate est déjà exécuté par la Gate 2 (`mvn verify`). Pour le relancer seul :

```bash
mvn -B test-compile org.pitest:pitest-maven:mutationCoverage
```

**Seuil** : 100 % de mutants tués sur `com.automower.domain.*` (`mutationThreshold` du `pom.xml`). Le rapport est produit dans `target/pit-reports/mutations.xml`. Un mutant survivant se corrige en renforçant les tests, jamais en réduisant `targetClasses` ou en modifiant le code de production pour lui seul (cf. skill `pricing-agent:mutation-coverage`).
