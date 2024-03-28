
// X = Colonne
// Y = Ligne

package Modele;

import java.awt.Point;

import Global.Configuration;
import Structures.Sequence;

class IASolver extends IA {

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
    Point joueur;
    int mouvementL; // Mouvement du joueur en ligne (1 -> bas, -1 -> haut, 0 -> pas de mouvement)
    int mouvementC; // Mouvement du joueur en colonne (1 -> droite, -1 -> gauche, 0 -> pas de
                    // mouvement)

    MouvementJoueur(Point joueur, int mouvementL, int mouvementC) {
      this.joueur = joueur;
      this.mouvementL = mouvementL;
      this.mouvementC = mouvementC;
    }
  }

  class Solution {
    EtatDuNiveau[] etats; // TODO: Move it to resoudre() (Avoir useless ram usage)
    MouvementJoueur[] mouvements;
    Point[] posButs;
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
          new EtatDuNiveau(new Point(niveau.lignePousseur(), niveau.colonnePousseur()), new Point(-1, -1),
              positionCaisses(niveau),
              -1));
    }

    // Renvoie la position des buts dans le niveau
    private Point[] positionsButs(Niveau niveau) {
      int indexButs = 0;
      Point[] posButs = new Point[niveau.nbButs];
      for (int i = 0; i < niveau.lignes(); i++) {
        for (int j = 0; j < niveau.colonnes(); j++) {
          if (niveau.aBut(i, j)) {
            posButs[indexButs] = new Point(j, i);
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
      while (etats[indexTemp].pere != -1) {
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
        MouvementJoueur mouvement = new MouvementJoueur(new Point(posc, posl), verticale, horizontale);
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
        CasesAccessibles cases = new CasesAccessibles(niveauCourant, new Point(posC, posL));
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
    Point[][] mouvementsPossibles;
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
      mouvementsPossibles = new Point[niveau.nbButs * 4][3];
      for (int i = 0; i < nbCaissesDeplacables; i++) {
        // On regarde si la case en haut est accessible
        // Si la case est accessible, on regarde si la case à l'opposée est libre (ou un
        // but)
        // Si vrai on ajoute le mouvement
        int caisseX = casesAccessibles.caissesAccessibles[i].x;
        int caisseY = casesAccessibles.caissesAccessibles[i].y;
        if (casesAccessibles.existe(caisseY - 1, caisseX) != -1) {
          if (niveau.estOccupable(caisseY + 1, caisseX)) {
            // Poisition du joueur requise pour déplacer la caisse
            mouvementsPossibles[nbMouvements][0] = new Point(caisseX, caisseY - 1);
            // Position de la caisse
            mouvementsPossibles[nbMouvements][1] = new Point(caisseX, caisseY);
            // Future position de la caisse
            mouvementsPossibles[nbMouvements][2] = new Point(caisseX, caisseY + 1);
            nbMouvements++;
          }
        }
        // Pareil pour le bas
        if (casesAccessibles.existe(caisseY + 1, caisseX) != -1) {
          if (niveau.estOccupable(caisseY - 1, caisseX)) {
            mouvementsPossibles[nbMouvements][0] = new Point(caisseX, caisseY + 1);
            mouvementsPossibles[nbMouvements][1] = new Point(caisseX, caisseY);
            mouvementsPossibles[nbMouvements][2] = new Point(caisseX, caisseY - 1);
            nbMouvements++;
          }
        }
        // Pareil pour la droite
        if (casesAccessibles.existe(caisseY, caisseX - 1) != -1) {
          if (niveau.estOccupable(caisseY, caisseX + 1)) {
            mouvementsPossibles[nbMouvements][0] = new Point(caisseX - 1, caisseY);
            mouvementsPossibles[nbMouvements][1] = new Point(caisseX, caisseY);
            mouvementsPossibles[nbMouvements][2] = new Point(caisseX + 1, caisseY);
            nbMouvements++;
          }
        }
        // Pareil pour la gauche
        if (casesAccessibles.existe(caisseY, caisseX + 1) != -1) {
          if (niveau.estOccupable(caisseY, caisseX - 1)) {
            mouvementsPossibles[nbMouvements][0] = new Point(caisseX + 1, caisseY);
            mouvementsPossibles[nbMouvements][1] = new Point(caisseX, caisseY);
            mouvementsPossibles[nbMouvements][2] = new Point(caisseX - 1, caisseY);
            nbMouvements++;
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
    int taille, nbEleme;
    int nbCaissesDeplacables = 0;

    // On récupère les cases et caisses accessibles par le joueur depuis sa position
    public CasesAccessibles(Niveau niveauInner, Point pousseur) {
      taille = niveauInner.lignes() * niveauInner.colonnes();
      nbEleme = 1;
      caissesAccessibles = new Point[niveauInner.nbButs];
      ajouterCasesAccessibles(niveauInner, new Point(pousseur.x, pousseur.y));
    }

    private void checkAndAddPosition(Niveau niveauInner, int l, int c) {
      if (existe(l, c) == -1) {
        // On met à jour les cases accessibles par le joueur
        if (niveauInner.estOccupable(l, c)) {
          position[nbEleme] = new Point(c, l);
          nbEleme++;
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

    public void ajouterCasesAccessibles(Niveau niveauInner, Point joueur) {
      // On clear le tableau
      position = new Point[taille];
      // On ajoute la position du pousseur
      position[0] = joueur;
      nbEleme = 1;
      int i = 0;
      while (i < nbEleme) {

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
      for (int i = 0; i < nbEleme; i++) {
        if (position[i].x == c && position[i].y == l)
          return i;
      }
      return -1;
    }

    // Double la taille du tableau si il est complet
    protected void redimensionne() {
      if (taille >= nbEleme) {
        taille = taille * 2;
        Point[] nouveau = new Point[taille];
        for (int i = 0; i < nbEleme; i++)
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
