# Test tondeuse

La société X souhaite développer une tondeuse automatique pour des surfaces carrées.

La tondeuse peut être programmée pour parcourir l'intégralité de la surface. La position de la tondeuse est représentée par des coordonnées (X, Y) et par un caractère indiquant son orientation selon la notation cardinale (N, E, W, S). La pelouse est découpée en grille pour simplifier la navigation.

Par exemple, la position peut être `0, 0, N`, ce qui signifie que la tondeuse se trouve dans le coin inférieur gauche de la pelouse et qu'elle est orientée vers le nord.

## Pilotage

Pour piloter la tondeuse, on lui envoie une simple séquence de caractères. Les caractères possibles sont `L`, `R` et `F` :

- `L` et `R` font pivoter la tondeuse de 90° vers la gauche ou vers la droite, sans la déplacer.
- `F` fait avancer la tondeuse d'une case dans la direction à laquelle elle fait face, sans changer son orientation.

Si la position après le déplacement se trouve en dehors de la pelouse, la tondeuse ne bouge pas, conserve son orientation et passe à la commande suivante.

On considère que la case directement au nord de (X, Y) est (X, Y+1).

## Fichier d'entrée

Pour programmer la tondeuse, on peut lui fournir un fichier d'entrée construit de la manière suivante :

- La première ligne correspond aux coordonnées du coin supérieur droit de la pelouse. Le coin inférieur gauche est supposé être (0, 0).
- La suite du fichier permet de piloter plusieurs tondeuses déployées sur la pelouse. Chaque tondeuse est décrite par deux lignes :
  1. La première ligne donne la position de départ et l'orientation de la tondeuse, sous la forme `X Y O`, X et Y étant la position et O l'orientation.
  2. La seconde ligne donne les instructions permettant à la tondeuse de parcourir la pelouse. Les instructions sont une suite de caractères sans espaces.

Les tondeuses se déplacent séquentiellement : la deuxième tondeuse ne bouge que lorsque la première a entièrement exécuté sa série d'instructions.

Lorsqu'une tondeuse a terminé, elle donne sa position finale et son orientation.

## Exemple

Fichier d'entrée :

```
5 5
1 2 N
LFLFLFLFF
3 3 E
FFRFFRFRRF
```

Résultat :

```
1 3 N
5 1 E
```
