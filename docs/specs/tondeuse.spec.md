---
id: SPEC-tondeuse
title: Pilotage de tondeuses automatiques sur une pelouse en grille
status: validated
date: 2026-11-21
---

## Contexte

Cette spec cadre un **exercice de test technique**. L'énoncé met en scène une société X qui développe une tondeuse automatique pour des surfaces carrées. Le format du fichier donne pourtant deux dimensions indépendantes : la spec accepte donc aussi les pelouses rectangulaires (RM-14). Pour simplifier la navigation, la pelouse est découpée en grille. Une tondeuse s'y repère par des coordonnées `(X, Y)` et une orientation cardinale (`N`, `E`, `S`, `W`).

Il faut pouvoir programmer une ou plusieurs tondeuses à partir d'un fichier d'entrée, et connaître la position et l'orientation finales de chacune une fois ses instructions exécutées.

Il n'y a pas de besoin produit réel derrière : la valeur attendue est la justesse du comportement et la façon de traiter les cas que l'énoncé ne couvre pas.

Source : `docs/enonce-tondeuse.md`, traduction fidèle de `docs/Test tondeuse 3.pdf` (vérifiée contre le texte du PDF). Les comportements absents de l'énoncé ont été tranchés le 2026-11-21 (RM-13 à RM-32), puis précisés lors du découpage en US (RM-20, RM-23, RM-27).

## Objectifs

S'agissant d'un test technique, il n'y a pas d'objectif business mesurable. Les objectifs portent sur le comportement attendu.

- **O-1** : à partir d'un fichier conforme, produire la position finale et l'orientation de chaque tondeuse, dans l'ordre du fichier.
  - Critère d'acceptation : l'exemple de référence (RM-12) donne exactement la sortie attendue.
- **O-2** : une tondeuse ne sort jamais de la pelouse et n'entre jamais sur une case occupée par une autre tondeuse.
  - Critère d'acceptation : pour toute séquence, la position finale vérifie `0 ≤ X ≤ Xmax` et `0 ≤ Y ≤ Ymax`, et ne coïncide avec la position d'aucune tondeuse déjà arrêtée.
- **O-3** : un fichier non conforme est rejeté en entier, avec une erreur explicite qui indique le numéro de ligne en cause.
  - Critère d'acceptation : aucun résultat partiel n'est produit quand le fichier contient une erreur.

## Périmètre fonctionnel

- Lecture d'un fichier d'entrée qui décrit une pelouse rectangulaire (carrée comprise), puis une suite de tondeuses (RM-01 et RM-04).
- Définition de la pelouse par son coin supérieur droit. Le coin inférieur gauche est `(0, 0)`.
- Placement de chaque tondeuse à une position et une orientation de départ.
- Pilotage d'une tondeuse par une séquence d'instructions `L`, `R` et `F`.
- Blocage d'une tondeuse en bord de pelouse ou devant une case occupée par une autre tondeuse.
- Exécution séquentielle de plusieurs tondeuses, dans l'ordre du fichier.
- Validation complète du fichier avant toute exécution, avec rejet explicite des fichiers non conformes.
- Restitution, sur la sortie standard, des positions finales ou de l'erreur rencontrée.

## Hors scope

- **Calcul automatique d'un parcours couvrant toute la pelouse.** On exécute les instructions fournies : on ne les génère pas et on ne vérifie pas que toute la surface est couverte *(confirmé)*.
- **Traitement partiel.** Un fichier en erreur ne produit aucune position finale, même pour les tondeuses valides (RM-13).
- **Tolérance de format.** Le fichier n'est ni normalisé ni corrigé : pas de minuscules, pas d'espaces en trop, pas de lignes vides (RM-21).
- **Suivi des cases tondues.** Aucune mémoire de l'herbe déjà coupée, aucun taux de couverture.
- **Exécution simultanée.** Plusieurs tondeuses ne se déplacent jamais en même temps (RM-10).
- **Pilotage en temps réel.** Pas de commande interactive ni de reprise après une interruption.
- **Obstacles autres que les tondeuses, pentes, batterie, météo.**
- **Déplacement en diagonale, recul, demi-tour en une instruction.** Seules `L`, `R` et `F` existent.

