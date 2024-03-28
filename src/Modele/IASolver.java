
// X = Colonne
// Y = Ligne

// TODO: Galere au niveau 24 car dejavu parcours tout le tableau

package Modele;

import Global.Configuration;
import Structures.Position;
import Structures.Sequence;

class IASolver extends IA {
  private static int EXISTE_PAS = -1;

  class EtatDuNiveau {
    Position positionApresDeplacement; // Position du pousseur après le déplacement de la caisse
    Position positionAvantDeplacement; // Position du pousseur avant le déplacement de la caisse
    Position[] posCaisses; // Position des caisses
    int pere; // Indice du père dans la liste des états

    EtatDuNiveau(Position positionApresDeplacement, Position positionAvantDeplacement, Position[] posCaisses,
        int pere) {
      this.positionApresDeplacement = positionApresDeplacement;
      this.positionAvantDeplacement = positionAvantDeplacement;
      this.posCaisses = posCaisses;
      this.pere = pere;
    }
  }

  class MouvementJoueur {
    Position joueur; // Emplacement du joueur pour déplacer la caisse
    Position caisse; // Emplacement de la caisse
    Position caisseDestination; // Emplacement de la caisse après le déplacement
    int vecteurLigne; // Vecteur de déplacement en ligne
    int vecteurColonne; // Vecteur de déplacement en colonne

    MouvementJoueur(Position joueur, Position caisse, Position caisseDestination, int vecteurLigne,
        int vecteurColonne) {
      this.joueur = joueur;
      this.caisse = caisse;
      this.caisseDestination = caisseDestination;
      this.vecteurLigne = vecteurLigne;
      this.vecteurColonne = vecteurColonne;
    }
  }

  class Solution {
    EtatDuNiveau[] etats; // TODO: Move it to resoudre() (Avoir useless ram usage)
    MouvementJoueur[] mouvements;
    Position[] posButs;
    Niveau niveauSansCaisse;
    int indexAjout = 0; // Index auquel on ajoute les états
    int indexParcours = 0; // Index qui indique l'état actuel

