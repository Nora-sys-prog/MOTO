package MOTO6809;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class FenetreOperations extends JFrame {
	public FenetreOperations() {
		super("Opérations et Rôles");

		setSize(650, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Barre de recherche
		JTextField searchField = new JTextField();
		searchField.setPreferredSize(new Dimension(200, 30));

		JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(new JLabel(" 🔎 Rechercher (Opérations) : "), BorderLayout.WEST);
		searchPanel.add(searchField, BorderLayout.CENTER);

		// Création des onglets
		JTabbedPane tabs = new JTabbedPane();

		tabs.addTab("Arithmétique",
				createSearchableTable(searchField,
						new String[][] { { "ABX", "Additionne B à X" }, { "ADC", "Addition avec retenue" },
								{ "ADD", "Addition simple" }, { "DAA", "Ajustement décimal" }, { "DEC", "Décrémente" },
								{ "INC", "Incrémente" }, { "NEG", "Négation (complément)" } }));

		tabs.addTab("Logique",
				createSearchableTable(searchField, new String[][] { { "AND", "ET logique" }, { "BIT", "Test de bits" },
						{ "COM", "Complément binaire" }, { "EOR", "OU exclusif" }, { "OR", "OU logique" } }));

		tabs.addTab("Décalages",
				createSearchableTable(searchField,
						new String[][] { { "ASL", "Shift logique gauche" }, { "ASR", "Shift arithmétique droite" },
								{ "LSL", "Shift logique gauche" }, { "LSR", "Shift logique droite" } }));

		tabs.addTab("Chargement", createSearchableTable(searchField, new String[][] { { "LD", "Charge une valeur" },
				{ "LEA", "Charge une adresse effective" }, { "EXG", "Échange de registres" } }));

		tabs.addTab("Sauts", createSearchableTable(searchField, new String[][] { { "JMP", "Saut inconditionnel" },
				{ "JSR", "Sous-routine" }, { "NOP", "Aucune opération" } }));

		tabs.addTab("Pile", createSearchableTable(searchField,
				new String[][] { { "PSH", "Empile des registres" }, { "PUL", "Dépile des registres" } }));

		tabs.addTab("Spécial",
				createSearchableTable(searchField, new String[][] { { "CWAI", "Attente interruption" } }));

		// Layout principal
		setLayout(new BorderLayout());
		add(searchPanel, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
	}

	// Fonction qui crée une table filtrable par un JTextField
	private JScrollPane createSearchableTable(JTextField searchField, String[][] data) {

		String[] columns = { "Opération", "Rôle" };

		DefaultTableModel model = new DefaultTableModel(data, columns) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JTable table = new JTable(model);
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);

		// Filtrage uniquement sur la colonne 0 "Opération"
		searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) {
				filter();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e) {
				filter();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e) {
				filter();
			}

			private void filter() {
				String text = searchField.getText().trim();
				if (text.length() == 0) {
					sorter.setRowFilter(null);
				} else {
					sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
				}
			}
		});

		return new JScrollPane(table);

	}
}
