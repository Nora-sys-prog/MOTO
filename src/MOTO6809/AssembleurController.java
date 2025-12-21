/*package MOTO6809;

import javax.swing.*;
import java.io.*;


public class AssembleurController extends JFrame{
	private AssembleurView view;
	 private Memoire memoire;     
	    private Registres registres;

	    public AssembleurController(AssembleurView view, Memoire memoire, Registres registres) {
	        this.view = view;
	        this.memoire = memoire;
	        this.registres = registres;

	        // Action bouton Exécuter 
	        view.btnRun.addActionListener(e -> {
	            String code = view.txtCode.getText();
	            try {
	                Assembleur assembleur = new Assembleur();
	                assembleur.assembler(code, memoire, 0x1000);  // Charge en RAM
	                registres.setPC(0x1000);

	                JOptionPane.showMessageDialog(view,
	                    "Code assemblé et chargé en mémoire à $1000 !\nPC positionné.",
	                    "Succès", JOptionPane.INFORMATION_MESSAGE);
	            } catch (Exception ex) {
	                JOptionPane.showMessageDialog(view,
	                    "Erreur d'assemblage :\n" + ex.getMessage(),
	                    "Erreur", JOptionPane.ERROR_MESSAGE);
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
        
     // Boutons existants (inchangés)
        view.btnClear.addActionListener(e -> view.txtCode.setText(""));
        view.btnOpen.addActionListener(e -> openFile());
        view.btnSave.addActionListener(e -> saveFile());
    }
	    
	 // Constructeur par compatibilité (si appelé sans mémoire)
	    public AssembleurController(AssembleurView view) {
	        this(view, null, null); // Pour éviter les erreurs si appelé à l’ancienne
	    }

    // Méthode pour ouvrir un fichier texte
    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(view);
        if(option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                view.txtCode.read(reader, null);
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(view, "Erreur lors de l'ouverture du fichier : " + ex.getMessage());
            }
        }
    }

    // Méthode pour enregistrer le texte dans un fichier
    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(view);
        if(option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                view.txtCode.write(writer);
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(view, "Erreur lors de l'enregistrement : " + ex.getMessage());
            }
        }                        
    }

}*/

/*package MOTO6809;

import javax.swing.*;
import java.io.*;

public class AssembleurController extends JFrame {
    private AssembleurView view;
    private Memoire memoire;     
    private Registres registres;

    public AssembleurController(AssembleurView view, Memoire memoire, Registres registres) {
        this.view = view;
        this.memoire = memoire;
        this.registres = registres;

        // Action bouton Exécuter 
        view.btnRun.addActionListener(e -> {
            String code = view.txtCode.getText();
            
            // ========== DEBUG - DÉBUT ==========
            System.out.println("\n");
            System.out.println("════════════════════════════════════════");
            System.out.println("        ASSEMBLAGE DU CODE");
            System.out.println("════════════════════════════════════════");
            System.out.println("Code à assembler:");
            System.out.println("----------------------------------------");
            System.out.println(code);
            System.out.println("----------------------------------------");
            // ========== DEBUG - FIN ==========
            
            try {
                Assembleur assembleur = new Assembleur();
                
                System.out.println("\n→ Assemblage en cours...");
                assembleur.assembler(code, memoire, 0x1000);
                System.out.println("✓ Assemblage terminé avec succès!");
                
                System.out.println("\n→ Positionnement du PC à $1000...");
                registres.setPC(0x1000);
                System.out.printf("✓ PC = $%04X\n", registres.getPC());
                
                // ===== VÉRIFICATION DE LA MÉMOIRE =====
                System.out.println("\n========================================");
                System.out.println("  VÉRIFICATION DU CODE EN MÉMOIRE");
                System.out.println("========================================");
                System.out.println("Contenu de la mémoire à partir de $1000:");
                System.out.println("----------------------------------------");
                
                // Afficher les 16 premiers bytes
                for (int i = 0; i < 16; i++) {
                    int addr = 0x1000 + i;
                    byte value = memoire.getByte(addr);
                    System.out.printf("$%04X: $%02X", addr, value & 0xFF);
                    
                    // Ajouter une note si c'est un opcode connu
                    int opcode = value & 0xFF;
                    String note = "";
                    switch (opcode) {
                        case 0x86: note = " (LDA immediate)"; break;
                        case 0x96: note = " (LDA direct)"; break;
                        case 0xB6: note = " (LDA extended)"; break;
                        case 0x8E: note = " (LDX immediate)"; break;
                        case 0xCC: note = " (LDD immediate)"; break;
                        case 0x97: note = " (STA direct)"; break;
                        case 0x12: note = " (NOP)"; break;
                        case 0x3F: note = " (SWI)"; break;
                        case 0x00: 
                            if (i > 0) note = " (vide)";
                            break;
                    }
                    System.out.println(note);
                }
                
                System.out.println("========================================");
                System.out.println("\n✓ Code assemblé et chargé en mémoire!");
                System.out.println("✓ PC positionné à $1000");
                System.out.println("→ Utilisez 'Step' dans la fenêtre Registres");
                System.out.println("════════════════════════════════════════\n");

                JOptionPane.showMessageDialog(view,
                    "Code assemblé et chargé en mémoire à $1000 !\n" +
                    "PC positionné à $1000.\n\n" +
                    "Utilisez 'Step' dans la fenêtre Registres.\n" +
                    "(Regardez aussi la console Java pour voir les détails)",
                    "Succès", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception ex) {
                System.err.println("\n❌ ERREUR D'ASSEMBLAGE:");
                System.err.println("----------------------------------------");
                System.err.println(ex.getMessage());
                ex.printStackTrace();
                System.err.println("========================================\n");
                
                JOptionPane.showMessageDialog(view,
                    "Erreur d'assemblage :\n" + ex.getMessage() + 
                    "\n\nVoir la console pour plus de détails.",
                    "Erreur", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action bouton Effacer
        view.btnClear.addActionListener(e -> {
            view.txtCode.setText("");
            System.out.println("✓ Éditeur effacé");
        });

        // Action bouton Ouvrir
        view.btnOpen.addActionListener(e -> openFile());

        // Action bouton Enregistrer
        view.btnSave.addActionListener(e -> saveFile());
    }
    
    // Constructeur par compatibilité (si appelé sans mémoire)
    public AssembleurController(AssembleurView view) {
        this(view, null, null);
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
                System.err.println("❌ Erreur lors de l'ouverture: " + ex.getMessage());
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
                System.err.println("❌ Erreur lors de l'enregistrement: " + ex.getMessage());
                JOptionPane.showMessageDialog(view, 
                    "Erreur lors de l'enregistrement : " + ex.getMessage());
            }
        }                        
    }
}*/

