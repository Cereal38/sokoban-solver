
// X = Colonne
// Y = Ligne

package Modele;

import java.awt.Point;

import Global.Configuration;
import Structures.Sequence;

class Position extends Point {
  // Takes column (x) and line (y) as parameters

  Position(int column, int line) {
    super(column, line);
  }

  Position haut() {
    return new Position(x, y - 1);
  }

  Position bas() {
    return new Position(x, y + 1);
  }

  Position gauche() {
    return new Position(x - 1, y);
  }

  Position droite() {
    return new Position(x + 1, y);
  }

  int ligne() {
    return y;
  }

  int colonne() {
    return x;
  }

}

class IASolver extends IA {
  private static int EXISTE_PAS = -1;

  class EtatDuNiveau {
    Point positionApresDeplacement; // Position du pousseur après le déplacement de la caisse
    Point positionAvantDeplacement; // Position du pousseur avant le déplacement de la caisse
    int[][] posCaisses; // Position des caisses
    int pere; // Indice du père dans la liste des états

    EtatDuNiveau(Point positionApresDeplacement, Point positionAvantDeplacement, int[][] posCaisses, int pere) {
      this.positionApresDeplacement = positionApresDeplacement;
      this.positionAvantDeplacement = positionAvantDeplacement;
      this.posCaisses = posCaisses;
      this.pere = pere;
    }
  }

  class MouvementJoueur {
    Position joueur;
    int mouvementL; // Mouvement du joueur en ligne (1 -> bas, -1 -> haut, 0 -> pas de mouvement)
    int mouvementC; // Mouvement du joueur en colonne (1 -> droite, -1 -> gauche, 0 -> pas de
                    // mouvement)

    MouvementJoueur(Position joueur, int mouvementL, int mouvementC) {
      this.joueur = joueur;
      this.mouvementL = mouvementL;
      this.mouvementC = mouvementC;
    }
  }

  class Solution {
    EtatDuNiveau[] etats; // TODO: Move it to resoudre() (Avoir useless ram usage)
    MouvementJoueur[] mouvements;
    Position[] posButs;
    Niveau niveauSansCaisse;
    int index = 0;

    Solution() {
      // Ajoute la position des buts
      posButs = positionsButs(niveau);
      etats = new EtatDuNiveau[100000];
      niveauSansCaisse = copieNiveauSansCaisseSansJoueur(niveau);
      index = 0;
      // Ajoute l'état initial
      ajouteEtat(
          new EtatDuNiveau(new Point(niveau.lignePousseur(), niveau.colonnePousseur()),
              new Point(EXISTE_PAS, EXISTE_PAS),
              positionCaisses(niveau),
              EXISTE_PAS));
    }

    // Renvoie la position des buts dans le niveau
    private Position[] positionsButs(Niveau niveau) {
      int indexButs = 0;
      Position[] posButs = new Position[niveau.nbButs];
      for (int i = 0; i < niveau.lignes(); i++) {
        for (int j = 0; j < niveau.colonnes(); j++) {
          if (niveau.aBut(i, j)) {
            posButs[indexButs] = new Position(j, i);
            indexButs++;
          }
        }
      }

      return posButs;
    }

    // Renvoie la position des caisses dans le niveau
    private int[][] positionCaisses(Niveau niveau) {
      int indexCaisse = 0;
      int[][] posCaisses = new int[niveau.nbButs][2];
      for (int i = 0; i < niveau.lignes(); i++) {
        for (int j = 0; j < niveau.colonnes(); j++) {
          if (niveau.aCaisse(i, j)) {
            posCaisses[indexCaisse][0] = i;
            posCaisses[indexCaisse][1] = j;
            indexCaisse++;
          }
        }
      }
      return posCaisses;
    }

