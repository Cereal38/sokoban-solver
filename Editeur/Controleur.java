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
import Vue.CollecteurEvenements;
import Vue.InterfaceUtilisateur;

public class Controleur implements CollecteurEvenements {
	final int INVALIDE = -1;
	final int SOL = 0;
	final int BUT = 1;
	final int MUR = 2;
	final int CAISSE = 3;
	final int POUSSEUR = 4;
	JeuEditeur jeu;
	int selection;

	public Controleur(JeuEditeur j) {
		jeu = j;
		selection = INVALIDE;
	}

	void selectionne(int val) {
		selection = val;
	}

	@Override
	public void clicSouris(int l, int c) {
		switch (selection) {
		case SOL:
			jeu.videCase(l, c);
			break;
		case BUT:
			jeu.ajouteBut(l, c);
			break;
		case MUR:
			jeu.ajouteMur(l, c);
			break;
		case CAISSE:
			jeu.ajouteCaisse(l, c);
			break;
		case POUSSEUR:
			try {
				jeu.ajoutePousseur(l, c);
			} catch (Exception e) {
				Configuration.instance().logger().severe("Impossible d'avoir deux pousseurs");
			}
			break;
		}
	}

	@Override
	public void toucheClavier(String t) {
		switch(t) {
			case "Undo":
				jeu.annuler();
				break;
			case "Redo":
				jeu.refaire();
				break;
		}

	}

	@Override
	public void ajouteInterfaceUtilisateur(InterfaceUtilisateur vue) {
		// Inutile ici, pas d'animations
	}

	@Override
	public void tictac() {
		// Inutile ici, pas d'animations
	}
}
