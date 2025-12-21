package MOTO6809;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FenetrePrincipale extends JFrame {
	public Memoire memoire = new Memoire();
	public Registres registres = new Registres();
	public CPU6809 cpu = new CPU6809(registres, memoire);
	private List<JFrame> fenetresOuvertes = new ArrayList<>(); // Liste pour synchroniser

	public FenetrePrincipale() {
		setTitle("Simu6809");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setSize(1440, 63);
		setSize(Toolkit.getDefaultToolkit().getScreenSize().width, 90);
		setLayout(new BorderLayout()); // Utilise BorderLayout pour fixer la barre en haut
		// setLocationRelativeTo(null);
		// setUndecorated(true); // Supprimer les décorations (bordures, titre) pour une
		// apparence fixe
		// Positionner en haut de l'écran et définir la taille (pleine largeur, hauteur
		// fixe)
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// setSize(screenSize.width, 100); // Hauteur de 100 pixels, ajustez selon vos
		// besoins

		setLayout(new BorderLayout());// Utiliser BorderLayout pour organiser le contenu
		setResizable(false); // Dimension reste fixe
		setAlwaysOnTop(true); // Rendre la fenêtre toujours en haut
		setLocation(0, 0); // En haut à gauche

		setJMenuBar(new BarreMenu(memoire)); // ← on passe la mémoire

		// Créer le bouton Registre
		JButton Registre = new JButton("Registre");
		Registre.setText("📁");
		Registre.setBorderPainted(false);
		Registre.setContentAreaFilled(false);
		Registre.setFocusPainted(false);
		Registre.setToolTipText("Registre");
		Registre.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Créer le bouton Émulateur (Assembleur/Éditeur)
		JButton Emulateur = new JButton("Émulateur");
		Emulateur.setText("🖥️");
		Emulateur.setBorderPainted(false);
		Emulateur.setContentAreaFilled(false);
		Emulateur.setFocusPainted(false);
		Emulateur.setToolTipText("Éditeur Assembleur 6809");
		Emulateur.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Action du bouton : ouvre votre éditeur assembleur existant
		Emulateur.addActionListener(e -> {
			AssembleurView view = new AssembleurView();
			new AssembleurController(view, memoire, registres); // Voilà la correction clé
			view.setVisible(true);
		});

		// Créer le bouton RAM
		JButton RAM = new JButton("RAM");
		RAM.setText("💾");
		RAM.setBorderPainted(false);
		RAM.setContentAreaFilled(false);
		RAM.setFocusPainted(false);
		RAM.setToolTipText("RAM");
		RAM.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Créer le bouton ROM
		JButton ROM = new JButton("ROM");
		ROM.setText("💿");
		ROM.setBorderPainted(false);
		ROM.setContentAreaFilled(false);
		ROM.setFocusPainted(false);
		ROM.setToolTipText("ROM");
		ROM.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Créer le bouton du Synchronisation
		JButton Synchroniser = new JButton("🔄");
		Synchroniser.setBorderPainted(false);
		Synchroniser.setContentAreaFilled(false);
		Synchroniser.setFocusPainted(false);
		Synchroniser.setToolTipText("Synchroniser RAM/ROM");
		Synchroniser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		Synchroniser.addActionListener(e -> synchroniserFenetres());

		// Action au clic
		// Actions au clic : Ajoute à la liste
		Registre.addActionListener(e -> {
			FenetreRegistre fen = new FenetreRegistre(this, registres, cpu);
			fenetresOuvertes.add(fen);
		});
		RAM.addActionListener(e -> {
			FenetreRAM fen = new FenetreRAM(this, memoire);
			fenetresOuvertes.add(fen);
		});
		ROM.addActionListener(e -> {
			FenetreROM fen = new FenetreROM(this, memoire);
			fenetresOuvertes.add(fen);
		});

		// Mise en page centrée et élégante
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(new Color(250, 250, 252));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(40, 100, 40, 100);

		// Ajoutez Registre dans le panel
		gbc.gridx = 0;
		panel.add(Registre, gbc);
		// Ajoutez RAM dans le panel
		gbc.gridx = 1;
		panel.add(RAM, gbc);
		// Ajoutez ROM dans le panel
		gbc.gridx = 2;
		panel.add(ROM, gbc);
		// Ajoutez Émulateur dans le panel
		gbc.gridx = 3;
		panel.add(Emulateur, gbc);
		// Ajoutez Synchronisation dans le panel
		gbc.gridx = 4;
		panel.add(Synchroniser, gbc);

		add(panel);
		setVisible(true);
	}

	private void synchroniserFenetres() {
		for (JFrame fen : fenetresOuvertes) {
			if (fen instanceof FenetreRAM) {
				((FenetreRAM) fen).rafraichir();
			} else if (fen instanceof FenetreROM) {
				((FenetreROM) fen).rafraichir();
			}
		}
	}
}