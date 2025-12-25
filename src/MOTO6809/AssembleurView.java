package MOTO6809;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.*;

public class AssembleurView extends JFrame {

    public JTextPane txtCode;
    public JButton btnRun;
    public JButton btnOpen;
    public JButton btnSave;
    public JButton btnClear;

    // Liste des instructions 6809
    private final String[] instructions = {
        "LDA","LDX","LDB","LDY","STA","STX","STY","ADDA","ADDB","ADD","SUBA","SUBB","CMPA","CMPB",
        "JMP","JSR","RTS","NOP","INC","DEC","AND","OR","EOR","BIT","ASL","ASR","LSL","LSR",
        "LEA","EXG","PSH","PUL","CWAI","NEG","DAA"
    };

    public AssembleurView() {

        setTitle("Éditeur Assembleur");
        setSize(546, 473);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        setLayout(null);
        setLocation(956, 120);
        ((JComponent) getContentPane()).setBorder(new LineBorder(Color.WHITE, 3));

        // Zone de texte
        txtCode = new JTextPane();
        txtCode.setBackground(new Color(25, 25, 25));
        txtCode.setForeground(Color.WHITE);
        txtCode.setCaretColor(Color.WHITE);
        txtCode.setFont(new Font("Consolas", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(txtCode);
        scroll.setBounds(20, 20, 506, 350);
        scroll.setBorder(new LineBorder(Color.WHITE, 1));

        // Barre de numéros
        LineNumberBar lineNumbers = new LineNumberBar(txtCode, 30);
        scroll.setRowHeaderView(lineNumbers);

        add(scroll);

        // Boutons
        btnRun   = createDarkButton("Exécuter");
        btnOpen  = createDarkButton("Ouvrir");
        btnSave  = createDarkButton("Enregistrer");
        btnClear = createDarkButton("Effacer");

        btnRun.setBounds(20, 390, 120, 30);
        btnOpen.setBounds(160, 390, 120, 30);
        btnSave.setBounds(300, 390, 120, 30);
        btnClear.setBounds(440, 390, 120, 30);

        add(btnRun);
        add(btnOpen);
        add(btnSave);
        add(btnClear);

        // Listener pour coloration syntaxique
        txtCode.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applySyntaxHighlighting(); }
            public void removeUpdate(DocumentEvent e) { applySyntaxHighlighting(); }
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    private JButton createDarkButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(Color.WHITE, 1));
        return btn;
    }

    // ===============================
    // Barre de numéros
    // ===============================
    class LineNumberBar extends JPanel {
        private JTextPane textArea;
        private int barWidth;

        public LineNumberBar(JTextPane ta, int width) {
            this.textArea = ta;
            this.barWidth = width;
            setPreferredSize(new Dimension(barWidth, 0));
            setBackground(new Color(40, 40, 40));
            setFont(new Font("Consolas", Font.PLAIN, 14));

            textArea.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { repaint(); }
                public void removeUpdate(DocumentEvent e) { repaint(); }
                public void changedUpdate(DocumentEvent e) { repaint(); }
            });

            textArea.addCaretListener(e -> repaint());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.RED);
            FontMetrics fm = g.getFontMetrics();

            int startOffset = textArea.viewToModel2D(new Point(0, 0));
            int endOffset = textArea.viewToModel2D(new Point(0, textArea.getHeight()));

            try {
                int startLine = textArea.getDocument().getDefaultRootElement().getElementIndex(startOffset);
                int endLine = textArea.getDocument().getDefaultRootElement().getElementIndex(endOffset);

                for (int i = startLine; i <= endLine; i++) {
                    int y = textArea.modelToView2D(
                        textArea.getDocument().getDefaultRootElement().getElement(i).getStartOffset()
                    ).getBounds().y + fm.getAscent();
                    g.drawString(String.valueOf(i + 1), 5, y);
                }
            } catch (Exception e) {}
        }
    }

    // ===============================
    // Coloration syntaxique
    // ===============================
    private void applySyntaxHighlighting() {
        SwingUtilities.invokeLater(() -> {

            StyledDocument doc = txtCode.getStyledDocument();

            Style defaultStyle = txtCode.addStyle("default", null);
            StyleConstants.setForeground(defaultStyle, Color.WHITE);

            Style instrStyle = txtCode.addStyle("instr", null);
            StyleConstants.setForeground(instrStyle, Color.WHITE);
            StyleConstants.setBold(instrStyle, true);

            Style valueStyle = txtCode.addStyle("value", null);
            StyleConstants.setForeground(valueStyle, new Color(135, 206, 250)); // bleu ciel

            // Réinitialisation
            doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
            String text = txtCode.getText();
            Matcher matcher;

            // ==== Instructions ====
            for (String instr : instructions) {
                Pattern pattern = Pattern.compile("\\b" + instr + "\\b", Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(text);
                while (matcher.find()) {
                    doc.setCharacterAttributes(
                        matcher.start(),
                        matcher.end() - matcher.start(),
                        instrStyle,
                        false
                    );
                }
            }

            // ==== Opérandes en bleu ciel ====

            // #$FF00FF ou #$123456
            Pattern immediateHexPattern = Pattern.compile("#\\$[0-9A-Fa-f]+");
            matcher = immediateHexPattern.matcher(text);
            while (matcher.find()) {
                doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), valueStyle, false);
            }

            // #255 ou #45
            Pattern immediateDecPattern = Pattern.compile("#\\d+");
            matcher = immediateDecPattern.matcher(text);
            while (matcher.find()) {
                doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), valueStyle, false);
            }

            // $2000 ou $FF
            Pattern hexPattern = Pattern.compile("\\$[0-9A-Fa-f]+");
            matcher = hexPattern.matcher(text);
            while (matcher.find()) {
                doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), valueStyle, false);
            }

            // 10 ou 1234
            Pattern decimalPattern = Pattern.compile("\\b\\d+\\b");
            matcher = decimalPattern.matcher(text);
            while (matcher.find()) {
                doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), valueStyle, false);
            }

        });
    }
}