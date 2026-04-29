import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.border.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientGui extends Thread {

    private final JTextPane jtextFilDiscu = new JTextPane();
    private final JTextPane jtextListUsers = new JTextPane();
    private final JTextField jtextInputChat = new JTextField();
    private String oldMsg = "";
    private Thread read;
    private String serverName = "localhost";
    private int PORT = 5555;
    private String name = "Name";
    private BufferedReader input;
    private PrintWriter output;
    private Socket server;

    // Design Constants
    private final Color COLOR_BG = new Color(18, 18, 18);
    private final Color COLOR_SIDEBAR = new Color(25, 25, 25);
    private final Color COLOR_CHAT_BG = new Color(15, 15, 15);
    private final Color COLOR_TEXT = new Color(220, 220, 220);
    private final Color COLOR_ACCENT = new Color(0, 173, 181);
    private final Color COLOR_BUTTON_BG = new Color(45, 45, 45);
    private final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private JFrame jfr;
    private JPanel mainPanel;
    private JPanel connectionPanel;
    private JPanel chatPanel;

    public ClientGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        jfr = new JFrame("Chat");
        final ImageIcon icon = new ImageIcon("C:\\programacao\\chat_projeto_final\\imagens\\chat.png");
        jfr.setIconImage(icon.getImage());
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jfr.setSize(850, 600);
        jfr.setLocationRelativeTo(null);
        jfr.getContentPane().setBackground(COLOR_BG);

        setupComponents();
        showConnectionScreen();

        jfr.setVisible(true);
    }

    private void setupComponents() {
        // Chat Display
        jtextFilDiscu.setBackground(COLOR_CHAT_BG);
        jtextFilDiscu.setForeground(COLOR_TEXT);
        jtextFilDiscu.setFont(FONT_MAIN);
        jtextFilDiscu.setEditable(false);
        jtextFilDiscu.setContentType("text/html");
        jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        
        // CSS for chat bubbles and overall look
        HTMLEditorKit kit = (HTMLEditorKit) jtextFilDiscu.getEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: 'Segoe UI', sans-serif; color: #e0e0e0; margin: 10px; background-color: #0f0f0f; }");
        styleSheet.addRule(".bubble { background-color: #252525; padding: 8px; margin: 5px; border-radius: 5px; border: 1px solid #333; }");
        styleSheet.addRule(".user { color: #00adb5; font-weight: bold; font-size: 13px; }");
        styleSheet.addRule(".msg-text { color: #ffffff; font-size: 14px; }");
        styleSheet.addRule("b { color: #00adb5; }");
        styleSheet.addRule("hr { border: 0; border-top: 1px solid #444; margin: 10px 0; }");

        // User List
        jtextListUsers.setBackground(COLOR_CHAT_BG);
        jtextListUsers.setForeground(COLOR_ACCENT);
        jtextListUsers.setFont(FONT_BOLD);
        jtextListUsers.setEditable(false);
        jtextListUsers.setContentType("text/html");
        jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        // Input Field
        jtextInputChat.setBackground(new Color(60, 63, 65));
        jtextInputChat.setForeground(Color.WHITE);
        jtextInputChat.setCaretColor(Color.WHITE);
        jtextInputChat.setFont(FONT_MAIN);
        jtextInputChat.setBorder(new CompoundBorder(new LineBorder(new Color(80, 80, 80)), new EmptyBorder(10, 10, 10, 10)));

        jtextInputChat.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    String current = jtextInputChat.getText().trim();
                    jtextInputChat.setText(oldMsg);
                    oldMsg = current;
                }
            }
        });
    }

    private void showConnectionScreen() {
        connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBackground(COLOR_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("BEM-VINDO AO CHAT", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(COLOR_ACCENT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        connectionPanel.add(title, gbc);

        JTextField jtfName = createStyledField(this.name, "Nome:");
        JTextField jtfAddr = createStyledField(this.serverName, "Servidor:");
        JTextField jtfPort = createStyledField(String.valueOf(this.PORT), "Porta:");

        gbc.gridwidth = 1;
        addLabelAndField(connectionPanel, "Nome do Utilizador:", jtfName, gbc, 1);
        addLabelAndField(connectionPanel, "Endereço IP:", jtfAddr, gbc, 2);
        addLabelAndField(connectionPanel, "Porta:", jtfPort, gbc, 3);

        JButton jbc = createStyledButton("CONECTAR", COLOR_ACCENT);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 10, 10, 10);
        connectionPanel.add(jbc, gbc);

        jbc.addActionListener(ae -> {
            try {
                name = jtfName.getText();
                serverName = jtfAddr.getText();
                PORT = Integer.parseInt(jtfPort.getText());

                server = new Socket(serverName, PORT);
                input = new BufferedReader(new InputStreamReader(server.getInputStream()));
                output = new PrintWriter(server.getOutputStream(), true);
                output.println(name);

                read = new Read();
                read.start();

                showChatScreen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(jfr, "Erro de conexão: " + ex.getMessage());
            }
        });

        jfr.setContentPane(connectionPanel);
        jfr.revalidate();
    }

    private void showChatScreen() {
        chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBackground(COLOR_BG);
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Center Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(jtextFilDiscu));
        splitPane.setRightComponent(new JScrollPane(jtextListUsers));
        splitPane.setDividerLocation(600);
        splitPane.setDividerSize(2);
        splitPane.setBackground(COLOR_BG);
        splitPane.setBorder(null);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 0));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(50, 50, 50)), // Top border separator
            new EmptyBorder(15, 5, 5, 5)
        ));
        bottomPanel.add(jtextInputChat, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setBackground(COLOR_BG);
        
        JButton btnSend = createStyledButton("Enviar", COLOR_ACCENT);
        JButton btnClear = createStyledButton("Limpar", new Color(70, 70, 70));
        JButton btnExit = createStyledButton("Sair", new Color(150, 40, 40));

        buttonPanel.add(btnSend);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnExit);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        chatPanel.add(splitPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        btnSend.addActionListener(e -> sendMessage());
        btnClear.addActionListener(e -> {
            if (showConfirmCustom("Deseja mesmo limpar o chat?", "Limpar Chat")) {
                jtextFilDiscu.setText("");
            }
        });
        btnExit.addActionListener(e -> {
            if (showConfirmCustom("Deseja realmente sair do chat?", "Sair")) {
                System.exit(0);
            }
        });

        jfr.setContentPane(chatPanel);
        jfr.revalidate();
        jtextInputChat.requestFocus();
        
        appendToPane(jtextFilDiscu, "<h3>Conectado ao servidor! ✅</h3><hr>");
    }

    private JTextField createStyledField(String text, String label) {
        JTextField field = new JTextField(text);
        field.setBackground(new Color(60, 63, 65));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(FONT_MAIN);
        field.setBorder(new CompoundBorder(new LineBorder(new Color(80, 80, 80)), new EmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private void addLabelAndField(JPanel p, String text, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.LIGHT_GRAY);
        lbl.setFont(FONT_MAIN);
        p.add(lbl, gbc);
        gbc.gridx = 1;
        p.add(field, gbc);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Paint Background
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Paint Text
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    private boolean showConfirmCustom(String message, String title) {
        JDialog dialog = new JDialog(jfr, title, true);
        dialog.getContentPane().setBackground(COLOR_BG);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JLabel lbl = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(FONT_MAIN);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(COLOR_BG);
        btnPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton btnYes = createStyledButton("Confirmar", COLOR_ACCENT);
        JButton btnNo = createStyledButton("Cancelar", new Color(150, 40, 40));

        final boolean[] result = {false};
        btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });
        btnNo.addActionListener(e -> { result[0] = false; dialog.dispose(); });

        btnPanel.add(btnYes);
        btnPanel.add(btnNo);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(jfr);
        dialog.setVisible(true);
        
        return result[0];
    }

    public void sendMessage() {
        String message = jtextInputChat.getText().trim();
        if (!message.isEmpty()) {
            oldMsg = message;
            output.println(message);
            jtextInputChat.setText("");
            jtextInputChat.requestFocus();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGui());
    }

    class Read extends Thread {
        public void run() {
            String message;
            try {
                while ((message = input.readLine()) != null) {
                    final String msg = message;
                    SwingUtilities.invokeLater(() -> {
                        if (msg.startsWith("[")) {
                            String users = msg.substring(1, msg.length() - 1);
                            jtextListUsers.setText("");
                            for (String u : users.split(",")) {
                                appendToPane(jtextListUsers, "● " + u.trim() + "<br>");
                            }
                        } else {
                            appendToPane(jtextFilDiscu, msg);
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("Conexão perdida.");
            }
        }
    }

    private void appendToPane(JTextPane tp, String msg) {
        HTMLDocument doc = (HTMLDocument) tp.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
        try {
            String formattedMsg = msg;
            // Se não for HTML complexo (como o bem-vindo), envolvemos numa bolha
            if (!msg.contains("<center>") && !msg.contains("<h3>")) {
                formattedMsg = "<div class='bubble'>" + msg + "</div>";
            }
            editorKit.insertHTML(doc, doc.getLength(), formattedMsg, 0, 0, null);
            tp.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