    Solution() {
      // Ajoute la position des buts
      posButs = positionsButs(niveau);
      etats = new EtatDuNiveau[10000000];
      niveauSansCaisse = copieNiveauSansCaisseSansJoueur(niveau);
      indexAjout = 0;
      indexParcours = 0;
      // Ajoute l'état initial
      ajouteEtat(
          new EtatDuNiveau(new Position(niveau.colonnePousseur(), niveau.lignePousseur()),
              new Position(EXISTE_PAS, EXISTE_PAS),
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
    private Position[] positionCaisses(Niveau niveau) {
      int indexCaisse = 0;
      Position[] posCaisses = new Position[niveau.nbButs];
      for (int i = 0; i < niveau.lignes(); i++) {
        for (int j = 0; j < niveau.colonnes(); j++) {
          if (niveau.aCaisse(i, j)) {
            posCaisses[indexCaisse] = new Position(j, i);
            indexCaisse++;
          }
        }
      }
      return posCaisses;
    }

    // Renvoie true si toutes les caisses sont sur des buts
    private boolean niveauTerminee(Position[] positionsCaisses) {
      int nbBut = 0;
      for (int i = 0; i < positionsCaisses.length; i++) {
        for (int j = 0; j < posButs.length; j++) {
          if (positionsCaisses[i].equals(posButs[j])) {
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

    private Niveau copieNiveauAvecCaisseAvecJoueur(Niveau niveauSansCaisseSansJoueur, Position[] posCaisses,
        int posLJoueur, int posCJoueur) {
      Niveau niveauAvecCaisseAvecJoueur = niveauSansCaisseSansJoueur.clone();
      // Parcours tout le niveau et ajoute les caisses
      for (int i = 0; i < posCaisses.length; i++) {
        niveauAvecCaisseAvecJoueur.cases[posCaisses[i].ligne()][posCaisses[i].colonne()] = Niveau.CAISSE;
      }
      // Ajoute le joueur
      niveauAvecCaisseAvecJoueur.cases[posLJoueur][posCJoueur] = Niveau.POUSSEUR;
      return niveauAvecCaisseAvecJoueur;
    }

    // Renvoie vrai si la configuration existe déjà dans le tableau
    private boolean dejaVu(Position joueur, Position[] posCaisses, int pere) {
      for (int i = 0; i < indexAjout; i++) {
        // Vérifie si la position après le déplacement du joueur sont les mêmes
        if (etats[i].positionApresDeplacement.equals(joueur)) {
          boolean caissesEgales = true;
          // Vérifie si les positions des caisses sont les mêmes
          for (int j = 0; j < posCaisses.length; j++) {
            if (!etats[i].posCaisses[j].equals(posCaisses[j])) {
              caissesEgales = false;
              break;
            }
          }
          if (caissesEgales) {
            return true;
          }
        }
      }
      return false;
    }

    // On vérifie si on ne va pas mettre la caisse dans un coin (qui n'est pas un
    // but)
    private boolean mouvementBloquant(Position posCaisse) {
      // On vérifie les cas ou il y a un mur dans 2 coins adjacents
      boolean caseHautMur = niveau.aMur(posCaisse.haut().ligne(), posCaisse.haut().colonne());
      boolean caseDroiteMur = niveau.aMur(posCaisse.droite().ligne(), posCaisse.droite().colonne());
      boolean caseGaucheMur = niveau.aMur(posCaisse.gauche().ligne(), posCaisse.gauche().colonne());
      boolean caseBasMur = niveau.aMur(posCaisse.bas().ligne(), posCaisse.bas().colonne());
      // On vérifie si la caisse est dans un coin
      if (caseHautMur && caseDroiteMur || caseDroiteMur && caseBasMur || caseBasMur && caseGaucheMur
          || caseGaucheMur && caseHautMur) {
        return true;
      }
      return false;
    }

    private void ajouteEtat(EtatDuNiveau etat) {
      if (!dejaVu(etat.positionApresDeplacement, etat.posCaisses, etat.pere)) {
        etats[indexAjout] = etat;
        indexAjout++;
      }
    }

    private void extraireChemin() {
      // Cas où l'on n'a pas trouvé de solution
      if (!niveauTerminee(etats[indexAjout - 1].posCaisses)) {
        System.out.println("Solution non trouvé");
        return;
      }
      int[] chemin = new int[indexAjout];
      int indexTemp = indexAjout - 1;
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
        Position positionJoueur = etats[chemin[indexCheminRetour]].positionAvantDeplacement;
        Position positionCaisse = etats[chemin[indexCheminRetour]].positionApresDeplacement;
        int vecteurLigne = positionCaisse.ligne() - positionJoueur.ligne();
        int vecteurColonne = positionCaisse.colonne() - positionJoueur.colonne();
        Position positionCaisseDestination = new Position(positionCaisse.colonne() + vecteurColonne,
            positionCaisse.ligne() + vecteurLigne);
        // On calcul le mouvement du joueur
        MouvementJoueur mouvement = new MouvementJoueur(positionJoueur, positionCaisse, positionCaisseDestination,
            vecteurLigne, vecteurColonne);
        mouvementsInner[indexMouvementJoueur] = mouvement;
        indexCheminRetour--;
        indexMouvementJoueur++;
      }

      mouvements = mouvementsInner;
    }

    public void resoudre() {
      indexParcours = 0;
      boolean solutionTrouvee = false;
      // Tant qu'il reste des élements dans la liste
      while (etats[indexParcours] != null && !solutionTrouvee) {
        // On récupère l'état de l'indice actuel
        EtatDuNiveau etatCourant = etats[indexParcours];
        // On récupère les infos
        int posL = etatCourant.positionApresDeplacement.ligne();
        int posC = etatCourant.positionApresDeplacement.colonne();
        Position[] posCaisses = etatCourant.posCaisses;
        // On récupère le niveau actuel
        Niveau niveauCourant = copieNiveauAvecCaisseAvecJoueur(niveauSansCaisse, posCaisses, posL, posC);
        // On récupère les cases accessibles
        CasesAccessibles cases = new CasesAccessibles(niveauCourant, new Position(posC, posL));
        // On récupère les mouvements possibles
        CaissesDeplacables caisses = new CaissesDeplacables(cases.nbCaissesDeplacables);
        caisses.trouverMouvementsCaisses(niveauCourant, cases);

        if (indexParcours % 1000 == 0) {
          System.out.println("Etat " + indexParcours + " | Pere : " + etatCourant.pere);
        }
        // On ajoute tout les mouvements de caisse possibles au tableau
        for (int i = 0; i < caisses.nbMouvements; i++) {
          // On récupère la nouvelle position du joueur (position actuelle de la caisse)
          int posLNew = caisses.mouvementsPossibles[i][1].ligne();
          int posCNew = caisses.mouvementsPossibles[i][1].colonne();
          int posLAncienne = 0;
          int posCAncienne = 0;
          // On récupère la nouvelle position de la caisse
          Position[] posCaissesNew = new Position[posCaisses.length];
          for (int j = 0; j < posCaisses.length; j++) {
            // Cas où la caisse bouge
            if (posCaisses[j].equals(caisses.mouvementsPossibles[i][1])) {
              posLAncienne = caisses.mouvementsPossibles[i][0].ligne();
              posCAncienne = caisses.mouvementsPossibles[i][0].colonne();
              posCaissesNew[j] = new Position(caisses.mouvementsPossibles[i][2].colonne(),
                  caisses.mouvementsPossibles[i][2].ligne());
              // Cas où la caisse ne bouge pas
            } else {
              posCaissesNew[j] = new Position(posCaisses[j].colonne(), posCaisses[j].ligne());
            }
          }

          // On ajoute des heuristiques
          // On vérifie si le mouvement est bloquant
          // TODO: POURQUOI 1 MARCHE ET PAS 2 ???
          // if (!mouvementBloquant(caisses.mouvementsPossibles[i][2])) {
          if (!mouvementBloquant(caisses.mouvementsPossibles[i][1])) {
            // On ajoute l'état
            ajouteEtat(
                new EtatDuNiveau(new Position(posCNew, posLNew), new Position(posCAncienne, posLAncienne),
                    posCaissesNew,
                    indexParcours));
            // On vérifie si le niveau est terminé
            if (niveauTerminee(posCaissesNew)) {
              System.out.println("Niveau terminé");
              solutionTrouvee = true;
              break;
            }
          }

        }
        indexParcours++;
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
        int caisseC = casesAccessibles.caissesAccessibles[i].colonne();
        int caisseL = casesAccessibles.caissesAccessibles[i].ligne();
        Position positionCaisse = new Position(caisseC, caisseL);
        // On regarde si la case en haut est accessible
        if (casesAccessibles.existe(positionCaisse.haut()) != EXISTE_PAS) {
          if (niveau.peutAccepterCaisse(caisseL + 1, caisseC)) {
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
          if (niveau.peutAccepterCaisse(caisseL - 1, caisseC)) {
            mouvementsPossibles[nbMouvements][0] = positionCaisse.bas();
            mouvementsPossibles[nbMouvements][1] = positionCaisse;
            mouvementsPossibles[nbMouvements][2] = positionCaisse.haut();
            nbMouvements++;
          }
        }
        // Pareil pour la gauche
        if (casesAccessibles.existe(positionCaisse.gauche()) != EXISTE_PAS) {
          if (niveau.peutAccepterCaisse(caisseL, caisseC + 1)) {
            mouvementsPossibles[nbMouvements][0] = positionCaisse.gauche();
            mouvementsPossibles[nbMouvements][1] = positionCaisse;
            mouvementsPossibles[nbMouvements][2] = positionCaisse.droite();
            nbMouvements++;
          }
        }
        // Pareil pour la droite
        if (casesAccessibles.existe(positionCaisse.droite()) != EXISTE_PAS) {
          if (niveau.peutAccepterCaisse(caisseL, caisseC - 1)) {
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
            if (caissesAccessibles[i].colonne() == p.colonne() && caissesAccessibles[i].ligne() == p.ligne())
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
        Position p = new Position(positions[i].colonne(), positions[i].ligne());
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
        if (positions[i].colonne() == p.colonne() && positions[i].ligne() == p.ligne())
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
      Position joueurDestination = mouvements[i].joueur;
      Position caisse = mouvements[i].caisse;
      Position caisseDestination = mouvements[i].caisseDestination;
      Coup coup = new Coup();
      // On récupère les mouvements
      coup.deplacementPousseur(joueurL, joueurC, joueurDestination.ligne(), joueurDestination.colonne());
      resultat.insereQueue(coup);
      // On déplace la caisse
      coup = new Coup();
      coup.deplacementCaisse(caisse.ligne(), caisse.colonne(), caisseDestination.ligne(), caisseDestination.colonne());
      resultat.insereQueue(coup);
      joueurL = joueurDestination.ligne();
      joueurC = joueurDestination.colonne();
    }

    return resultat;
  }
}
