
// X = Colonne
// Y = Ligne

package Modele;

import java.awt.Point;
import java.util.PriorityQueue;

import Global.Configuration;
import Structures.Sequence;

class IASolver extends IA {
  // Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
  final static int VERT = 0x00CC00;
  final static int MARRON = 0xBB7755;

  int nb_caisses = 0;

  class EtatDuNiveau {
    int posL; // Position du pousseur en ligne
    int posC; // Position du pousseur en colonne
    int[][] posCaisses; // Position des caisses
    int pere; // Indice du père dans la liste des états

    EtatDuNiveau(int posL, int posC, int[][] posCaisses, int pere) {
      this.posL = posL;
      this.posC = posC;
      this.posCaisses = posCaisses;
      this.pere = pere;
    }
  }

  class Solution {
    PriorityQueue<EtatDuNiveau> etats;
    Niveau niveauSansCaisse;

    Solution() {
      etats = new PriorityQueue<EtatDuNiveau>();
      niveauSansCaisse = copieNiveauSansCaisse(niveau);
    }

    private Niveau copieNiveauSansCaisse(Niveau niveauAvecCaisses) {
      Niveau niveauSansCaisse = niveau.clone();
      // Parcours tout le niveau et remplace les caisses par des cases vides
      for (int i = 0; i < niveauAvecCaisses.lignes(); i++) {
        for (int j = 0; j < niveauAvecCaisses.colonnes(); j++) {
          if (niveauAvecCaisses.aCaisse(i, j)) {
            niveauSansCaisse.cases[i][j] = Niveau.VIDE;
          }
        }
      }
      return niveauSansCaisse;
    }

    private void ajouteEtat(EtatDuNiveau etat) {
      etats.add(etat);
    }

    public void resoudre() {
      // Tant qu'une solution n'a pas été trouvé et que toute la liste n'a pas été
      // exploré

    }
  }

  class CaissesDeplacables {
    Point[][] mouvementsPossibles;
    int nb_mouvements = 0;

    // [ [(2,3), (2, 4)], [(6, 7), (5, 7)] ]
    public void trouverMouvementsCaisses(CasesAccessibles casesAccessibles) {
      // On reset les infos
      nb_mouvements = 0;
      mouvementsPossibles = new Point[niveau.nbButs * 4][2];
      for (int i = 0; i < nb_caisses; i++) {
        // On regarde si la case en haut est accessible
        // Si la case est accessible, on regarde si la case à l'opposée est libre (ou un
        // but)
        // Si vrai on ajoute le mouvement
        if (casesAccessibles.existe(casesAccessibles.caissesAccessibles[i].y - 1,
            casesAccessibles.caissesAccessibles[i].x) != -1) {
          if (niveau.estOccupable(casesAccessibles.caissesAccessibles[i].y + 1,
              casesAccessibles.caissesAccessibles[i].x)) {
            mouvementsPossibles[nb_mouvements][0] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y - 1);
            mouvementsPossibles[nb_mouvements][1] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y);
            nb_mouvements++;
          }
        }
        // Pareil pour le bas
        if (casesAccessibles.existe(casesAccessibles.caissesAccessibles[i].y + 1,
            casesAccessibles.caissesAccessibles[i].x) != -1) {
          if (niveau.estOccupable(casesAccessibles.caissesAccessibles[i].y - 1,
              casesAccessibles.caissesAccessibles[i].x)) {
            mouvementsPossibles[nb_mouvements][0] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y + 1);
            mouvementsPossibles[nb_mouvements][1] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y);
            nb_mouvements++;
          }
        }
        // Pareil pour la droite
        if (casesAccessibles.existe(casesAccessibles.caissesAccessibles[i].y,
            casesAccessibles.caissesAccessibles[i].x - 1) != -1) {
          if (niveau.estOccupable(casesAccessibles.caissesAccessibles[i].y,
              casesAccessibles.caissesAccessibles[i].x + 1)) {
            mouvementsPossibles[nb_mouvements][0] = new Point(casesAccessibles.caissesAccessibles[i].x - 1,
                casesAccessibles.caissesAccessibles[i].y);
            mouvementsPossibles[nb_mouvements][1] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y);
            nb_mouvements++;
          }
        }
        // Pareil pour la gauche
        if (casesAccessibles.existe(casesAccessibles.caissesAccessibles[i].y,
            casesAccessibles.caissesAccessibles[i].x + 1) != -1) {
          if (niveau.estOccupable(casesAccessibles.caissesAccessibles[i].y,
              casesAccessibles.caissesAccessibles[i].x - 1)) {
            mouvementsPossibles[nb_mouvements][0] = new Point(casesAccessibles.caissesAccessibles[i].x + 1,
                casesAccessibles.caissesAccessibles[i].y);
            mouvementsPossibles[nb_mouvements][1] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y);
            nb_mouvements++;
          }
        }
      }
    }
  }

  // Cases accessibles par le pousseur
  class CasesAccessibles {
    // Toutes les caisses accessibles à l'instant t
    Point[] caissesAccessibles;
    Point[] position;
    int taille, nb_eleme;

    public CasesAccessibles(int x, int y) {
      taille = niveau.lignes() * niveau.colonnes();
      nb_eleme = 1;
      ajouterCasesAccessibles(x, y);
      caissesAccessibles = new Point[niveau.nbButs];
    }

    private void checkAndAddPosition(int y, int x) {
      if (existe(y, x) == -1) {
        // On met à jour les cases accessibles par le joueur
        if (niveau.estOccupable(y, x)) {
          position[nb_eleme] = new Point(x, y);
          nb_eleme++;
          // On met à jour les caisses accessibles par le joueur
        } else {
          if (niveau.aCaisse(y, x)) {
            for (int i = 0; i < nb_caisses; i++) {
              if (caissesAccessibles[i].x == x && caissesAccessibles[i].y == y)
                return;
            }
            caissesAccessibles[nb_caisses] = new Point(x, y);
            nb_caisses++;
          }
        }
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
        // for (int j = 0; j < nb_eleme; j++) {
        // System.out.print(position[j].x + " " + position[j].y + " | ");
        // }
        // System.out.println();

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
        if (position[i].x == c && position[i].y == l)
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
      return estAccessible(position[i].x - 1, position[i].y);
    }

    public boolean bas(int i) {
      return estAccessible(position[i].x + 1, position[i].y);
    }

    public boolean gauche(int i) {
      return estAccessible(position[i].x, position[i].y - 1);
    }

    public boolean droite(int i) {
      return estAccessible(position[i].x, position[i].y + 1);
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

    // On test si Solution retire bien les caisses
    Solution solution = new Solution();
    System.out.println("Niveau sans caisses : ");
    solution.niveauSansCaisse.affiche();

    // for (int i = 0; i < cases.nb_eleme - 1; i++) {
    // Coup coup = new Coup();
    // coup.deplacementPousseur(cases.position[i].y, cases.position[i].x,
    // cases.position[i + 1].y,
    // cases.position[i + 1].x);
    // System.out.println("Position " + i + " : " + cases.position[i].y + " " +
    // cases.position[i].x);
    // // coup = niveau.deplace(cases.position[i].y - cases.position[i + 1].y,
    // // cases.position[i].x - cases.position[i + 1].x);
    // resultat.insereQueue(coup);
    // }
    // Coup coup = new Coup();
    // coup.deplacementPousseur(cases.position[cases.nb_eleme - 1].y,
    // cases.position[cases.nb_eleme - 1].x,
    // cases.position[0].y,
    // cases.position[0].x);
    // resultat.insereQueue(coup);
    return resultat;
  }
}
