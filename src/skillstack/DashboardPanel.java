package skillstack;

import java.awt.*;
import java.io.IOException;
import java.util.HashSet;

/** AWT dashboard showing skill/cert counts and export buttons; calls refresh() on every visit. */
public class DashboardPanel extends Panel {

    private final MainFrame  frame;
    private final AppService service;

    private final Label welcomeLabel    = new Label("Welcome!", Label.CENTER);
    private final Label careerLabel     = new Label("", Label.CENTER);
    private final Label skillCountLabel = new Label("Skills: 0", Label.CENTER);
    private final Label certCountLabel  = new Label("Certifications: 0", Label.CENTER);
    private final Label statusLabel     = new Label("", Label.CENTER);

    public DashboardPanel(MainFrame frame, AppService service) {
        this.frame = frame; this.service = service;
        setBackground(new Color(240, 244, 248));
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 16, 10, 16);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;

        Label appTitle = new Label("SkillStack Dashboard", Label.CENTER);
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        appTitle.setForeground(new Color(37, 99, 235));
        g.gridy = 0; g.gridwidth = 3; add(appTitle, g);

        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcomeLabel.setForeground(new Color(30, 41, 59));
        g.gridy = 1; add(welcomeLabel, g);

        careerLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        careerLabel.setForeground(new Color(100, 116, 139));
        g.gridy = 2; add(careerLabel, g);

        g.gridy = 3;
        add(new Label("─────────────────────────────────────────────", Label.CENTER), g);

        g.gridwidth = 1; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.CENTER;
        skillCountLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        skillCountLabel.setForeground(new Color(37, 99, 235));
        g.gridy = 4; g.gridx = 0; add(skillCountLabel, g);

        certCountLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        certCountLabel.setForeground(new Color(22, 163, 74));
        g.gridx = 2; add(certCountLabel, g);

        g.fill = GridBagConstraints.HORIZONTAL; g.gridwidth = 3; g.gridx = 0;

        addBtn(g, 5, "Manage Skills",                new Color(37,99,235),   e -> frame.showCard(MainFrame.CARD_SKILLS));
        addBtn(g, 6, "Manage Certifications",        new Color(22,163,74),   e -> frame.showCard(MainFrame.CARD_CERTS));
        addBtn(g, 7, "Export Resume (.txt)",          new Color(234,88,12),   e -> exportResume());
        addBtn(g, 8, "Save Profile Snapshot (.ser)", new Color(109,40,217),  e -> saveSnapshot());
        addBtn(g, 9, "Logout",                        new Color(200,200,200), e -> frame.logout());

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.gridy = 10; add(statusLabel, g);
    }

    private void addBtn(GridBagConstraints g, int row, String label, Color bg,
                        java.awt.event.ActionListener listener) {
        Button btn = new Button(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        g.gridy = row; add(btn, g);
        btn.addActionListener(listener);
    }

    /** Refreshes counts from DB; called every time this panel becomes visible. */
    public void refresh() {
        User u = frame.getCurrentUser();
        if (u == null) return;
        welcomeLabel.setText("Welcome back, " + u.getName() + "!");
        String goal = u.getCareerGoal();
        careerLabel.setText(goal != null && !goal.isEmpty() ? "Goal: " + goal : "");

        HashSet<Skill>         sk = service.getSkills(u.getId());
        HashSet<Certification> ce = service.getCerts(u.getId());
        u.setSkills(sk); u.setCerts(ce);

        skillCountLabel.setText("Skills: " + sk.size());
        certCountLabel.setText("Certifications: " + ce.size());
        statusLabel.setText("");
    }

    private void exportResume() {
        User u = frame.getCurrentUser();
        if (u == null) return;
        try {
            String path = service.generateResume(u, service.getSkills(u.getId()), service.getCerts(u.getId()));
            ok("Resume saved: " + path);
        } catch (IOException e) { err("Error saving resume: " + e.getMessage()); }
    }

    private void saveSnapshot() {
        User u = frame.getCurrentUser();
        if (u == null) return;
        u.setSkills(service.getSkills(u.getId()));
        u.setCerts(service.getCerts(u.getId()));
        try {
            String path = service.saveSnapshot(u);
            ok("Snapshot saved: " + path);
        } catch (IOException e) { err("Error saving snapshot: " + e.getMessage()); }
    }

    private void ok(String msg)  { statusLabel.setForeground(new Color(22,163,74)); statusLabel.setText(msg); }
    private void err(String msg) { statusLabel.setForeground(Color.RED);            statusLabel.setText(msg); }
}
