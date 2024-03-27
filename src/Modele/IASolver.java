
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
    EtatDuNiveau[] etats;
    Niveau niveauSansCaisse;
    int index = 0;

    Solution() {
      etats = new EtatDuNiveau[1000];
      niveauSansCaisse = copieNiveauSansCaisse(niveau);
      index = 0;
      // Ajoute l'état initial
      ajouteEtat(new EtatDuNiveau(niveau.lignePousseur(), niveau.colonnePousseur(), positionCaisses(niveau), -1));
    }

    private int[][] positionCaisses(Niveau niveau) {
      int index_caisse = 0;
      int[][] posCaisses = new int[niveau.nbButs][2];
      for (int i = 0; i < niveau.lignes(); i++) {
        for (int j = 0; j < niveau.colonnes(); j++) {
          if (niveau.aCaisse(i, j)) {
            posCaisses[index_caisse][0] = i;
            posCaisses[index_caisse][1] = j;
            index_caisse++;
          }
        }
      }
      return posCaisses;
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

    private Niveau copieNiveauAvecCaisse(Niveau niveauSansCaisse, int[][] posCaisses) {
      Niveau niveauAvecCaisse = niveauSansCaisse.clone();
      // Parcours tout le niveau et remplace les cases vides par des caisses
      for (int i = 0; i < posCaisses.length; i++) {
        niveauAvecCaisse.cases[posCaisses[i][0]][posCaisses[i][1]] = Niveau.CAISSE;
      }
      return niveauAvecCaisse;
    }

    private void ajouteEtat(EtatDuNiveau etat) {
      etats[index] = etat;
      index++;
    }

    public void resoudre() {
      int index_temp = 0;
      // Tant qu'il reste des élements dans la liste
      while (etats[index_temp] != null) {
        // On récupère l'état de l'indice actuel
        EtatDuNiveau etatCourant = etats[index_temp];
        // On récupère les infos
        int posL = etatCourant.posL;
        int posC = etatCourant.posC;
        int[][] posCaisses = etatCourant.posCaisses;
        // On récupère le niveau actuel
        Niveau niveauCourant = copieNiveauAvecCaisse(niveauSansCaisse, posCaisses);
        // On récupère les cases accessibles
        CasesAccessibles cases = new CasesAccessibles(niveauCourant, posL, posC);
        // On récupère les mouvements possibles
        CaissesDeplacables caisses = new CaissesDeplacables(cases.nbCaissesDeplacables);
        caisses.trouverMouvementsCaisses(niveauCourant, cases);
        niveauCourant.affiche();
        // On ajoute tout les mouvements de caisse possibles au tableau
        for (int i = 0; i < caisses.nb_mouvements; i++) {
          // On récupère la nouvelle position du joueur (position actuelle de la caisse)
          int posLNew = caisses.mouvementsPossibles[i][1].y;
          int posCNew = caisses.mouvementsPossibles[i][1].x;
          // On récupère la nouvelle position de la caisse
          int[][] posCaissesNew = new int[posCaisses.length][2];
          for (int j = 0; j < posCaisses.length; j++) {
            if (posCaisses[j][0] == caisses.mouvementsPossibles[i][1].y
                && posCaisses[j][1] == caisses.mouvementsPossibles[i][1].x) {
              posCaissesNew[j][0] = caisses.mouvementsPossibles[i][2].y;
              posCaissesNew[j][1] = caisses.mouvementsPossibles[i][2].x;
            } else {
              posCaissesNew[j][0] = posCaisses[j][0];
              posCaissesNew[j][1] = posCaisses[j][1];
            }
          }
          // On ajoute l'état
          ajouteEtat(new EtatDuNiveau(posLNew, posCNew, posCaissesNew, index_temp));
        }
        index_temp++;
      }
    }
  }

  class CaissesDeplacables {
    Point[][] mouvementsPossibles;
    int nb_mouvements = 0;
    int nbCaissesDeplacables;

    CaissesDeplacables(int nbCaisses) {
      nbCaissesDeplacables = nbCaisses;
    }

    // Position du joueur requise pour déplacer la caisse, position de la caisse,
    // future position de la caisse
    // [ [(2,3), (2, 4), (2, 5)], [(6, 7), (5, 7), (4, 7)] ]
    public void trouverMouvementsCaisses(Niveau niveau, CasesAccessibles casesAccessibles) {
      // On reset les infos
      nb_mouvements = 0;
      mouvementsPossibles = new Point[niveau.nbButs * 4][3];
      for (int i = 0; i < nbCaissesDeplacables; i++) {
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
            mouvementsPossibles[nb_mouvements][2] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y + 1);
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
            mouvementsPossibles[nb_mouvements][2] = new Point(casesAccessibles.caissesAccessibles[i].x,
                casesAccessibles.caissesAccessibles[i].y - 1);
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
            mouvementsPossibles[nb_mouvements][1] = new Point(casesAccessibles.caissesAccessibles[i].x + 1,
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
            mouvementsPossibles[nb_mouvements][2] = new Point(casesAccessibles.caissesAccessibles[i].x - 1,
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
    int nbCaissesDeplacables = 0;

    public CasesAccessibles(Niveau niveauInner, int l, int c) {
      taille = niveauInner.lignes() * niveauInner.colonnes();
      nb_eleme = 1;
      caissesAccessibles = new Point[niveauInner.nbButs];
      ajouterCasesAccessibles(niveauInner, l, c);
    }

    private void checkAndAddPosition(Niveau niveauInner, int l, int c) {
      if (existe(l, c) == -1) {
        // On met à jour les cases accessibles par le joueur
        if (niveauInner.estOccupable(l, c)) {
          position[nb_eleme] = new Point(c, l);
          nb_eleme++;
          // On met à jour les caisses accessibles par le joueur
        } else {
          if (niveauInner.aCaisse(l, c)) {
            for (int i = 0; i < nbCaissesDeplacables; i++) {
              if (caissesAccessibles[i].x == c && caissesAccessibles[i].y == l)
                return;
            }
            caissesAccessibles[nbCaissesDeplacables] = new Point(c, l);
            nbCaissesDeplacables++;
          }
        }
      }
    }

    public void ajouterCasesAccessibles(Niveau niveauInner, int l, int c) {
      // On clear le tableau
      position = new Point[taille];
      // On ajoute la position du pousseur
      position[0] = new Point(c, l);
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
        checkAndAddPosition(niveauInner, position[i].y - 1, position[i].x);
        checkAndAddPosition(niveauInner, position[i].y + 1, position[i].x);
        checkAndAddPosition(niveauInner, position[i].y, position[i].x - 1);
        checkAndAddPosition(niveauInner, position[i].y, position[i].x + 1);

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
    solution.resoudre();

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
