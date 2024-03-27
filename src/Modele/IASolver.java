
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

  int nb_caisses = 0;

  class EtatDuNiveau {
    int posL; // Position du pousseur en ligne
    int posC; // Position du pousseur en colonne
    int[][] posCaisses; // Position des caisses
    int pere; // Indice du père dans la liste des états
    int nbCaissesAccessibles = 0;

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
    }

    public void resoudre() {
      // Tant qu'il reste des élements dans la liste
      while (etats[index] != null) {
        // On récupère l'état de l'indice actuel
        EtatDuNiveau etatCourant = etats[index];
        // On récupère les infos
        int posL = etatCourant.posL;
        int posC = etatCourant.posC;
        int[][] posCaisses = etatCourant.posCaisses;
        int pere = etatCourant.pere;
        // On récupère le niveau actuel
        Niveau niveauCourant = copieNiveauAvecCaisse(niveauSansCaisse, posCaisses);
        // On récupère les cases accessibles
        CasesAccessibles cases = new CasesAccessibles(niveauCourant, posL, posC);
        // On récupère les mouvements possibles
        CaissesDeplacables caisses = new CaissesDeplacables();
        caisses.trouverMouvementsCaisses(niveauCourant, cases);
        // On ajoute tout les mouvements de caisse possibles au tableau
        for (int i = 0; i < caisses.nb_mouvements; i++) {
          System.out.println("Index: " + index + " | Mouvement: " + i + " | Pere: " + pere + " | Position: " + posL
              + " " + posC + " | Caisse: " + posCaisses[i][0] + " " + posCaisses[i][1]);
          // On récupère les infos du mouvement
          Point posCaisse = caisses.mouvementsPossibles[i][1];
          Point posPousseur = caisses.mouvementsPossibles[i][0];
          // On crée un nouveau tableau de caisses
          int[][] posCaissesNouveau = new int[posCaisses.length][2];
          for (int j = 0; j < posCaisses.length; j++) {
            posCaissesNouveau[j][0] = posCaisses[j][0];
            posCaissesNouveau[j][1] = posCaisses[j][1];
          }
          // On met à jour la position de la caisse
          for (int j = 0; j < posCaissesNouveau.length; j++) {
            if (posCaissesNouveau[j][0] == posCaisse.y && posCaissesNouveau[j][1] == posCaisse.x) {
              posCaissesNouveau[j][0] = posPousseur.y;
              posCaissesNouveau[j][1] = posPousseur.x;
            }
          }
          // On ajoute l'état
          ajouteEtat(new EtatDuNiveau(posPousseur.y, posPousseur.x, posCaissesNouveau, index));
        }
        index++;
      }
    }
  }

  class CaissesDeplacables {
    Point[][] mouvementsPossibles; // Position du joueur requise pour déplacer la caisse, position de la caisse
    int nb_mouvements = 0;

    // [ [(2,3), (2, 4)], [(6, 7), (5, 7)] ]
    public void trouverMouvementsCaisses(Niveau niveau, CasesAccessibles casesAccessibles) {
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

    public CasesAccessibles(Niveau niveauInner, int l, int c) {
      taille = niveauInner.lignes() * niveauInner.colonnes();
      nb_eleme = 1;
      caissesAccessibles = new Point[niveauInner.nbButs];
      ajouterCasesAccessibles(niveauInner, l, c);
    }

    private void checkAndAddPosition(Niveau niveauInner, int l, int c) {
      if (existe(l, c) == -1) {
        // TODO: Remove print
        System.out.println("Ligne: " + l + " | Colonne: " + c);
        // On met à jour les cases accessibles par le joueur
        if (niveauInner.estOccupable(l, c)) {
          position[nb_eleme] = new Point(c, l);
          nb_eleme++;
          // On met à jour les caisses accessibles par le joueur
        } else {
          if (niveauInner.aCaisse(l, c)) {
            for (int i = 0; i < nb_caisses; i++) {
              if (caissesAccessibles[i].x == c && caissesAccessibles[i].y == l)
                return;
            }
            System.out.println("Caisse: " + l + " " + c);
            caissesAccessibles[nb_caisses] = new Point(c, l);
            nb_caisses++;
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