    // Renvoie true si toutes les caisses sont sur des buts
    private boolean niveauTerminee(int[][] positionsCaisses) {
      int nbBut = 0;
      for (int i = 0; i < positionsCaisses.length; i++) {
        for (int j = 0; j < posButs.length; j++) {
          if (positionsCaisses[i][0] == posButs[j].y && positionsCaisses[i][1] == posButs[j].x) {
            nbBut++;
          }
        }
      }
      return nbBut == posButs.length;
    }

    private Niveau copieNiveauSansCaisseSansJoueur(Niveau niveauAvecCaissesAvecJoueur) {
      Niveau niveauSansCaisse = niveau.clone();
      // Parcours tout le niveau et remplace les caisses et le joueur par des cases
      // vides
      for (int i = 0; i < niveauAvecCaissesAvecJoueur.lignes(); i++) {
        for (int j = 0; j < niveauAvecCaissesAvecJoueur.colonnes(); j++) {
          if (niveauAvecCaissesAvecJoueur.aCaisse(i, j) || niveauAvecCaissesAvecJoueur.aPousseur(i, j)) {
            niveauSansCaisse.cases[i][j] = Niveau.VIDE;
          }
        }
      }
      return niveauSansCaisse;
    }

    private Niveau copieNiveauAvecCaisseAvecJoueur(Niveau niveauSansCaisseSansJoueur, int[][] posCaisses,
        int posLJoueur, int posCJoueur) {
      Niveau niveauAvecCaisseAvecJoueur = niveauSansCaisseSansJoueur.clone();
      // Parcours tout le niveau et ajoute les caisses
      for (int i = 0; i < posCaisses.length; i++) {
        niveauAvecCaisseAvecJoueur.cases[posCaisses[i][0]][posCaisses[i][1]] = Niveau.CAISSE;
      }
      // Ajoute le joueur
      niveauAvecCaisseAvecJoueur.cases[posLJoueur][posCJoueur] = Niveau.POUSSEUR;
      return niveauAvecCaisseAvecJoueur;
    }

    // Return true if this configuration already exists in the array
    private boolean dejaVu(int posL, int posC, int[][] posCaisses) {
      for (int i = 0; i < index; i++) {
        if (etats[i].positionApresDeplacement.y == posL && etats[i].positionApresDeplacement.x == posC) {
          boolean caisses = true;
          for (int j = 0; j < posCaisses.length; j++) {
            if (etats[i].posCaisses[j][0] != posCaisses[j][0] || etats[i].posCaisses[j][1] != posCaisses[j][1]) {
              caisses = false;
              break;
            }
          }
          if (caisses)
            return true;
        }
      }
      return false;
    }

    // On vérifie si on ne va pas mettre la caisse dans un coin (qui n'est pas un
    // but)
    private boolean mouvementBloquant(Point posCaisse) {
      // On vérifie les cas ou il y a un mur dans 2 coins adjacents
      boolean caseHautMur = niveau.aMur(posCaisse.y - 1, posCaisse.x);
      boolean caseDroiteMur = niveau.aMur(posCaisse.y, posCaisse.x + 1);
      boolean caseBasMur = niveau.aMur(posCaisse.y + 1, posCaisse.x);
      boolean caseGaucheMur = niveau.aMur(posCaisse.y, posCaisse.x - 1);
      if (caseHautMur && caseDroiteMur) {
        return true;
      }
      if (caseHautMur && caseGaucheMur) {
        return true;
      }
      if (caseDroiteMur && caseBasMur) {
        return true;
      }
      if (caseBasMur && caseGaucheMur) {
        return true;
      }
      return false;
    }

    private void ajouteEtat(EtatDuNiveau etat) {
      if (!dejaVu(etat.positionApresDeplacement.y, etat.positionApresDeplacement.x, etat.posCaisses)) {
        etats[index] = etat;
        index++;
      }
    }

