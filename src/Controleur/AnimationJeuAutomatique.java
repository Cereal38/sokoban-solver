
package Controleur;

import Global.Configuration;
import Modele.Coup;
import Modele.IA;
import Structures.Sequence;

class AnimationJeuAutomatique extends Animation {
  IA joueur;
  Sequence<Coup> enAttente;

  AnimationJeuAutomatique(int lenteur, IA j, ControleurMediateur c) {
    super(lenteur, c);
    joueur = j;
    control = c;
  }

  @Override
  public void miseAJour() {
    if ((enAttente == null) || enAttente.estVide())
      enAttente = joueur.elaboreCoups();
    if ((enAttente == null) || enAttente.estVide())
      Configuration.erreur("Bug : l'IA n'a joué aucun coup");
    else
      control.joue(enAttente.extraitTete());
  }
}
