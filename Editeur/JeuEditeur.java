/*
 * Sokoban - Encore une nouvelle version (à but pédagogique) du célèbre jeu
 * Copyright (C) 2018 Guillaume Huard
 *
 * Ce programme est libre, vous pouvez le redistribuer et/ou le
 * modifier selon les termes de la Licence Publique Générale GNU publiée par la
 * Free Software Foundation (version 2 ou bien toute autre version ultérieure
 * choisie par vous).
 *
 * Ce programme est distribué car potentiellement utile, mais SANS
 * AUCUNE GARANTIE, ni explicite ni implicite, y compris les garanties de
 * commercialisation ou d'adaptation dans un but spécifique. Reportez-vous à la
 * Licence Publique Générale GNU pour plus de détails.
 *
 * Vous devez avoir reçu une copie de la Licence Publique Générale
 * GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
 * États-Unis.
 *
 * Contact:
 *          Guillaume.Huard@imag.fr
 *          Laboratoire LIG
 *          700 avenue centrale
 *          Domaine universitaire
 *          38401 Saint Martin d'Hères
 */
package Editeur;

import Global.Configuration;
import Modele.Coup;
import Modele.Jeu;
import Modele.Niveau;
import Modele.RedacteurNiveau;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/* Cette classe tient le même rôle que dans le Sokoban :
 * c'est un point d'entrée unique vers l'ensemble des niveaux
 * et, en particulier le niveau en cours.
 * Par soucis de simplicité de gestion, on charge tous les niveaux d'un coup.
 */
class JeuEditeur extends Jeu {
	List<Niveau> niveaux;
	ListIterator<Niveau> position;

	JeuEditeur() {
		niveaux = new ArrayList<>();
		niveaux.add(n);
		while (n != null) {
			prochainNiveau();
			if (n != null) {
				niveaux.add(n);
			}
		}
		position = niveaux.listIterator();
		n = position.next();
		metAJour();
	}

	@Override
	public boolean peutAnnuler() {
		position.previous();
		boolean resultat = position.hasPrevious();
		position.next();
		return resultat;
	}

	@Override
	public boolean peutRefaire() {
		return position.hasNext();
	}

	@Override
	public Coup annuler() {
		position.previous();
		n = position.previous();
		position.next();
		metAJour();
		return null;
	}

	@Override
	public Coup refaire() {
		n = position.next();
		metAJour();
		return null;
	}

	void sauve(OutputStream out) {
		RedacteurNiveau redac = new RedacteurNiveau(out);
		Iterator<Niveau> it = niveaux.iterator();
		while (it.hasNext()) {
			redac.ecrisNiveau(it.next());
		}
	}

	void videCase(int l, int c) {
		n.supprime(n.contenu(l, c), l, c);
		n.videCase(l, c);
		metAJour();
	}

	void ajouteBut(int l, int c) {
		if (!n.aMur(l, c))
			n.ajouteBut(l, c);
		metAJour();
	}

	void ajouteMur(int l, int c) {
		if (!n.aCaisse(l, c) && !n.aBut(l, c) && !n.aPousseur(l, c))
			n.ajouteMur(l, c);
		metAJour();
	}

	void ajouteCaisse(int l, int c) {
		if (!n.aMur(l, c) && !n.aPousseur(l, c))
			n.ajouteCaisse(l, c);
		metAJour();
	}

	void ajoutePousseur(int l, int c) {
		try {
			if (!n.aMur(l, c) && !n.aCaisse(l, c))
				n.ajoutePousseur(l, c);
		} catch (Exception e) {
			Configuration.instance().logger().severe("Impossible d'avoir deux pousseurs");
		}
		metAJour();
	}
}
