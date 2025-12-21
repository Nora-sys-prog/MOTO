package MOTO6809;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FenetreRAM extends JFrame {
	private Memoire memoire;
	private JTable table;
	private RAMTableModel model;
	private int pageSize = 256;
	private int currentPage = 0;

	public FenetreRAM(JFrame parent, Memoire memoire) {
		this.memoire = memoire;
		setTitle("RAM (0x0000-0x7FFF)");
		setSize(233, 355);
		setLocationRelativeTo(parent);
		setLocation(435, 120);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		model = new RAMTableModel();
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		table.setRowHeight(20);
		table.setDefaultRenderer(Object.class, new RAMCellRenderer());

		// Ajuster la largeur des colonnes
		table.getColumnModel().getColumn(0).setPreferredWidth(80); // Adresse
		table.getColumnModel().getColumn(1).setPreferredWidth(80); // Valeur
		table.getColumnModel().getColumn(2).setPreferredWidth(60); // Caractère

		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		JButton prevButton = new JButton("Précédent");
		prevButton.addActionListener(e -> {
			if (currentPage > 0) {
				currentPage--;
				model.fireTableDataChanged();
			}
		});
		JButton nextButton = new JButton("Suivant");
		nextButton.addActionListener(e -> {
			if ((currentPage + 1) * pageSize < 0x8000) {
				currentPage++;
				model.fireTableDataChanged();
			}
		});

		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);

		add(buttonPanel, BorderLayout.SOUTH);
		setVisible(true);

		// Double-clic → édition de la valeur
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = table.rowAtPoint(e.getPoint());
					int col = table.columnAtPoint(e.getPoint());
					if (col == 1) { // uniquement la colonne "Valeur"
						editCellAt(row);
					}
				}
			}
		});
	}

	private void editCellAt(int row) {
		int addr = 0x0000 + currentPage * pageSize + row;
		if (addr > 0x8000)
			return;

		int oldValue = memoire.getByte(addr) & 0xFF;

		JPanel panel = new JPanel(new GridLayout(0, 1, 5, 8));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		panel.add(new JLabel("Adresse  " + String.format("%04X", addr)));
		panel.add(new JLabel("Ancienne valeur  $" + String.format("%02X", oldValue)));

		JTextField newValField = new JTextField(String.format("%02X", oldValue), 6);
		newValField.setHorizontalAlignment(JTextField.CENTER);
		newValField.setFont(new Font("Monospaced", Font.BOLD, 16));

		JPanel newValPanel = new JPanel();
		newValPanel.add(new JLabel("Nouvelle valeur  $"));
		newValPanel.add(newValField);
		panel.add(newValPanel);

		int result = JOptionPane.showOptionDialog(this, panel, "Nouvelle valeur RAM", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, new Object[] { "OK", "Annuler" }, "OK");

		if (result == JOptionPane.OK_OPTION) {
			try {
				String text = newValField.getText().trim().toUpperCase();
				text = text.replaceAll("[^0-9A-F]", "");
				int newValue = Integer.parseInt(text, 16);
				if (newValue >= 0 && newValue <= 0xFF) {
					memoire.setByte(addr, (byte) newValue);
					model.fireTableRowsUpdated(row, row); // Rafraîchir toute la ligne
				} else {
					JOptionPane.showMessageDialog(this, "Valeur hors plage (00–FF)", "Erreur",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Format hexadécimal invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void rafraichir() {
		model.fireTableDataChanged();
	}

	private class RAMTableModel extends AbstractTableModel {
		private final String[] columnNames = { "Adresse", "Valeur (Hex)", "Caractère" };

		@Override
		public int getRowCount() {
			return pageSize;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			int addr = currentPage * pageSize + rowIndex;
			if (addr >= 0x8000)
				return null;

			switch (columnIndex) {
			case 0:
				return String.format("%04X", addr);
			case 1:
				return String.format("%02X", memoire.getByte(addr) & 0xFF);
			case 2:
				int value = memoire.getByte(addr) & 0xFF;
				// Afficher TOUS les caractères sans exception (0-255)
				return String.valueOf((char) value);
			default:
				return null;
			}
		}

		// @Override
		// public boolean isCellEditable(int rowIndex, int columnIndex) {
		// return columnIndex == 1;
		// }

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			int addr = currentPage * pageSize + rowIndex;
			if (columnIndex == 1 && addr < 0x8000 && aValue instanceof String) {
				try {
					int value = Integer.parseInt((String) aValue, 16);
					if (value >= 0 && value <= 255) {
						memoire.setByte(addr, (byte) value);
						fireTableRowsUpdated(rowIndex, rowIndex);
					} else {
						JOptionPane.showMessageDialog(FenetreRAM.this, "Valeur hex invalide (0-FF).");
					}
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(FenetreRAM.this, "Format hex invalide (ex. : FF).");
				} catch (UnsupportedOperationException e) {
					JOptionPane.showMessageDialog(FenetreRAM.this, "Erreur inattendue.");
				}
			}
		}
	}

	private class RAMCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			c.setBackground(Color.LIGHT_GRAY);
			if (isSelected)
				c.setBackground(Color.GRAY.darker());

			// Centrer le texte dans la colonne caractère
			if (column == 2) {
				setHorizontalAlignment(SwingConstants.CENTER);
				setFont(new Font("Monospaced", Font.PLAIN, 14));
			} else {
				setHorizontalAlignment(SwingConstants.LEFT);
			}

			return c;
		}
	}
}