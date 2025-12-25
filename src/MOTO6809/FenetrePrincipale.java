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

    private JWindow fondNoir; // <- fenêtre noire plein écran

    public FenetrePrincipale() {

        // ===== FOND NOIR PLEIN ÉCRAN =====
    	fondNoir = new JWindow();
    	fondNoir.getContentPane().setBackground(Color.BLACK);
    	fondNoir.setBackground(new Color(0, 0, 0, 255));
    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	fondNoir.setBounds(0, 0, screenSize.width, screenSize.height);
    	// Retirer AlwaysOnTop pour laisser les autres fenêtres par-dessus
    	// fondNoir.setAlwaysOnTop(true);
    	fondNoir.setVisible(true);

        // ===== FENETRE PRINCIPALE EXISTANTE =====
        setTitle("Simu6809");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(screenSize.width, 90);
        setLayout(new BorderLayout());
        setResizable(false);
        setAlwaysOnTop(true);
        setLocation(0, 0);

        setJMenuBar(new BarreMenu(memoire));

        JButton Registre = new JButton("📁");
        Registre.setBorderPainted(false);
        Registre.setContentAreaFilled(false);
        Registre.setFocusPainted(false);
        Registre.setToolTipText("Registre");
        Registre.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Registre.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); // icône haute qualité

        JButton Emulateur = new JButton("🖥️");
        Emulateur.setBorderPainted(false);
        Emulateur.setContentAreaFilled(false);
        Emulateur.setFocusPainted(false);
        Emulateur.setToolTipText("Éditeur Assembleur 6809");
        Emulateur.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Emulateur.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        Emulateur.addActionListener(e -> {
            AssembleurView view = new AssembleurView();
            new AssembleurController(view, memoire, registres);
            view.setVisible(true);
        });

        JButton RAM = new JButton("💾");
        RAM.setBorderPainted(false);
        RAM.setContentAreaFilled(false);
        RAM.setFocusPainted(false);
        RAM.setToolTipText("RAM");
        RAM.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        RAM.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JButton ROM = new JButton("💿");
        ROM.setBorderPainted(false);
        ROM.setContentAreaFilled(false);
        ROM.setFocusPainted(false);
        ROM.setToolTipText("ROM");
        ROM.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ROM.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JButton Synchroniser = new JButton("🔄");
        Synchroniser.setBorderPainted(false);
        Synchroniser.setContentAreaFilled(false);
        Synchroniser.setFocusPainted(false);
        Synchroniser.setToolTipText("Synchroniser RAM/ROM");
        Synchroniser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Synchroniser.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        Synchroniser.addActionListener(e -> synchroniserFenetres());

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

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(250, 250, 252));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(40, 100, 40, 100);

        gbc.gridx = 0; panel.add(Registre, gbc);
        gbc.gridx = 1; panel.add(RAM, gbc);
        gbc.gridx = 2; panel.add(ROM, gbc);
        gbc.gridx = 3; panel.add(Emulateur, gbc);
        gbc.gridx = 4; panel.add(Synchroniser, gbc);

        add(panel);
        setVisible(true);

        // ===== FIN FENETRE PRINCIPALE =====
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