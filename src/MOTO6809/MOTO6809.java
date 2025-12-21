package MOTO6809;

import javax.swing.*;

public class MOTO6809 {
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Simu6809");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
				FenetrePrincipale fenetre = new FenetrePrincipale();
           }
        });
    }
}