package MOTO6809;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FenetreRegistre extends JFrame {
	private Registres registres;
	private CPU6809 cpu;
	private JTextField pcField, sField, uField, aField, bField, dpField, xField, yField;
	private JLabel adressageLabel;
	private JLabel[] flagValueLabels;
	private JTextField instructionField;
	private JLabel cyclesLabel;

	public FenetreRegistre(JFrame parent, Registres regs, CPU6809 cpu) {
		this.registres = regs;
		this.cpu = cpu;
		setTitle("Architecture interne du 6809");
		setSize(428, 600);
		setLocationRelativeTo(parent);
		setLocation(0, 120);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		setUndecorated(true);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(Color.LIGHT_GRAY);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// ======== TITRE ========
		JLabel titleLabel = new JLabel("Architecture interne du 6809");
		titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(titleLabel);
		mainPanel.add(Box.createVerticalStrut(15));

		// ======== PANNEAU INSTRUCTION + CYCLES ========
		/*
		 * JPanel instructionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		 * instructionPanel.setBackground(Color.LIGHT_GRAY);
		 * instructionPanel.setMaximumSize(new Dimension(450, 40));
		 * 
		 * JLabel instructionTitleLabel = new JLabel("Instruction:");
		 * instructionTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		 * instructionPanel.add(instructionTitleLabel);
		 * 
		 * adressageLabel = new JLabel("ADDA $FC00"); adressageLabel.setFont(new
		 * Font("Arial", Font.BOLD, 16)); adressageLabel.setForeground(Color.BLUE);
		 * instructionPanel.add(adressageLabel);
		 */
		// ======== PANNEAU INSTRUCTION + CYCLES ========
		JPanel instructionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		instructionPanel.setBackground(Color.LIGHT_GRAY);
		instructionPanel.setMaximumSize(new Dimension(450, 40));

		JLabel instructionTitleLabel = new JLabel("Instruction:");
		instructionTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		instructionPanel.add(instructionTitleLabel);

		// Champ de texte pour l'instruction (comme A, B, etc.)
		instructionField = new JTextField(12); // 12 caractères de large
		instructionField.setFont(new Font("Monospaced", Font.BOLD, 16));
		instructionField.setEditable(false);
		instructionField.setBackground(Color.WHITE);
		instructionField.setForeground(Color.BLUE);
		instructionPanel.add(instructionField);

		JLabel cyclesTitleLabel = new JLabel("Cycles:");
		cyclesTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
		instructionPanel.add(cyclesTitleLabel);

		cyclesLabel = new JLabel("0");
		cyclesLabel.setFont(new Font("Arial", Font.BOLD, 16));
		cyclesLabel.setForeground(Color.RED);
		instructionPanel.add(cyclesLabel);

		mainPanel.add(instructionPanel);
		mainPanel.add(Box.createVerticalStrut(10));

		// ======== PANNEAU PC ========
		JPanel pcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		pcPanel.setBackground(Color.LIGHT_GRAY);
		JLabel pcLabel = new JLabel("PC");
		pcLabel.setFont(new Font("Arial", Font.BOLD, 24));
		pcField = new JTextField(6);
		pcField.setFont(new Font("Monospaced", Font.BOLD, 20));
		pcField.setEditable(false);
		pcField.setBackground(Color.WHITE);
		pcField.setForeground(Color.BLUE);
		pcPanel.add(pcLabel);
		pcPanel.add(pcField);
		mainPanel.add(pcPanel);

		// ======== PANNEAU S et U ========
		JPanel suPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 5));
		suPanel.setBackground(Color.LIGHT_GRAY);
		JLabel sLabel = new JLabel("S");
		sLabel.setFont(new Font("Arial", Font.BOLD, 20));
		sField = new JTextField(6);
		sField.setFont(new Font("Monospaced", Font.BOLD, 18));
		sField.setEditable(false);
		sField.setBackground(Color.WHITE);
		sField.setForeground(Color.BLUE);
		JLabel uLabel = new JLabel("U");
		uLabel.setFont(new Font("Arial", Font.BOLD, 20));
		uField = new JTextField(6);
		uField.setFont(new Font("Monospaced", Font.BOLD, 18));
		uField.setEditable(false);
		uField.setBackground(Color.WHITE);
		uField.setForeground(Color.BLUE);
		suPanel.add(sLabel);
		suPanel.add(sField);
		suPanel.add(uLabel);
		suPanel.add(uField);
		mainPanel.add(suPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// ======== PANNEAU UAL ========
		JPanel ualPanel = new JPanel();
		ualPanel.setLayout(new BorderLayout());
		ualPanel.setBackground(Color.LIGHT_GRAY);
		ualPanel.setPreferredSize(new Dimension(550, 180));
		ualPanel.setMaximumSize(new Dimension(450, 180));

		// Panneau gauche avec A et B
		JPanel leftAB = new JPanel();
		leftAB.setLayout(new BoxLayout(leftAB, BoxLayout.Y_AXIS));
		leftAB.setBackground(Color.LIGHT_GRAY);
		leftAB.add(Box.createVerticalStrut(20));

		JPanel aPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		aPanel.setBackground(Color.LIGHT_GRAY);
		JLabel aLabel = new JLabel("A");
		aLabel.setFont(new Font("Arial", Font.BOLD, 24));
		aField = new JTextField(4);
		aField.setFont(new Font("Monospaced", Font.BOLD, 20));
		aField.setEditable(false);
		aField.setBackground(Color.WHITE);
		aField.setForeground(Color.BLUE);
		aPanel.add(aLabel);
		aPanel.add(aField);
		leftAB.add(aPanel);
		leftAB.add(Box.createVerticalStrut(5));

		JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		bPanel.setBackground(Color.LIGHT_GRAY);
		JLabel bLabel = new JLabel("B");
		bLabel.setFont(new Font("Arial", Font.BOLD, 24));
		bField = new JTextField(4);
		bField.setFont(new Font("Monospaced", Font.BOLD, 20));
		bField.setEditable(false);
		bField.setBackground(Color.WHITE);
		bField.setForeground(Color.BLUE);
		bPanel.add(bLabel);
		bPanel.add(bField);
		leftAB.add(bPanel);

		// Panneau central avec schéma UAL
		JPanel centerUAL = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setStroke(new BasicStroke(3));
				g2.setColor(Color.BLACK);

				// Trapèze UAL élargi
				int[] xHexagon = { 30, 150, 150, 30, 30, 76, 30 };
				int[] yHexagon = { 30, 50, 120, 140, 94, 85, 76 };
				g2.drawPolygon(xHexagon, yHexagon, 7);

				// Flèches d'entrée
				g2.drawLine(0, 50, 30, 50);
				g2.drawLine(0, 120, 30, 120);

				// Flèche de sortie
				g2.drawLine(90, 130, 90, 186);

				// Texte "UAL"
				g2.setColor(Color.BLACK);
				g2.setFont(new Font("Arial", Font.BOLD, 20));
				g2.drawString("UAL", 92, 92);
			}
		};
		centerUAL.setBackground(Color.LIGHT_GRAY);
		centerUAL.setPreferredSize(new Dimension(170, 180));

		ualPanel.add(leftAB, BorderLayout.WEST);
		ualPanel.add(centerUAL, BorderLayout.CENTER);
		mainPanel.add(ualPanel);

		// ======== PANNEAU FLAGS (ENCADRÉS) ========
		JPanel flagsPanel = new JPanel();
		flagsPanel.setLayout(new GridLayout(2, 8, 15, 2));
		// flagsPanel.setBackground(Color.LIGHT_GRAY);
		flagsPanel.setMaximumSize(new Dimension(450, 70));
		flagsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2),
				"Flags CC", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
				new Font("Arial", Font.BOLD, 12)));

		// Ligne 1 : Les valeurs 0 ou 1
		flagValueLabels = new JLabel[8];
		for (int i = 0; i < 8; i++) {
			flagValueLabels[i] = new JLabel("0", SwingConstants.CENTER);
			flagValueLabels[i].setFont(new Font("Courier New", Font.BOLD, 16));
			flagValueLabels[i].setForeground(Color.BLUE);
			flagsPanel.add(flagValueLabels[i]);
		}

		// Ligne 2 : Les noms des flags
		String[] flags = { "E", "F", "H", "I", "N", "Z", "V", "C" };
		for (String flag : flags) {
			JLabel flagLabel = new JLabel(flag, SwingConstants.CENTER);
			flagLabel.setFont(new Font("Arial", Font.BOLD, 14));
			flagLabel.setForeground(Color.BLACK);
			flagsPanel.add(flagLabel);
		}

		// Conteneur avec flèche sous les flags
		JPanel flagsWithArrow = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(3));
				g2.setColor(Color.BLACK);

				// Flèche verticale au centre
				int centerX = getWidth() / 2;
				g2.drawLine(centerX, 75, centerX, 95);
				g2.drawLine(centerX, 95, centerX - 5, 88);
				g2.drawLine(centerX, 95, centerX + 5, 88);
			}
		};
		flagsWithArrow.setLayout(new BorderLayout());
		flagsWithArrow.setBackground(Color.LIGHT_GRAY);
		flagsWithArrow.setMaximumSize(new Dimension(450, 100));
		flagsWithArrow.add(flagsPanel, BorderLayout.NORTH);

		mainPanel.add(flagsWithArrow);
		mainPanel.add(Box.createVerticalStrut(5));

		// ======== PANNEAU DP (APRÈS FLAGS) ========
		JPanel dpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 5));
		dpPanel.setBackground(Color.LIGHT_GRAY);
		dpPanel.setMaximumSize(new Dimension(450, 40));

		JLabel dpLabel = new JLabel("DP");
		dpLabel.setFont(new Font("Arial", Font.BOLD, 20));
		dpField = new JTextField(3);
		dpField.setFont(new Font("Monospaced", Font.BOLD, 18));
		dpField.setEditable(false);
		dpField.setBackground(Color.WHITE);
		dpField.setForeground(Color.BLUE);

		dpPanel.add(dpLabel);
		dpPanel.add(dpField);
		mainPanel.add(dpPanel);
		mainPanel.add(Box.createVerticalStrut(10));

		// ======== PANNEAU X et Y ========
		JPanel xyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 5));
		xyPanel.setBackground(Color.LIGHT_GRAY);
		JLabel xLabel = new JLabel("X");
		xLabel.setFont(new Font("Arial", Font.BOLD, 20));
		xField = new JTextField(6);
		xField.setFont(new Font("Monospaced", Font.BOLD, 18));
		xField.setEditable(false);
		xField.setBackground(Color.WHITE);
		xField.setForeground(Color.BLUE);
		JLabel yLabel = new JLabel("Y");
		yLabel.setFont(new Font("Arial", Font.BOLD, 20));
		yField = new JTextField(6);
		yField.setFont(new Font("Monospaced", Font.BOLD, 18));
		yField.setEditable(false);
		yField.setBackground(Color.WHITE);
		yField.setForeground(Color.BLUE);
		xyPanel.add(xLabel);
		xyPanel.add(xField);
		xyPanel.add(yLabel);
		xyPanel.add(yField);
		mainPanel.add(xyPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// ======== BOUTONS ========
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(Color.LIGHT_GRAY);

		JButton stepButton = new JButton("Step");
		stepButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cpu.executeOneInstruction();
				updateDisplay();
			}
		});

		JButton run100Button = new JButton("Run 100");
		run100Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 100; i++) {
					cpu.executeOneInstruction();
				}
				updateDisplay();
			}
		});

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cpu.reset();
				updateDisplay();
			}
		});

		buttonPanel.add(stepButton);
		buttonPanel.add(run100Button);
		buttonPanel.add(resetButton);
		mainPanel.add(buttonPanel);

		add(mainPanel);
		updateDisplay();
		setVisible(true);
	}

	public void updateDisplay() {
		// Mise à jour de l'instruction
		// adressageLabel.setText(cpu.getCurrentInstruction());
		instructionField.setText(cpu.getCurrentInstruction());
		// Mise à jour des cycles
		cyclesLabel.setText(String.valueOf(cpu.getCycleCount()));

		// Mise à jour des registres
		pcField.setText(String.format("%04X", registres.getPC()));
		sField.setText(String.format("%04X", registres.getS()));
		uField.setText(String.format("%04X", registres.getU()));
		aField.setText(String.format("%02X", registres.getA()));
		bField.setText(String.format("%02X", registres.getB()));
		dpField.setText(String.format("%02X", registres.getDP()));
		xField.setText(String.format("%04X", registres.getX()));
		yField.setText(String.format("%04X", registres.getY()));

		// Mise à jour des flags
		int[] masks = { Registres.CC_E, Registres.CC_F, Registres.CC_H, Registres.CC_I, Registres.CC_N, Registres.CC_Z,
				Registres.CC_V, Registres.CC_C };
		for (int i = 0; i < 8; i++) {
			boolean set = registres.getFlag(masks[i]);
			flagValueLabels[i].setText(set ? "1" : "0");
			flagValueLabels[i].setForeground(set ? Color.RED : Color.BLUE);
		}
	}
}