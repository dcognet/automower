# Architecture du code

Vue du code livré, à mettre à jour dans la PR de chaque US qui ajoute ou déplace une classe. La conception cible et ses arbitrages sont dans [`plans/tondeuse.tech.md`](plans/tondeuse.tech.md).

## Couches

Hexagonal à 3 couches (§ 1 de `conventions/java.md`) :

- **`domain`** : modèle immuable et règles métier. Pur, sans I/O ni framework, ce que vérifie `DomainPurityTest` (ArchUnit).
- **`application`** : `MowLawnUseCase` enchaîne lecture, simulation et rendu.
- **`infrastructure`** : adapters concrets, sans interface tant qu'il n'existe qu'une implémentation. `file` lit et analyse le fichier d'entrée, `cli` porte le point d'entrée et le rendu sur stdout.

## Vue des classes

```mermaid
flowchart LR
  subgraph infra_cli[infrastructure.cli]
    MowerCli
    MowerOutputWriter
    MowerFormatter
    ErrorFormatter
  end
  subgraph app[application]
    MowLawnUseCase
  end
  subgraph infra_file[infrastructure.file]
    MowingPlanFileReader
    LineSplitter
    MowingPlanFileMapper
  end
  subgraph domain[domain]
    MowingPlan
    OccupancyGrid
    Lawn
    MowerProgram
    StartingPositions
    Mower
    Position
    Orientation
    Instruction
    BusinessValidationException
    BusinessErrorCode
  end

  MowerCli -->|"mow(path)"| MowLawnUseCase
  MowLawnUseCase -->|"1. read(path)"| MowingPlanFileReader
  MowingPlanFileReader -->|"bytes UTF-8"| LineSplitter
  MowingPlanFileReader -->|"lignes"| MowingPlanFileMapper
  MowingPlanFileMapper -->|"construit"| MowingPlan
  MowingPlanFileMapper -->|"add(départ), ligne par ligne"| StartingPositions
  StartingPositions --> Lawn
  MowLawnUseCase -->|"2. run()"| MowingPlan
  MowLawnUseCase -->|"3. write(mower), une fois par tondeuse"| MowerOutputWriter
  MowerOutputWriter --> MowerFormatter
  MowerCli -.->|"writeError(e), code 1"| MowerOutputWriter
  MowerOutputWriter --> ErrorFormatter

  MowingPlan --> Lawn
  MowingPlan -->|"crée avec les cases de départ"| OccupancyGrid
  OccupancyGrid --> Lawn
  MowingPlan -->|"1..n, ordre du fichier"| MowerProgram
  MowerProgram -->|"runOn(grid)"| Mower
  Mower -->|"isFree(cible)"| OccupancyGrid
  Mower --> Position
  Mower --> Orientation
  MowerProgram -.->|"fromCode à la construction"| Instruction
  Orientation -.->|"code inconnu"| BusinessValidationException
  Instruction -.->|"code inconnu"| BusinessValidationException
  MowerProgram -.->|"séquence vide"| BusinessValidationException
  Lawn -.->|"dimension hors bornes, pelouse 0 0"| BusinessValidationException
  StartingPositions -.->|"départ hors pelouse ou en doublon"| BusinessValidationException
  MowingPlanFileMapper -.->|"structure et format de ligne, atLine(n)"| BusinessValidationException
  BusinessValidationException --> BusinessErrorCode
```

## Flux d'exécution

1. `MowerCli` reçoit le chemin du fichier et appelle `MowLawnUseCase.mow`.
2. `MowingPlanFileReader` lit les octets, les décode en UTF-8, et `LineSplitter` découpe les lignes sur `\n` (un `\r` qui précède est retiré, un `\r` isolé reste dans la ligne ; un unique saut de ligne final ne produit pas de ligne vide, un second en produit une).
3. `MowingPlanFileMapper` traduit les lignes en `MowingPlan` : la pelouse, puis un `MowerProgram` par couple de lignes (position, instructions), dans l'ordre du fichier. C'est le seul endroit qui connaît le format texte. Avant d'analyser une ligne, il en vérifie la structure : un BOM (U+FEFF) en tête de la ligne 1, une ligne vide ou un `\r` restant sont rejetés sur cette ligne. Une ligne d'instructions vide est donc signalée comme ligne vide (RM-21) : le refus d'une séquence vide par `MowerProgram` (RM-20) reste un invariant du domaine. Les lignes de pelouse et de position sont reconnues par une regex ancrée (valeurs séparées par une seule espace U+0020, coordonnées `0|[1-9][0-9]{0,8}` en chiffres ASCII, vérifiées avant toute conversion en `int`) ; l'orientation et la ligne d'instructions sont validées par le domaine (`Orientation.fromCode`, `MowerProgram`). Le constructeur de `Lawn` borne chaque dimension à `0..40075016` et refuse la pelouse `0 0`. Chaque position de départ est confiée, au moment où sa ligne est analysée, à `StartingPositions` (un `HashSet<Position>`, O(1) moyen par tondeuse), qui la rejette si elle est hors pelouse ou déjà prise : l'erreur garde ainsi le numéro de sa ligne et l'ordre des lignes. Les constructeurs et les `fromCode` du domaine portent la validation fonctionnelle : le plan obtenu est entièrement valide. L'analyse fait un seul passage dans l'ordre des lignes et s'arrête à la première erreur ; une `BusinessValidationException` levée pendant l'analyse d'une ligne y reçoit le numéro de cette ligne (`atLine`). Une ligne attendue mais absente (tondeuse, instructions) est signalée avec le numéro de la ligne attendue.
4. `MowingPlan.run()` crée une `OccupancyGrid` (grille creuse : un `HashSet<Position>`, la pelouse n'est jamais matérialisée) avec toutes les cases de départ, puis exécute les tondeuses l'une après l'autre : la suivante ne bouge qu'une fois la précédente arrêtée. Au démarrage, une tondeuse libère sa case de départ ; à l'arrêt, elle occupe sa case finale. Un `F` vers une case hors pelouse ou occupée est ignoré ; la simulation ne lève aucune erreur.
5. `MowerOutputWriter` écrit `X Y O\n` en UTF-8 sur stdout pour chaque tondeuse, dans l'ordre du fichier, et `MowerCli` rend le code de sortie `0`.

En cas d'erreur, `MowerCli` est le seul à traduire la `BusinessValidationException` : `ErrorFormatter` produit `ERROR line <n>: <description>` (ou `ERROR: <description>` sans numéro de ligne), en échappant les caractères invisibles cités (`\\`, `\t`, `\r`, `\n`, `\uXXXX` pour les autres caractères de contrôle, de format et séparateurs de ligne) pour que le message tienne sur une ligne lisible, écrit par `MowerOutputWriter`, et le code de sortie est `1`. Comme le plan est validé en entier avant la simulation, aucune position finale n'a été écrite.

Une nouvelle source d'entrée (API, Kafka) apporterait son propre adapter et son mapper vers le même `MowingPlan`, sans toucher au domaine ni au cas d'usage.
