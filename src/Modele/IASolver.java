
// X = Colonne
// Y = Ligne

package Modele;

import java.awt.Point;

import Global.Configuration;
import Structures.Sequence;
import Structures.SequenceListe;

class IASolver extends IA {
  // Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
  final static int VERT = 0x00CC00;
  final static int MARRON = 0xBB7755;
  CasesAccessibles cases = null;
  CaissesDeplacables caisses = null;

  int nb_caisses = 0;

  class EtatDuNiveau {
    CaissesDeplacables caisses;
    CasesAccessibles cases;
    int joueurC;
    int joueurL;

    void EtatDuNiveau(CasesAccessibles ca, CaissesDeplacables cd, int jC, int jL) {
      cases = ca;
      caisses = cd;
      joueurC = jC;
      joueurL = jL;
    }
  }

  class Solutions {
    // 1 Solution = 1 tableau de tuples correspondant aux mouvements de caisses
    // (Position pousseur, Position caisse)
    // Solutions = tableau de solutions
    SequenceListe<EtatDuNiveau>[] solutions;
    // Enchainement de déplacement de caisses

    public void trouverSolutions() {
      SequenceListe<EtatDuNiveau> deplacements = new SequenceListe();
      // solutions = trouverSolutionsRec(niveau, niveau.pousseurL, niveau.pousseurC,
      // deplacements);
    }

    // Prend l'état actuel
    private EtatDuNiveau[] trouverSolutionsRec(Niveau etatDuNiveau, int joueurL, int joueurC,
        SequenceListe<EtatDuNiveau> anciennesPositions) {

      // Cas de base
      if (etatDuNiveau.estTermine()) {
        return null;
      }

      CasesAccessibles cases = new CasesAccessibles(etatDuNiveau.colonnePousseur(), etatDuNiveau.lignePousseur());
      CaissesDeplacables caisses = new CaissesDeplacables();
      caisses.trouverMouvementsCaisses(cases);

      for (int i = 0; i < caisses.nb_mouvements; i++) {
        Niveau etatSuivant = etatDuNiveau.clone();
        // L'ancienne position du joueur devient vide
        etatSuivant.cases[joueurL][joueurC] = Niveau.VIDE;
        // L'ancienne position de la caisse devient la position du joueur
        int nouvellePositionJoueurC = caisses.mouvementsPossibles[i][0].x;
        int nouvellePositionJoueurL = caisses.mouvementsPossibles[i][0].y;
        etatSuivant.cases[nouvellePositionJoueurL][nouvellePositionJoueurC] = Niveau.POUSSEUR;
        // pousseur.x - caisse.x
        // gauche = 1 , droite = -1
        // pousseur.y - caisse.y
        // bas = -1, haut = 1
        // si le res != 0 alors caisse + res
        // On calcul la nouvelle position de la caisse
        int mouvementX = (caisses.mouvementsPossibles[i][0].x - caisses.mouvementsPossibles[i][1].x) * -1;
        int mouvementY = (caisses.mouvementsPossibles[i][0].y - caisses.mouvementsPossibles[i][1].y) * -1;
        etatSuivant.cases[caisses.mouvementsPossibles[i][1].y + mouvementY][caisses.mouvementsPossibles[i][1].x
            + mouvementX] = Niveau.CAISSE;

        // trouverSolutionsRec(etatSuivant, nouvellePositionJoueurL,
        // nouvellePositionJoueurC);
      }

      return null;
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

    if (cases == null) {
      // caissesAccessibles = new Point[niveau.nbButs];
      cases = new CasesAccessibles(niveau.colonnePousseur(), niveau.lignePousseur());
      CaissesDeplacables caissesAccessiblesTemporaire = new CaissesDeplacables();
      caissesAccessiblesTemporaire.trouverMouvementsCaisses(cases);
      // System.out.println("Nombre de mouvements : " +
      // caissesAccessiblesTemporaire.nb_mouvements);
      // // Affiche tout le tableau
      // for (int i = 0; i < nb_caisses; i++) {
      // System.out.println("Caisse " + i + " : " + caissesAccessibles[i].x + " " +
      // caissesAccessibles[i].y);
      // }
      // for (int i = 0; i < caissesAccessiblesTemporaire.nb_mouvements; i++) {
      // System.out.println("Mouvement " + i + " : " +
      // caissesAccessiblesTemporaire.mouvementsPossibles[i][0].x + " "
      // + caissesAccessiblesTemporaire.mouvementsPossibles[i][0].y + " | "
      // + caissesAccessiblesTemporaire.mouvementsPossibles[i][1].x
      // + " " + caissesAccessiblesTemporaire.mouvementsPossibles[i][1].y);
      // }
    }
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