## Règles métier

### Pelouse

- **RM-01** : la première ligne du fichier donne les coordonnées du coin supérieur droit de la pelouse, au format `Xmax Ymax`. Le coin inférieur gauche est toujours `(0, 0)`.
- **RM-02** : les bornes sont inclusives. Une case `(x, y)` est sur la pelouse si `0 ≤ x ≤ Xmax` et `0 ≤ y ≤ Ymax`.
  - Exemple : une pelouse `5 5` compte 6 × 6 = 36 cases. Le résultat attendu de l'énoncé (tondeuse 2 finit en `5 1`) n'est possible que si `x = 5` est sur la pelouse.

### Tondeuse

- **RM-03** : une tondeuse a une position `(X, Y)` et une orientation parmi `N`, `E`, `S`, `W`. Exemple : `0 0 N` = coin inférieur gauche, orientée vers le nord.
- **RM-04** : après la première ligne, chaque tondeuse est décrite par deux lignes consécutives :
  1. sa position et son orientation de départ, au format `X Y O`, séparés par une seule espace ;
  2. sa séquence d'instructions : une suite de caractères sans espace.

### Instructions

- **RM-05** : `L` fait pivoter la tondeuse de 90° vers la gauche, sans la déplacer : `N → W → S → E → N`.
- **RM-06** : `R` fait pivoter la tondeuse de 90° vers la droite, sans la déplacer : `N → E → S → W → N`.
- **RM-07** : `F` fait avancer la tondeuse d'une case dans la direction à laquelle elle fait face, sans changer son orientation. La case au nord de `(X, Y)` est `(X, Y+1)`.

  | Orientation | Case visée |
  |---|---|
  | `N` | `(X, Y+1)` |
  | `E` | `(X+1, Y)` |
  | `S` | `(X, Y-1)` |
  | `W` | `(X-1, Y)` |

- **RM-08** : si la case visée par `F` est hors de la pelouse (RM-02) ou occupée par une autre tondeuse (RM-15 et RM-25), la tondeuse ne bouge pas, garde son orientation et passe à l'instruction suivante. Ce n'est pas une erreur.
  - Exemple (bord) : pelouse `5 5`, tondeuse en `5 3 E`, instructions `FL`. Le `F` est ignoré, puis `L` donne `5 3 N`.
- **RM-09** : les instructions s'exécutent une par une, dans l'ordre de la séquence.

### Exécution et restitution

