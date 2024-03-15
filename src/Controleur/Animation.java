
package Controleur;

public abstract class Animation {
  int lenteur;
  int decompte;
  ControleurMediateur control;

  public Animation(int l, ControleurMediateur c) {
    lenteur = l;
    decompte = l;
    control = c;
  }

  public void tictac() {
    decompte--;
    if (decompte <= 0) {
      decompte = lenteur;
      miseAJour();
    }
  }

  public abstract void miseAJour();

  public boolean estTerminee() {
    return false;
  }
}
