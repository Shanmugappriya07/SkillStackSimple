package skillstack.ui;

import skillstack.model.User;
import skillstack.service.AppService;

import java.awt.*;

/** AWT login panel with email and password fields. */
public class LoginPanel extends Panel {

    private final MainFrame  frame;
    private final AppService service;

    private final TextField emailField    = new TextField(25);
    private final TextField passwordField = new TextField(25);
    private final Label     statusLabel   = new Label("", Label.CENTER);

    public LoginPanel(MainFrame frame, AppService service) {
        this.frame = frame; this.service = service;
        setBackground(new Color(240, 244, 248));
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        Label title = new Label("SkillStack", Label.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(37, 99, 235));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; add(title, g);

        Label sub = new Label("Sign in to your account", Label.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(100, 116, 139));
        g.gridy = 1; add(sub, g);

        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 2; add(new Label("Email:"), g);
        g.gridx = 1; add(emailField, g);

        passwordField.setEchoChar('*');
        g.gridx = 0; g.gridy = 3; add(new Label("Password:"), g);
        g.gridx = 1; add(passwordField, g);

        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; add(statusLabel, g);

        Button loginBtn = new Button("Login");
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginBtn.setBackground(new Color(37, 99, 235));
        loginBtn.setForeground(Color.WHITE);
        g.gridy = 5; add(loginBtn, g);

        Button regBtn = new Button("Don't have an account? Register");
        regBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        regBtn.setBackground(new Color(240, 244, 248));
        g.gridy = 6; add(regBtn, g);

        loginBtn.addActionListener(e -> doLogin());
        regBtn.addActionListener(e -> { statusLabel.setText(""); frame.showCard(MainFrame.CARD_REGISTER); });
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass  = passwordField.getText();
        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please enter email and password."); return;
        }
        User u = service.login(email, pass);
        if (u == null) { statusLabel.setText("Invalid email or password."); return; }
        statusLabel.setText("");
        passwordField.setText("");
        frame.setCurrentUser(u);
        frame.showCard(MainFrame.CARD_DASHBOARD);
    }
}