    private void extraireChemin() {
      // Cas où l'on n'a pas trouvé de solution
      if (!niveauTerminee(etats[index - 1].posCaisses)) {
        System.out.println("Solution non trouvé");
        return;
      }
      int[] chemin = new int[index];
      int indexTemp = index - 1;
      int indexChemin = 0;
      // On remonte le chemin
      while (etats[indexTemp].pere != EXISTE_PAS) {
        chemin[indexChemin] = indexTemp;
        indexTemp = etats[indexTemp].pere;
        indexChemin++;
      }
      MouvementJoueur[] mouvementsInner = new MouvementJoueur[indexChemin];
      int indexCheminRetour = indexChemin - 1;
      int indexMouvementJoueur = 0;
      // On parcours le chemin à l'envers
      while (indexMouvementJoueur < indexChemin) {
        int posl = etats[chemin[indexCheminRetour]].positionApresDeplacement.y;
        int posc = etats[chemin[indexCheminRetour]].positionApresDeplacement.x;
        // On calcul le mouvement du joueur
        int verticale = etats[chemin[indexCheminRetour]].positionAvantDeplacement.y - posl;
        int horizontale = etats[chemin[indexCheminRetour]].positionAvantDeplacement.x - posc;
        MouvementJoueur mouvement = new MouvementJoueur(new Position(posc, posl), verticale, horizontale);
        mouvementsInner[indexMouvementJoueur] = mouvement;
        indexCheminRetour--;
        indexMouvementJoueur++;
      }

      mouvements = mouvementsInner;
    }

    public void resoudre() {
      int indexTemp = 0;
      // Tant qu'il reste des élements dans la liste
      while (etats[indexTemp] != null) {
        // On récupère l'état de l'indice actuel
        EtatDuNiveau etatCourant = etats[indexTemp];
        // On récupère les infos
        int posL = etatCourant.positionApresDeplacement.y;
        int posC = etatCourant.positionApresDeplacement.x;
        int[][] posCaisses = etatCourant.posCaisses;
        // On récupère le niveau actuel
        Niveau niveauCourant = copieNiveauAvecCaisseAvecJoueur(niveauSansCaisse, posCaisses, posL, posC);
        // On récupère les cases accessibles
        CasesAccessibles cases = new CasesAccessibles(niveauCourant, new Position(posC, posL));
        // On récupère les mouvements possibles
        CaissesDeplacables caisses = new CaissesDeplacables(cases.nbCaissesDeplacables);
        caisses.trouverMouvementsCaisses(niveauCourant, cases);
        System.out.println("Etat " + indexTemp);
        niveauCourant.affiche();
        // On ajoute tout les mouvements de caisse possibles au tableau
        for (int i = 0; i < caisses.nbMouvements; i++) {
          // On récupère la nouvelle position du joueur (position actuelle de la caisse)
          int posLNew = caisses.mouvementsPossibles[i][1].y;
          int posCNew = caisses.mouvementsPossibles[i][1].x;
          int posLAncienne = 0;
          int posCAncienne = 0;
          // On récupère la nouvelle position de la caisse
          int[][] posCaissesNew = new int[posCaisses.length][2];
          for (int j = 0; j < posCaisses.length; j++) {
            // Cas où la caisse bouge
            if (posCaisses[j][0] == caisses.mouvementsPossibles[i][1].y
                && posCaisses[j][1] == caisses.mouvementsPossibles[i][1].x) {
              posLAncienne = caisses.mouvementsPossibles[i][0].y;
              posCAncienne = caisses.mouvementsPossibles[i][0].x;
              posCaissesNew[j][0] = caisses.mouvementsPossibles[i][2].y;
              posCaissesNew[j][1] = caisses.mouvementsPossibles[i][2].x;
              // Cas où la caisse ne bouge pas
            } else {
              posCaissesNew[j][0] = posCaisses[j][0];
              posCaissesNew[j][1] = posCaisses[j][1];
            }
          }

          // On ajoute des heuristiques
          // On vérifie si le mouvement est bloquant
          if (!mouvementBloquant(caisses.mouvementsPossibles[i][2])) {
            // On ajoute l'état
            ajouteEtat(
                new EtatDuNiveau(new Point(posCNew, posLNew), new Point(posCAncienne, posLAncienne), posCaissesNew,
                    indexTemp));
            // On vérifie si le niveau est terminé
            if (niveauTerminee(posCaissesNew)) {
              System.out.println("Niveau terminé");
              break;
            }
          }

        }
        indexTemp++;
      }
      // On construit le bon chemin
      extraireChemin();
    }

  }

