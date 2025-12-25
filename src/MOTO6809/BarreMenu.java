package MOTO6809;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BarreMenu extends JMenuBar {
	private Memoire memoire;

	public BarreMenu(Memoire memoire) {
		this.memoire = memoire;
		String[] menuTitres = { "File", "Search", "Help" };
		for (String titre : menuTitres) {
			JMenu menu = new JMenu(titre);
			ajouterItems(menu, titre);
			add(menu);
		}
	}

	private void ajouterItems(JMenu menu, String titre) {
		if ("File".equals(titre)) {
			JMenuItem loadRom = new JMenuItem("Load ROM...");
			loadRom.addActionListener(e -> chargerROM());
			menu.add(loadRom);
			menu.add(new JMenuItem("Exit"));
		} else if ("Search".equals(titre)) { // CORRECTION 1 : Ajouté ici !
			JMenuItem ops = new JMenuItem("Opérations 6809 - Référence complète");
			ops.setAccelerator(KeyStroke.getKeyStroke("O"));
			ops.addActionListener(e -> new FenetreOperations().setVisible(true));
			menu.add(ops);
		} else if ("Help".equals(titre)) {
			menu.add(new JMenuItem("About Simu6809"));
		} else {
			menu.add(new JMenuItem(titre + " Action 1"));
		}
	}

	private void chargerROM() {
		JFileChooser chooser = new JFileChooser();
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] data = fis.readAllBytes();
				memoire.chargerROM(0x8000, data);
				JOptionPane.showMessageDialog(null, "ROM chargée à 0x8000");
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Erreur de chargement : " + ex.getMessage());
			}
		}
	}
}