/*package MOTO6809;

import javax.swing.*;
import java.io.*;

public class AssembleurController extends JFrame {
    private AssembleurView view;
    private Memoire memoire;     
    private Registres registres;
    private CPU6809 cpu;  // ← AJOUTER

    public AssembleurController(AssembleurView view, Memoire memoire, Registres registres, CPU6809 cpu) {
        this.view = view;
        this.memoire = memoire;
        this.registres = registres;
        this.cpu = cpu;  // ← AJOUTER

        // Action bouton Exécuter 
        view.btnRun.addActionListener(e -> {
            String code = view.txtCode.getText();
            try {
                Assembleur assembleur = new Assembleur();
                
                // ===== CHANGÉ DE 0x1000 À 0x8000 =====
                int adresse = 0x8000;  // ← ROM au lieu de RAM
                assembleur.assembler(code, memoire, adresse);
                
                // ===== AJOUTER CES 2 LIGNES =====
                memoire.forceSetWord(0xFFFE, adresse);  // Vecteur de reset
                cpu.reset();  // Reset du CPU
                // ================================
                
                JOptionPane.showMessageDialog(view,
                    "Code assemblé et chargé en ROM à $8000 !\n" +
                    "PC positionné à $8000.\n" +
                    "Utilisez 'Step' dans la fenêtre Registres.",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view,
                    "Erreur d'assemblage :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        view.btnClear.addActionListener(e -> view.txtCode.setText(""));
        view.btnOpen.addActionListener(e -> openFile());
        view.btnSave.addActionListener(e -> saveFile());
    }
    
    // ===== AJOUTER CES CONSTRUCTEURS =====
    public AssembleurController(AssembleurView view, Memoire memoire, Registres registres) {
        this(view, memoire, registres, null);
    }
    
    public AssembleurController(AssembleurView view) {
        this(view, null, null, null);
    }
    // =====================================

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(view);
        if(option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                view.txtCode.read(reader, null);
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(view, "Erreur lors de l'ouverture du fichier : " + ex.getMessage());
            }
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(view);
        if(option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                view.txtCode.write(writer);
            } catch(IOException ex) {
                JOptionPane.showMessageDialog(view, "Erreur lors de l'enregistrement : " + ex.getMessage());
            }
        }                        
    }
}*/

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
//action Exécuter 
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

                // --- Compter le nombre d'instructions valides ---
                String[] lignes = code.split("\\R");
                int instructionCount = 0;
                int pc = adresse; // même adresse que chargée

                for (String ligne : lignes) {
                    ligne = ligne.trim();
                    if (ligne.isEmpty() || ligne.startsWith(";") || ligne.endsWith(":") || ligne.toUpperCase().startsWith("ORG"))
                        continue;

                    try {
                        int taille = assembleur.assemblerLigne(ligne, memoire, pc);
                        pc += taille;
                        instructionCount++;
                    } catch (Exception ex) {
                        // Ignorer les erreurs pour le comptage
                    }
                }

                // Mettre à jour le JLabel dans l'interface
                view.lblInstructionCount.setText("Nombre d'instructions : " + instructionCount);

                // Message de succès
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
