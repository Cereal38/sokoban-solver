package Vue;

import java.util.Scanner;

import Modele.Jeu;
import Modele.RedacteurNiveau;
import Patterns.Observateur;

// Interface textuelle permettant de mettre en évidence la modularité de la vue
public class InterfaceTextuelle implements InterfaceUtilisateur, Observateur {
  Jeu j;
  CollecteurEvenements control;
  RedacteurNiveau affichage;

  InterfaceTextuelle(Jeu jeu, CollecteurEvenements c) {
    j = jeu;
    control = c;
    j.ajouteObservateur(this);
    affichage = new RedacteurNiveau(System.out);
  }

  public static void demarrer(Jeu j, CollecteurEvenements c) {
    InterfaceTextuelle vue = new InterfaceTextuelle(j, c);
    c.ajouteInterfaceUtilisateur(vue);
    vue.miseAJour();
    Scanner s = new Scanner(System.in);
    while (true) {
      System.out.print("Commande > ");
      c.toucheClavier(s.next());
    }
  }

  public void toggleFullscreen() {
    System.out.println("Pas de plein écran en mode textuel");
  }

  // On ignore simplement les animations et la direction du pousseur en mode
  // textuel
  @Override
  public void changeEtape() {
  }

  @Override
  public void metAJourDirection(int dL, int dC) {
  }

  @Override
  public void decale(int versL, int versC, double dL, double dC) {
  }

  @Override
  public void miseAJour() {
    affichage.ecrisNiveau(j.niveau());
  }
}
