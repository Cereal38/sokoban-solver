package Modele;

import Global.Configuration;
import Structures.Sequence;

public class Coup {
  Mouvement pousseur, caisse;
  Sequence<Marque> marques;
  int dirPousseurL, dirPousseurC;

  Coup() {
    marques = Configuration.nouvelleSequence();
  }

  private Mouvement creeDeplacement(String nom, Mouvement existant, int dL, int dC, int vL, int vC) {
    if (existant != null) {
      Configuration.alerte("Deplacement " + nom + " déjà présent : " + existant);
    }
    return new Mouvement(dL, dC, vL, vC);
  }

  public void deplacementPousseur(int dL, int dC, int vL, int vC) {
    pousseur = creeDeplacement("pousseur", pousseur, dL, dC, vL, vC);
    dirPousseurL = vL - dL;
    dirPousseurC = vC - dC;
    if (dirPousseurC * dirPousseurC > dirPousseurL * dirPousseurL) {
      dirPousseurL = 0;
      dirPousseurC = dirPousseurC < 0 ? -1 : 1;
    } else {
      dirPousseurL = dirPousseurL < 0 ? -1 : 1;
      dirPousseurC = 0;
    }
  }

  public void deplacementCaisse(int dL, int dC, int vL, int vC) {
    caisse = creeDeplacement("caisse", caisse, dL, dC, vL, vC);
  }

  public Mouvement pousseur() {
    return pousseur;
  }

  public Mouvement caisse() {
    return caisse;
  }

  public void ajouteMarque(int l, int c, int val) {
    Marque m = new Marque(l, c, val);
    marques.insereQueue(m);
  }

  public Sequence<Marque> marques() {
    return marques;
  }

  public int dirPousseurL() {
    return dirPousseurL;
  }

  public int dirPousseurC() {
    return dirPousseurC;
  }
}
