package MOTO6809;

import javax.swing.*;
import java.io.*;

public class AssembleurController extends JFrame {
    private AssembleurView view;
    private Memoire memoire;     
    private Registres registres;
    private CPU6809 cpu;

    public AssembleurController(AssembleurView view, Memoire memoire, Registres registres, CPU6809 cpu) {
        this.view = view;
        this.memoire = memoire;
        this.registres = registres;
        this.cpu = cpu;

        // Action bouton Exécuter 
        view.btnRun.addActionListener(e -> {
            String code = view.txtCode.getText();
            try {
                Assembleur assembleur = new Assembleur();
                
                // Charger en ROM à $8000
                int adresse = 0x8000;
                assembleur.assembler(code, memoire, adresse);
                
                // Définir le vecteur de reset
                memoire.forceSetWord(0xFFFE, adresse);
                
                // Reset du CPU (si disponible)
                if (cpu != null) {
                    cpu.reset();
                    System.out.println("✓ CPU reset effectué, PC = $" + 
                        Integer.toHexString(registres.getPC()).toUpperCase());
                } else {
                    // Si cpu est null, positionner PC manuellement
                    registres.setPC(adresse);
                    System.out.println("⚠ CPU non disponible, PC positionné manuellement à $" + 
                        Integer.toHexString(adresse).toUpperCase());
                }
                
                JOptionPane.showMessageDialog(view,
                    "✓ Code assemblé et chargé en ROM à $8000\n" +
                    "✓ Vecteur de reset: $FFFE → $8000\n" +
                    "✓ PC = $" + Integer.toHexString(registres.getPC()).toUpperCase() + "\n\n" +
                    "→ Utilisez 'Step' dans la fenêtre Registres",
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception ex) {
                System.err.println("❌ ERREUR: " + ex.getMessage());
                ex.printStackTrace();
                
                JOptionPane.showMessageDialog(view,
                    "❌ Erreur d'assemblage:\n\n" + ex.getMessage(),
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action bouton Effacer
        view.btnClear.addActionListener(e -> {
            view.txtCode.setText("");
        });

        // Action bouton Ouvrir
        view.btnOpen.addActionListener(e -> openFile());

        // Action bouton Enregistrer
        view.btnSave.addActionListener(e -> saveFile());
    }
    
    // Constructeur par compatibilité (si appelé sans CPU)
    public AssembleurController(AssembleurView view, Memoire memoire, Registres registres) {
        this(view, memoire, registres, null);
    }
    
    // Constructeur par compatibilité (si appelé sans mémoire)
    public AssembleurController(AssembleurView view) {
        this(view, null, null, null);
    }

    // Méthode pour ouvrir un fichier texte
    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(view);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                view.txtCode.read(reader, null);
                System.out.println("✓ Fichier ouvert: " + file.getName());
            } catch (IOException ex) {
                System.err.println("❌ Erreur: " + ex.getMessage());
                JOptionPane.showMessageDialog(view, 
                    "Erreur lors de l'ouverture du fichier : " + ex.getMessage());
            }
        }
    }

    // Méthode pour enregistrer le texte dans un fichier
    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(view);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                view.txtCode.write(writer);
                System.out.println("✓ Fichier enregistré: " + file.getName());
            } catch (IOException ex) {
                System.err.println("❌ Erreur: " + ex.getMessage());
                JOptionPane.showMessageDialog(view, 
                    "Erreur lors de l'enregistrement : " + ex.getMessage());
            }
        }                        
    }
}
