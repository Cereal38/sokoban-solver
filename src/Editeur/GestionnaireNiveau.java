package Editeur;
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

import Vue.NiveauGraphique;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GestionnaireNiveau extends MouseAdapter {
	NiveauGraphique niv;
	Controleur control;
	DragAndDrop DAD;
	int selection;

	void selectionne(int num) {
		selection = num;
		control.selectionne(num);
	}

	GestionnaireNiveau(NiveauGraphique n, Controleur c, DragAndDrop d) {
		niv = n;
		control = c;
		DAD = d;
	}

	void demarreDAD(Image i, MouseEvent e) {
		DAD.demarre(i, e, niv.largeurCase(), niv.hauteurCase());
	}

	void modifie(int x, int y) {
		int ligne = (y / niv.hauteurCase());
		int colonne = (x / niv.largeurCase());
		control.clicSouris(ligne, colonne);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		modifie(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (DAD.enCours()) {
			Point p = DAD.getPos(niv);
			modifie(p.x, p.y);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (DAD.enCours()) {
			niv.miseAJour();
		}
	}
}
