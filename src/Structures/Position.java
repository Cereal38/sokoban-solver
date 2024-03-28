package Structures;

import java.awt.Point;

public class Position extends Point {
  // Takes column (x) and line (y) as parameters

  public Position(int column, int line) {
    super(column, line);
  }

  public Position haut() {
    return new Position(x, y - 1);
  }

  public Position bas() {
    return new Position(x, y + 1);
  }

  public Position gauche() {
    return new Position(x - 1, y);
  }

  public Position droite() {
    return new Position(x + 1, y);
  }

  public int ligne() {
    return y;
  }

  public int colonne() {
    return x;
  }

  public boolean equals(Position p) {
    return x == p.x && y == p.y;
  }

}