- **RM-10** : les tondeuses se déplacent séquentiellement, dans l'ordre du fichier. La tondeuse n+1 ne bouge qu'une fois que la tondeuse n a exécuté toute sa séquence.
- **RM-11** : quand une tondeuse a terminé, elle donne sa position finale et son orientation au format `X Y O`. Il y a une ligne par tondeuse, dans l'ordre du fichier, sur la sortie standard. Chaque ligne de sortie se termine par `\n`, quelle que soit la fin de ligne du fichier d'entrée, et la dernière position aussi.
- **RM-12** : exemple de référence, qui sert de test d'acceptation.

  Entrée :
  ```
  5 5
  1 2 N
  LFLFLFLFF
  3 3 E
  FFRFFRFRRF
  ```
  Sortie attendue :
  ```
  1 3 N
  5 1 E
  ```
  Déroulé de la tondeuse 2 : `3 3 E` → F `4 3` → F `5 3` → R `S` → F `5 2` → F `5 1` → R `W` → F `4 1` → R `N` → R `E` → F `5 1` → **`5 1 E`**. Aucune collision ne change ce résultat : la tondeuse 1 ne passe jamais par `3 3` (case de départ de la tondeuse 2, RM-25), et la tondeuse 2 ne passe jamais par `1 3` (case d'arrêt de la tondeuse 1, RM-15).

### Validation et erreurs

- **RM-13** : le fichier est validé **en entier avant toute exécution**. À la moindre non-conformité, il est rejeté : aucune tondeuse n'est exécutée et aucune position finale n'est produite.
- **RM-14** : la pelouse est un rectangle, et `Xmax` et `Ymax` sont indépendants. Un carré (`Xmax = Ymax`) n'en est qu'un cas particulier. L'énoncé parle de « surfaces carrées », mais son format donne deux dimensions : on accepte donc les deux.
  - Exemple : `5 3` est accepté : la pelouse fait 6 × 4 = 24 cases. Une tondeuse `0 0 E` avec la séquence `FFFFFFL` avance jusqu'en `5 0`, son 6ᵉ `F` est bloqué par le bord, puis `L` donne `5 0 N`.
- **RM-15** : une tondeuse arrêtée occupe sa case. Pour les tondeuses suivantes, cette case se comporte comme un bord (RM-08).
  - Exemple : pelouse `5 5`, tondeuse 1 `1 1 N` avec la séquence `L` (elle s'arrête en `1 1 W`), tondeuse 2 `0 1 E` avec la séquence `FL`. Le `F` vise `1 1`, qui est occupée : il est ignoré. Puis `L` donne `0 1 N`.
- **RM-16** : une instruction autre que `L`, `R` ou `F` est une erreur.
  - Exemple : `LFA` est rejeté, tout comme `lf` ou `L F`.
- **RM-17** : une orientation autre que `N`, `E`, `S` ou `W` est une erreur, minuscules comprises (`n` est rejeté).
- **RM-18** : une position de départ hors de la pelouse est une erreur.
  - Exemple : pelouse `5 5`, tondeuse `6 2 N` → rejet.
- **RM-19** : une coordonnée négative ou non numérique, dans la pelouse comme dans une position de départ, est une erreur. Ce qui compte comme « numérique » est défini par RM-28.
  - Exemple : `-1 -1` ou `5 A` → rejet.
- **RM-20** : sont des erreurs explicites :
  - une séquence d'instructions vide ;
  - une pelouse `0 0` (une seule case). Une bande d'une seule ligne ou d'une seule colonne (`5 0`, `0 3`) reste acceptée ;
  - un fichier qui définit la pelouse mais aucune tondeuse. L'erreur est signalée sur la ligne 2, la ligne attendue (comme RM-22) ;
  - un fichier vide (0 octet) ou absent. C'est une erreur sur le fichier entier, sans numéro de ligne ;
  - un fichier illisible (droits refusés) ou un chemin qui désigne un dossier, traités comme un fichier absent.
- **RM-21** : pas de tolérance de format. Les espaces en trop et les lignes vides sont des erreurs.
  - Les lignes se terminent par `\n` (Unix) ou `\r\n` (Windows) : les deux sont acceptés, y compris mélangés dans un même fichier. Toute autre fin de ligne est une erreur, par exemple `\r` seul.
  - Un unique saut de ligne (`\n` ou `\r\n`) en fin de fichier n'est pas une ligne vide : il est accepté.
- **RM-22** : une tondeuse sans sa ligne d'instructions (nombre de lignes incohérent) est une erreur. Le numéro signalé est celui de la ligne attendue, c'est-à-dire la ligne qui suit la position.
  - Exemple : fichier `5 5` / `1 2 N` (2 lignes) → erreur ligne 3.
- **RM-23** : une erreur est signalée sur la sortie standard, avec le **numéro de la ligne** du fichier en cause. Les lignes sont numérotées à partir de 1 (la ligne de la pelouse est la ligne 1). Si le fichier contient plusieurs erreurs, seule **la première** (dans l'ordre des lignes) est signalée. Exceptions : les erreurs qui portent sur le fichier entier n'ont pas de numéro de ligne. Ce sont la taille (RM-27), l'encodage (RM-30) et le fichier absent, vide, illisible ou qui désigne un dossier (RM-20).
- **RM-24** : une case mesure 1 m × 1 m. Chaque dimension de la pelouse est limitée séparément par la circonférence de la Terre à l'équateur, 40 075 017 m. On a donc `Xmax ≤ 40 075 016` et `Ymax ≤ 40 075 016`, soit au plus 40 075 017 cases par axe. Au-delà, c'est une erreur. La surface totale n'est pas limitée.
  - Exemple : `40075016 0` et `40075016 40075016` sont acceptés. `40075017 5` est rejeté.
  - La longueur d'une séquence d'instructions n'est limitée que par la taille maximale du fichier (RM-27).
- **RM-25** : toutes les tondeuses sont déployées sur la pelouse dès le départ. Une tondeuse pas encore exécutée occupe sa case de départ, et cette case bloque les tondeuses précédentes comme un bord (RM-08). Une tondeuse ne peut donc jamais s'arrêter sur la case de départ d'une tondeuse suivante. Avec RM-15, cela garantit qu'une tondeuse ne démarre jamais sur une case occupée.
  - Exemple : pelouse `5 5`, tondeuse 1 `0 0 E` avec la séquence `FF`, tondeuse 2 `2 0 N` avec la séquence `L`. Le premier `F` mène la tondeuse 1 en `1 0`, le second vise `2 0`, occupée par la tondeuse 2 : il est ignoré. Sortie : `1 0 E`, puis `2 0 W`.
- **RM-26** : deux tondeuses ne peuvent pas avoir la même position de départ : c'est une erreur.
  - Exemple : `1 2 N` puis `1 2 E` → rejet, signalé sur la ligne de la seconde tondeuse.
- **RM-27** : le fichier d'entrée ne doit pas dépasser **10 Mo, soit 10 000 000 octets**. Au-delà, il est rejeté avant toute lecture de son contenu. Le message d'erreur donne la taille du fichier et la limite, sans numéro de ligne (exception à RM-23).
  - Exemple : un fichier de 10 000 000 octets est accepté, un fichier de 10 000 001 octets est rejeté.
  - Cette vérification passe avant toutes les autres : un fichier trop gros est rejeté même s'il contient aussi des erreurs de format.
  - Il arrive que la taille réelle ne soit connue qu'à la lecture : fichier qui grossit pendant le traitement, ou fichier spécial qui annonce une taille de 0. Le fichier est alors rejeté dès que plus de 10 000 000 octets ont été lus, et le message ne donne que la limite : `ERROR: file size exceeds limit of 10000000 bytes`.
- **RM-28** : la lecture des coordonnées est stricte. Une coordonnée valide est soit `0`, soit un chiffre de `1` à `9` suivi de chiffres de `0` à `9`, uniquement en chiffres ASCII. Toute autre écriture est une erreur, même si on pourrait l'interpréter comme un entier.

  | Valeur | Verdict |
  |---|---|
  | `0`, `5`, `12` | ✅ accepté |
  | `05`, `00` (zéros en tête) | ❌ rejeté |
  | `+3` (signe plus) | ❌ rejeté |
  | `-0`, `-1` (signe moins) | ❌ rejeté |
  | `５`, `٥` (chiffres non ASCII) | ❌ rejeté |
  | `1.5`, `1e3`, `abc` | ❌ rejeté |
- **RM-29** : les valeurs d'une ligne sont séparées par **exactement une espace ASCII** (U+0020). Tout autre séparateur est une erreur.
  - Exemple : `1 2 N` est accepté. `1\t2 N` (tabulation), `1 2 N` avec une espace insécable et `1  2 N` (deux espaces) sont rejetés.
- **RM-30** : le fichier est lu en **UTF-8**. Un fichier qui n'est pas de l'UTF-8 valide est rejeté, sans numéro de ligne.
  - Un caractère non ASCII mais valide en UTF-8 (`é`, `５`…) n'est pas une erreur d'encodage. C'est une erreur de format, signalée avec son numéro de ligne par les autres règles.
  - Un BOM UTF-8 en début de fichier est rejeté : c'est une erreur sur la ligne 1.
- **RM-31** : la ligne de la pelouse contient exactement 2 valeurs, et une ligne de position exactement 3. Tout autre nombre de valeurs est une erreur.
  - Exemple : `5`, `5 5 5`, `1 2`, `1 2 N X` et `1 2N` sont rejetés.
- **RM-32** : format d'un message d'erreur, en anglais, sur une seule ligne terminée par `\n` :
  - erreur rattachée à une ligne : `ERROR line <n>: <description>`. Exemple : `ERROR line 4: invalid orientation 'X'` ;
  - erreur sur le fichier entier (RM-20, RM-27, RM-30) : `ERROR: <description>`. Exemple : `ERROR: file size 12000000 bytes exceeds limit of 10000000 bytes`.
  - Le préfixe `ERROR` distingue une erreur d'une position finale. Une position commence toujours par un chiffre.
