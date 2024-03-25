package Modele;

import Global.Configuration;
import Structures.Sequence;

public abstract class IA {
  private Jeu jeu;
  Niveau niveau;

  public static IA nouvelle(Jeu j) {
    IA resultat = null;
    // Méthode de fabrication pour l'IA, qui crée le bon objet selon la config
    String type = Configuration.IA;
    switch (type) {
      case "Solver":
        resultat = new IASolver();
        break;
      case "Aleatoire":
        resultat = new IAAleatoire();
        break;
      case "Teleportations":
        resultat = new IATeleportations();
        break;
      case "ParcoursFixe":
        resultat = new IAParcoursFixe();
        break;
      default:
        Configuration.erreur("IA de type " + type + " non supportée");
    }
    if (resultat != null) {
      resultat.jeu = j;
    }
    return resultat;
  }

  public final Sequence<Coup> elaboreCoups() {
    niveau = jeu.niveau().clone();
    return joue();
  }

  Sequence<Coup> joue() {
    return null;
  }
}