  class CaissesDeplacables {
    Position[][] mouvementsPossibles;
    int nbMouvements = 0;
    int nbCaissesDeplacables;

    CaissesDeplacables(int nbCaisses) {
      nbCaissesDeplacables = nbCaisses;
    }

    // Position du joueur requise pour déplacer la caisse, position de la caisse,
    // future position de la caisse
    // [ [(2,3), (2, 4), (2, 5)], [(6, 7), (5, 7), (4, 7)] ]
    public void trouverMouvementsCaisses(Niveau niveau, CasesAccessibles casesAccessibles) {
      // On reset les infos
      nbMouvements = 0;
      mouvementsPossibles = new Position[nbCaissesDeplacables * 4][3];
      for (int i = 0; i < nbCaissesDeplacables; i++) {
        // On regarde si la case en haut est accessible
        // Si la case est accessible, on regarde si la case à l'opposée est libre (ou un
        // but)
        // Si vrai on ajoute le mouvement
        int caisseX = casesAccessibles.caissesAccessibles[i].x;
        int caisseY = casesAccessibles.caissesAccessibles[i].y;
        Position positionCaisse = new Position(caisseX, caisseY);
        // On regarde si la case en haut est accessible
        if (casesAccessibles.existe(positionCaisse.haut()) != EXISTE_PAS) {
          if (niveau.estOccupable(caisseY + 1, caisseX)) {
            // Poisition du joueur requise pour déplacer la caisse
            mouvementsPossibles[nbMouvements][0] = positionCaisse.haut();
            // Position de la caisse
            mouvementsPossibles[nbMouvements][1] = positionCaisse;
            // Future position de la caisse
            mouvementsPossibles[nbMouvements][2] = positionCaisse.bas();
            nbMouvements++;
          }
        }
        // Pareil pour le bas
        if (casesAccessibles.existe(positionCaisse.bas()) != EXISTE_PAS) {
          if (niveau.estOccupable(caisseY - 1, caisseX)) {
            mouvementsPossibles[nbMouvements][0] = positionCaisse.bas();
            mouvementsPossibles[nbMouvements][1] = positionCaisse;
            mouvementsPossibles[nbMouvements][2] = positionCaisse.haut();
            nbMouvements++;
          }
        }
        // Pareil pour la gauche
        if (casesAccessibles.existe(positionCaisse.gauche()) != EXISTE_PAS) {
          if (niveau.estOccupable(caisseY, caisseX + 1)) {
            mouvementsPossibles[nbMouvements][0] = positionCaisse.gauche();
            mouvementsPossibles[nbMouvements][1] = positionCaisse;
            mouvementsPossibles[nbMouvements][2] = positionCaisse.droite();
            nbMouvements++;
          }
        }
        // Pareil pour la droite
        if (casesAccessibles.existe(positionCaisse.droite()) != EXISTE_PAS) {
          if (niveau.estOccupable(caisseY, caisseX - 1)) {
            mouvementsPossibles[nbMouvements][0] = positionCaisse.droite();
            mouvementsPossibles[nbMouvements][1] = positionCaisse;
            mouvementsPossibles[nbMouvements][2] = positionCaisse.gauche();
            nbMouvements++;
          }
        }
      }
    }
  }

  // Cases accessibles par le pousseur
  class CasesAccessibles {

    // Toutes les caisses accessibles à l'instant t
    Position[] caissesAccessibles;
    // Toutes les cases accessibles à l'instant t
    Position[] positions;
    int taille, nbEleme;
    int nbCaissesDeplacables = 0;

