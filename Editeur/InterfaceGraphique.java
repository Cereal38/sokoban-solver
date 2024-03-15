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
import Vue.NiveauGraphique;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class InterfaceGraphique implements Runnable {
	final String[] nomsImages = { "Sol", "But", "Mur", "Caisse", "Pousseur_2_0" };
	JeuEditeur jeu;
	Controleur control;
	GestionnaireNiveau sourisNiveau;
	JFrame frame;
	DragAndDrop DAD;
	NiveauGraphique niv;
	JRadioButton[] selections;
	Image [] img;

	public static void demarrer(JeuEditeur j) {
		InterfaceGraphique vue = new InterfaceGraphique(j);
		SwingUtilities.invokeLater(vue);
	}

	public InterfaceGraphique(JeuEditeur j) {
		jeu = j;
		control = new Controleur(jeu);
	}

	@Override
	public void run() {
		frame = new JFrame("Editeur");
		DAD = new DragAndDrop(frame);
		niv = new VueNiveauEditeur(jeu, DAD);
		niv.setPreferredSize(new Dimension(600, 400));
		sourisNiveau = new GestionnaireNiveau(niv, control, DAD);
		niv.addMouseListener(sourisNiveau);

		Box boiteLaterale = Box.createVerticalBox();
		boiteLaterale.add(Box.createGlue());
		selections = new JRadioButton[nomsImages.length];
		img = new Image[nomsImages.length];
		ButtonGroup gp = new ButtonGroup();

		for (int i = 0; i < selections.length; i++) {
			img[i] = niv.lisImage(nomsImages[i]);
			final int largeur = 50;
			final int hauteur = 50;
			Image resized = img[i].getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(resized);
			JLabel visuel = new JLabel(icon);
			selections[i] = new JRadioButton();
			GestionnaireSelection selecteur = new GestionnaireSelection(this, sourisNiveau, i);
			selections[i].addActionListener(selecteur);
			visuel.addMouseListener(selecteur);
			gp.add(selections[i]);
			Box bloc = Box.createHorizontalBox();
			bloc.add(visuel);
			bloc.add(selections[i]);
			boiteLaterale.add(bloc);
			boiteLaterale.add(Box.createGlue());
		}

		boiteLaterale.add(Vue.InterfaceGraphique.creerAnnulerRefaire(jeu, control));
		JButton sauve = new JButton("Sauve");
		sauve.setAlignmentX(Component.CENTER_ALIGNMENT);
		sauve.setFocusable(false);
		boiteLaterale.add(sauve);
		sauve.addActionListener(new AdaptateurSauvegarde(this));
		boiteLaterale.add(Box.createGlue());

		frame.add(niv);
		frame.add(boiteLaterale, BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public void sauve() {
		JFileChooser selecteur = new JFileChooser();
		selecteur.setDialogTitle("Fichier de niveaux");
		selecteur.setFileFilter(new FileNameExtensionFilter("Niveaux", "*.txt"));
		int resultat = selecteur.showSaveDialog(frame);
		if (resultat == JFileChooser.APPROVE_OPTION) {
			File fichier = selecteur.getSelectedFile();
			try {
				OutputStream out = new FileOutputStream(fichier);
				jeu.sauve(out);
			} catch (Exception e) {
				Configuration.instance().logger().severe(e.toString());
			}
		}
	}

	void selectionne(int num) {
		selections[num].setSelected(true);
	}

	Image image(int num) {
		return img[num];
	}
}
