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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Nous choisissons d'implémenter le drag&drop à l'aide d'un composant qui retransmet systématiquement
// tous les évènements aux composants situées sous lui. Ainsi les composants de notre application reçoivent
// les évènements classiques comme mouseDragged ou MouseReleased et peuvent savoir s'il s'agit d'un
// drag&drop en déterminant si celui ci est actif.

// Du coté du drag&drop lui même, il est implémenté à l'aide du glassPane (voir le tutoriel d'oracle sur les root panes :
// https://docs.oracle.com/javase/tutorial/uiswing/components/rootpane.html) et se contente d'assurer la partie
// affichage de l'image "trainée" par la souris. L'ensemble du code étant peu volumineux et très interdépendant, les
// écouteurs de souris et la partie affichage (paintComponent) sont regroupés dans une seule classe : DragAndDrop.

class DragAndDrop extends JComponent implements MouseListener, MouseMotionListener {
	Component conteneur;
	int x, y;
	int largeur, hauteur;
	Image img;
	boolean actif;

	DragAndDrop(JFrame f) {
		conteneur = f.getContentPane();
		f.setGlassPane(this);
		setVisible(true);
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		metAJour(e.getX(), e.getY());
		forwardEvent(e);
		if (actif) {
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		metAJour(e.getX(), e.getY());
		forwardEvent(e);
		if (actif) {
			actif = false;
			repaint();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gc = (Graphics2D) g;

		if (actif) {
			gc.drawImage(img, x-largeur/2, y-hauteur/2, null);
		}
	}

	void metAJour(int x, int y) {
		this.x = x;
		this.y = y;
	}

	// La principale difficulté technique du drag&drop est qu'il s'affiche au desssus de divers composants,
	// il est donc amené à effectuer beaucoup de conversion de coordonnées (changement de repère entre composants)
	void demarre(Image i, MouseEvent e, double l, double h) {
		actif = true;
		Point cp = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), this);
		metAJour(cp.x, cp.y);
		largeur = (int) l;
		hauteur = (int) h;
		img = i.getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH);
		repaint();
	}

	public Point getPos(Component dest) {
		return SwingUtilities.convertPoint(this, x, y, dest);
	}

	public boolean enCours() {
		return actif;
	}

	// Le GlassPane interceptant tous les evenements, il nous faut une methode pour
	// retransmettre ceux qui concernent d'autres composants, voir le tutoriel sur les root panes :
	// https://docs.oracle.com/javase/tutorial/uiswing/components/rootpane.html
	public void forwardEvent(MouseEvent e) {
		Point p = e.getPoint();
		Point cp = SwingUtilities.convertPoint(this, p, conteneur); // .getContentPane());
		Component c = SwingUtilities.getDeepestComponentAt(conteneur, cp.x, cp.y);
		if ((c != null) && (c != this)) {
			Point dp = SwingUtilities.convertPoint(conteneur, cp, c);
			c.dispatchEvent(new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers(), dp.x, dp.y, e.getClickCount(),
					e.isPopupTrigger()));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		forwardEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		forwardEvent(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		forwardEvent(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		forwardEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		forwardEvent(e);
	}
}