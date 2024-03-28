package Global;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import Structures.Sequence;
import Structures.SequenceListe;
import Structures.SequenceTableau;

public class Configuration {
  static final int silence = 1;
  public static final String typeInterface = "Graphique";
  static final String typeSequences = "Liste";
  public static final double vitesseAnimations = 0.15;
  public static final int lenteurPas = 15;
  public static final boolean animations = true;
  public static final String IA = "Solver";
  public static int lenteurJeuAutomatique = 15;

  public static InputStream ouvre(String s) {
    InputStream in = null;
    try {
      in = new FileInputStream("res/" + s);
    } catch (FileNotFoundException e) {
      erreur("impossible de trouver le fichier " + s);
    }
    return in;
  }

  public static void affiche(int niveau, String message) {
    if (niveau > silence)
      System.err.println(message);
  }

  public static void info(String s) {
    affiche(1, "INFO : " + s);
  }

  public static void alerte(String s) {
    affiche(2, "ALERTE : " + s);
  }

  public static void erreur(String s) {
    affiche(3, "ERREUR : " + s);
    System.exit(1);
  }

  public static <E> Sequence<E> nouvelleSequence() {
    switch (typeSequences) {
      case "Liste":
        return new SequenceListe<>();
      case "Tableau":
        return new SequenceTableau<>();
      default:
        erreur("Type de séquence invalide : " + typeSequences);
        return null;
    }
  }
}