    // On récupère les cases et caisses accessibles par le joueur depuis sa position
    public CasesAccessibles(Niveau niveauInner, Position joueur) {
      taille = niveauInner.lignes() * niveauInner.colonnes();
      nbEleme = 1;
      caissesAccessibles = new Position[niveauInner.nbButs];
      ajouterCasesAccessibles(niveauInner, joueur);
    }

    private void verifieEtAjoute(Niveau niveauInner, Position p) {
      // On vérifie si la case existe
      if (existe(p) != EXISTE_PAS) {
        return;
      }

      // On met à jour les cases accessibles par le joueur
      if (niveauInner.estOccupable(p.ligne(), p.colonne())) {
        positions[nbEleme] = p;
        nbEleme++;
        // On met à jour les caisses accessibles par le joueur
      } else {
        if (niveauInner.aCaisse(p.ligne(), p.colonne())) {
          for (int i = 0; i < nbCaissesDeplacables; i++) {
            if (caissesAccessibles[i].x == p.colonne() && caissesAccessibles[i].y == p.ligne())
              return;
          }
          caissesAccessibles[nbCaissesDeplacables] = p;
          nbCaissesDeplacables++;
        }
      }
    }

    public void ajouterCasesAccessibles(Niveau niveauInner, Position joueur) {
      // On clear le tableau
      positions = new Position[taille];
      // On ajoute la position du pousseur
      positions[0] = joueur;
      nbEleme = 1;
      int i = 0;
      while (i < nbEleme) {

        // On regarde chaque case adjacente si elle n'est pas déjà dans le tableau et si
        // elle est accessible, si oui on l'ajoute
        Position p = new Position(positions[i].x, positions[i].y);
        verifieEtAjoute(niveauInner, p.haut());
        verifieEtAjoute(niveauInner, p.bas());
        verifieEtAjoute(niveauInner, p.gauche());
        verifieEtAjoute(niveauInner, p.droite());

        i++;
      }
    }

    // Renvoie l'indice de l'élément si il existe, -1 sinon
    public int existe(Position p) {
      for (int i = 0; i < nbEleme; i++) {
        if (positions[i].x == p.colonne() && positions[i].y == p.ligne())
          return i;
      }
      return EXISTE_PAS;
    }

  }

  @Override
  public Sequence<Coup> joue() {
    Sequence<Coup> resultat = Configuration.nouvelleSequence();

    // On test si Solution retire bien les caisses
    Solution solution = new Solution();
    solution.resoudre();

    // On se déplace de la position joueur à la position devant caisse
    // Ensuite on fait le déplacement de la caisse
    // On récupère la nouvelle position
    // Retour au début de boucle
    MouvementJoueur[] mouvements = solution.mouvements;
    // On récupère les pos de base du joueur
    int joueurL = niveau.lignePousseur();
    int joueurC = niveau.colonnePousseur();
    // On effectue les mouvements
    for (int i = 0; i < mouvements.length; i++) {
      Coup coup = new Coup();
      coup.deplacementPousseur(joueurL, joueurC, mouvements[i].joueur.y - mouvements[i].mouvementL,
          mouvements[i].joueur.x - mouvements[i].mouvementC);
      resultat.insereQueue(coup);
      coup = niveau.deplace(mouvements[i].mouvementL, mouvements[i].mouvementC);
      resultat.insereQueue(coup);
      joueurC = solution.mouvements[i].joueur.x;
      joueurL = solution.mouvements[i].joueur.y;
    }

    // for (int i = 0; i < cases.nbEleme - 1; i++) {
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
    // coup.deplacementPousseur(cases.position[cases.nbEleme - 1].y,
    // cases.position[cases.nbEleme - 1].x,
    // cases.position[0].y,
    // cases.position[0].x);
    // resultat.insereQueue(coup);
    return resultat;
  }
}
