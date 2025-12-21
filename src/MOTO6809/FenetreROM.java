package MOTO6809;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class FenetreROM extends JFrame {
	private Memoire memoire;
	private JTable table;
	private ROMTableModel model;
	private int pageSize = 256;
	private int currentPage = 0;

	public FenetreROM(JFrame parent, Memoire memoire) {
		this.memoire = memoire;
		setTitle("ROM(0x8000-0xFFFF)");
		setSize(233, 355);
		setLocationRelativeTo(parent);
		setLocation(666, 200);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		model = new ROMTableModel();
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		table.setRowHeight(20);
		table.setDefaultRenderer(Object.class, new ROMCellRenderer());

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
	}

	public void rafraichir() {
		model.fireTableDataChanged();
	}

	private class ROMTableModel extends AbstractTableModel {
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
			int addr = 0x8000 + currentPage * pageSize + rowIndex;
			if (addr > 0xFFFF)
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

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	private class ROMCellRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			c.setBackground(Color.GRAY);
			if (isSelected)
				c.setBackground(Color.LIGHT_GRAY.darker());

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