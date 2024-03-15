
// X = Colonne
// Y = Ligne

package Modele;

import java.awt.Point;

import Global.Configuration;
import Structures.Sequence;

class IASolver extends IA {
  // Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
  final static int VERT = 0x00CC00;
  final static int MARRON = 0xBB7755;
  CasesAccessibles cases = null;

  // Cases accessibles par le pousseur
  class CasesAccessibles {
    Point[] position;
    int taille, nb_eleme;

    public CasesAccessibles(int x, int y) {
      taille = 1000;
      nb_eleme = 1;
      ajouterCasesAccessibles(x, y);
    }

    private void checkAndAddPosition(int y, int x) {
      if (existe(y, x) == -1 && niveau.estOccupable(y, x)) {
        // redimensionne();
        position[nb_eleme] = new Point(x, y);
        nb_eleme++;
      }
    }

    public void ajouterCasesAccessibles(int x, int y) {
      // On clear le tableau
      position = new Point[taille];
      // On ajoute la position du pousseur
      position[0] = new Point(x, y);
      nb_eleme = 1;
      int i = 0;
      while (i < nb_eleme) {

        // Affiche le contenu du tableau sur une ligne
        for (int j = 0; j < nb_eleme; j++) {
          System.out.print(position[j].x + " " + position[j].y + " | ");
        }
        System.out.println();

        // On regarde chaque case adjacente si elle n'est pas déjà dans le tableau et si
        // elle est
        // accessible, si oui on l'ajoute
        checkAndAddPosition(position[i].y - 1, position[i].x);
        checkAndAddPosition(position[i].y + 1, position[i].x);
        checkAndAddPosition(position[i].y, position[i].x - 1);
        checkAndAddPosition(position[i].y, position[i].x + 1);

        i++;
      }
    }

    // Renvoie l'indice de l'élément si il existe, -1 sinon
    public int existe(int l, int c) {
      for (int i = 0; i < nb_eleme; i++) {
        if (position[i].getX() == c && position[i].getY() == l)
          return i;
      }
      return -1;
    }

    // Double la taille du tableau si il est complet
    protected void redimensionne() {
      if (taille >= nb_eleme) {
        taille = taille * 2;
        Point[] nouveau = new Point[taille];
        for (int i = 0; i < nb_eleme; i++)
          nouveau[i] = position[i];
        position = nouveau;
      }
    }

    public boolean haut(int i) {
      return estAccessible((int) position[i].getX() - 1, (int) position[i].getY());
    }

    public boolean bas(int i) {
      return estAccessible((int) position[i].getX() + 1, (int) position[i].getY());
    }

    public boolean gauche(int i) {
      return estAccessible((int) position[i].getX(), (int) position[i].getY() - 1);
    }

    public boolean droite(int i) {
      return estAccessible((int) position[i].getX(), (int) position[i].getY() + 1);
    }

    // Indique si la case demandé est accessible
    private boolean estAccessible(int l, int c) {
      if (l < 0 || l >= niveau.lignes() || c < 0 || c >= niveau.colonnes() || !niveau.estOccupable(l, c)) {
        return false;
      }
      return true;
    }

  }

  @Override
  public Sequence<Coup> joue() {
    Sequence<Coup> resultat = Configuration.nouvelleSequence();

    if (cases == null)
      cases = new CasesAccessibles(niveau.colonnePousseur(), niveau.lignePousseur());
    for (int i = 0; i < cases.nb_eleme - 1; i++) {
      Coup coup = new Coup();
      coup.deplacementPousseur(cases.position[i].y, cases.position[i].x, cases.position[i + 1].y,
          cases.position[i + 1].x);
      System.out.println("Position " + i + " : " + cases.position[i].y + " " + cases.position[i].x);
      // coup = niveau.deplace(cases.position[i].y - cases.position[i + 1].y,
      // cases.position[i].x - cases.position[i + 1].x);
      resultat.insereQueue(coup);
    }
    Coup coup = new Coup();
    coup.deplacementPousseur(cases.position[cases.nb_eleme - 1].y, cases.position[cases.nb_eleme - 1].x,
        cases.position[0].y,
        cases.position[0].x);
    resultat.insereQueue(coup);
    return resultat;
  }
}
