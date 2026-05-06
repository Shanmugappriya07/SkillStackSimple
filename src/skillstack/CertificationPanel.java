package skillstack;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** AWT panel for adding, viewing, and deleting certifications stored in a HashSet. */
public class CertificationPanel extends Panel {

    private final MainFrame  frame;
    private final AppService service;

    private final TextField titleField      = new TextField(20);
    private final TextField issuerField     = new TextField(20);
    private final TextField issueDateField  = new TextField(15);
    private final TextField expiryField     = new TextField(15);
    private final TextField credIdField     = new TextField(20);
    private final TextArea  listArea        = new TextArea("", 8, 60, TextArea.SCROLLBARS_VERTICAL_ONLY);
    private final Label     statusLabel     = new Label("", Label.CENTER);

    private final List<Certification> displayed = new ArrayList<>();

    public CertificationPanel(MainFrame frame, AppService service) {
        this.frame = frame; this.service = service;
        setBackground(new Color(240, 244, 248));
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        // Top bar
        Panel top = new Panel(new BorderLayout());
        top.setBackground(new Color(22, 163, 74));
        Label title = new Label("  Manage Certifications", Label.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        top.add(title, BorderLayout.CENTER);
        Button back = new Button("← Dashboard");
        back.setBackground(new Color(15, 118, 56)); back.setForeground(Color.WHITE);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Form
        Panel form = new Panel(new GridBagLayout());
        form.setBackground(new Color(240, 244, 248));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10); g.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, g, "Cert Title:",               titleField,     0);
        addRow(form, g, "Issuer:",                   issuerField,    1);
        addRow(form, g, "Issue Date (YYYY-MM-DD):",  issueDateField, 2);
        addRow(form, g, "Expiry Date (YYYY-MM-DD):", expiryField,    3);
        addRow(form, g, "Credential ID:",            credIdField,    4);

        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2; form.add(statusLabel, g);

        Panel btns = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        btns.setBackground(new Color(240, 244, 248));
        Button addBtn = new Button("Add Certification");
        addBtn.setBackground(new Color(22, 163, 74)); addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        Button delBtn = new Button("Delete Selected (by line#)");
        delBtn.setBackground(new Color(220, 38, 38)); delBtn.setForeground(Color.WHITE);
        btns.add(addBtn); btns.add(delBtn);
        g.gridy = 6; form.add(btns, g);

        Label lbl = new Label("Your Certifications (HashSet — deduplicates by credential ID):");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.gridy = 7; form.add(lbl, g);

        listArea.setEditable(false);
        listArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.gridy = 8; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        form.add(listArea, g);

        add(form, BorderLayout.CENTER);

        back.addActionListener(e -> frame.showCard(MainFrame.CARD_DASHBOARD));
        addBtn.addActionListener(e -> addCert());
        delBtn.addActionListener(e -> deleteSelected());
    }

    private void addRow(Panel p, GridBagConstraints g, String lbl, TextField field, int row) {
        g.gridwidth = 1; g.weightx = 0; g.gridx = 0; g.gridy = row; p.add(new Label(lbl), g);
        g.gridx = 1; g.weightx = 1; p.add(field, g);
    }

    /** Reloads certifications from DB into the HashSet and refreshes the list display. */
    public void refresh() {
        if (frame.getCurrentUser() == null) return;
        HashSet<Certification> set = service.getCerts(frame.getCurrentUser().getId());
        displayed.clear(); displayed.addAll(set);
        render(); statusLabel.setText("");
    }

    private void render() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < displayed.size(); i++) {
            Certification c = displayed.get(i);
            sb.append(String.format("[%d] %-30s | %-20s | %s%n",
                      i + 1, c.getTitle(), c.getIssuer(), c.getIssueDate()));
            sb.append(String.format("     Expiry: %-15s | ID: %s%n",
                      c.getExpiryDate().isEmpty() ? "None" : c.getExpiryDate(), c.getCredentialId()));
        }
        if (displayed.isEmpty()) sb.append("No certifications added yet.");
        listArea.setText(sb.toString());
    }

    private void addCert() {
        String title = titleField.getText().trim();
        String issuer = issuerField.getText().trim();
        if (title.isEmpty())  { err("Certification title is required."); return; }
        if (issuer.isEmpty()) { err("Issuer is required."); return; }

        Certification saved = service.addCert(
            frame.getCurrentUser().getId(), title, issuer,
            issueDateField.getText().trim(), expiryField.getText().trim(),
            credIdField.getText().trim()
        );
        if (saved == null) { err("Failed to save certification."); return; }
        titleField.setText(""); issuerField.setText("");
        issueDateField.setText(""); expiryField.setText(""); credIdField.setText("");
        ok("Certification '" + saved.getTitle() + "' added.");
        refresh();
    }

    private void deleteSelected() {
        String sel = listArea.getSelectedText();
        if (sel == null || sel.trim().isEmpty()) { err("Select a line then click Delete."); return; }
        try {
            int s = sel.indexOf('['), e = sel.indexOf(']');
            if (s < 0 || e < 0) { err("Select the full line including [N]."); return; }
            int idx = Integer.parseInt(sel.substring(s + 1, e)) - 1;
            if (idx < 0 || idx >= displayed.size()) { err("Invalid line number."); return; }
            Certification c = displayed.get(idx);
            if (service.deleteCert(c.getId())) { ok("Certification '" + c.getTitle() + "' deleted."); refresh(); }
            else err("Failed to delete certification.");
        } catch (NumberFormatException ex) { err("Select the full line including [N]."); }
    }

    private void ok(String m)  { statusLabel.setForeground(new Color(22,163,74)); statusLabel.setText(m); }
    private void err(String m) { statusLabel.setForeground(Color.RED);            statusLabel.setText(m); }
}
