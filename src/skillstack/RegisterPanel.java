package skillstack;

import java.awt.*;

/** AWT registration panel with name, email, password, and career goal fields. */
public class RegisterPanel extends Panel {

    private final MainFrame  frame;
    private final AppService service;

    private final TextField nameField       = new TextField(25);
    private final TextField emailField      = new TextField(25);
    private final TextField passwordField   = new TextField(25);
    private final TextField careerField     = new TextField(25);
    private final Label     statusLabel     = new Label("", Label.CENTER);

    public RegisterPanel(MainFrame frame, AppService service) {
        this.frame = frame; this.service = service;
        setBackground(new Color(240, 244, 248));
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        Label title = new Label("Create Account", Label.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(37, 99, 235));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; add(title, g);

        Label sub = new Label("Join SkillStack and track your growth", Label.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));
        g.gridy = 1; add(sub, g);

        g.gridwidth = 1;
        addRow(g, "Full Name:", nameField, 2);
        addRow(g, "Email:", emailField, 3);
        passwordField.setEchoChar('*');
        addRow(g, "Password (min 4):", passwordField, 4);
        addRow(g, "Career Goal:", careerField, 5);

        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; add(statusLabel, g);

        Button regBtn = new Button("Create Account");
        regBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        regBtn.setBackground(new Color(22, 163, 74));
        regBtn.setForeground(Color.WHITE);
        g.gridy = 7; add(regBtn, g);

        Button backBtn = new Button("Already have an account? Login");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        backBtn.setBackground(new Color(240, 244, 248));
        g.gridy = 8; add(backBtn, g);

        regBtn.addActionListener(e -> doRegister());
        backBtn.addActionListener(e -> { statusLabel.setText(""); frame.showCard(MainFrame.CARD_LOGIN); });
    }

    private void addRow(GridBagConstraints g, String lbl, TextField field, int row) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; add(new Label(lbl), g);
        g.gridx = 1; add(field, g);
    }

    private void doRegister() {
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass  = passwordField.getText();
        String goal  = careerField.getText().trim();

        if (name.isEmpty())                       { err("Full name is required."); return; }
        if (email.isEmpty() || !email.contains("@")){ err("Enter a valid email address."); return; }
        if (pass.length() < 4)                    { err("Password must be at least 4 characters."); return; }

        User u = service.register(name, email, pass, goal);
        if (u == null) { err("Registration failed — email may already be in use."); return; }

        statusLabel.setForeground(new Color(22, 163, 74));
        statusLabel.setText("Account created! Logging you in...");
        passwordField.setText("");
        frame.setCurrentUser(u);
        frame.showCard(MainFrame.CARD_DASHBOARD);
    }

    private void err(String msg) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(msg);
    }
}
