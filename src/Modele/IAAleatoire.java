package Modele;

import java.util.Random;

import Global.Configuration;
import Structures.Sequence;

class IAAleatoire extends IA {
  Random r;
  // Couleurs au format RGB (rouge, vert, bleu, un octet par couleur)
  final static int VERT = 0x00CC00;
  final static int MARRON = 0xBB7755;

  public IAAleatoire() {
    r = new Random();
  }

  @Override
  public Sequence<Coup> joue() {
    Sequence<Coup> resultat = Configuration.nouvelleSequence();
    Coup coup = null;
    boolean mur = true;
    int dL = 0, dC = 0;
    int nouveauL = 0;
    int nouveauC = 0;

    int pousseurL = niveau.lignePousseur();
    int pousseurC = niveau.colonnePousseur();
    // Mouvement du pousseur
    while (mur) {
      int direction = r.nextInt(2) * 2 - 1;
      if (r.nextBoolean()) {
        dL = direction;
      } else {
        dC = direction;
      }
      nouveauL = pousseurL + dL;
      nouveauC = pousseurC + dC;
      coup = niveau.deplace(dL, dC);
      if (coup == null) {
        if (niveau.aMur(nouveauL, nouveauC))
          Configuration.info("Tentative de déplacement (" + dL + ", " + dC + ") heurte un mur");
        else if (niveau.aCaisse(nouveauL, nouveauC))
          Configuration.info("Tentative de déplacement (" + dL + ", " + dC + ") heurte une caisse non déplaçable");
        else
          Configuration.erreur("Tentative de déplacement (" + dL + ", " + dC + "), erreur inconnue");
        dL = dC = 0;
      } else
        mur = false;
    }

    nouveauL += dL;
    nouveauC += dC;
    // Ajout des marques
    for (int l = 0; l < niveau.lignes(); l++)
      for (int c = 0; c < niveau.colonnes(); c++) {
        int marque = niveau.marque(l, c);
        if (marque == VERT)
          coup.ajouteMarque(l, c, 0);
      }
    coup.ajouteMarque(pousseurL, pousseurC, MARRON);
    while (niveau.estOccupable(nouveauL, nouveauC)) {
      int marque = niveau.marque(nouveauL, nouveauC);
      if (marque == 0)
        coup.ajouteMarque(nouveauL, nouveauC, VERT);
      nouveauL += dL;
      nouveauC += dC;
    }
    resultat.insereQueue(coup);
    return resultat;
  }
}
