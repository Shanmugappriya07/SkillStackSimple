package skillstack.ui;

import skillstack.model.Skill;
import skillstack.service.AppService;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** AWT panel for adding, viewing, and deleting skills stored in a HashSet. */
public class SkillPanel extends Panel {

    private final MainFrame  frame;
    private final AppService service;

    private final TextField nameField     = new TextField(20);
    private final TextField categoryField = new TextField(20);
    private final Choice    levelChoice   = new Choice();
    private final TextArea  listArea      = new TextArea("", 10, 60, TextArea.SCROLLBARS_VERTICAL_ONLY);
    private final Label     statusLabel   = new Label("", Label.CENTER);

    private final List<Skill> displayed = new ArrayList<>();

    public SkillPanel(MainFrame frame, AppService service) {
        this.frame = frame; this.service = service;
        for (Skill.Level l : Skill.Level.values()) levelChoice.add(l.name());
        setBackground(new Color(240, 244, 248));
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        // Top bar
        Panel top = new Panel(new BorderLayout());
        top.setBackground(new Color(37, 99, 235));
        Label title = new Label("  Manage Skills", Label.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        top.add(title, BorderLayout.CENTER);
        Button back = new Button("← Dashboard");
        back.setBackground(new Color(30, 64, 175)); back.setForeground(Color.WHITE);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Form
        Panel form = new Panel(new GridBagLayout());
        form.setBackground(new Color(240, 244, 248));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10); g.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, g, "Skill Name:",  nameField,     0);
        addRow(form, g, "Category:",    categoryField, 1);
        addRow(form, g, "Proficiency:", levelChoice,   2);

        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; form.add(statusLabel, g);

        Panel btns = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        btns.setBackground(new Color(240, 244, 248));
        Button addBtn = new Button("Add Skill");
        addBtn.setBackground(new Color(37, 99, 235)); addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        Button delBtn = new Button("Delete Selected (by line#)");
        delBtn.setBackground(new Color(220, 38, 38)); delBtn.setForeground(Color.WHITE);
        btns.add(addBtn); btns.add(delBtn);
        g.gridy = 4; form.add(btns, g);

        Label lbl = new Label("Your Skills (HashSet — no duplicates by name):");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.gridy = 5; form.add(lbl, g);

        listArea.setEditable(false);
        listArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.gridy = 6; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        form.add(listArea, g);

        add(form, BorderLayout.CENTER);

        back.addActionListener(e -> frame.showCard(MainFrame.CARD_DASHBOARD));
        addBtn.addActionListener(e -> addSkill());
        delBtn.addActionListener(e -> deleteSelected());
    }

    private void addRow(Panel p, GridBagConstraints g, String lbl, Component field, int row) {
        g.gridwidth = 1; g.weightx = 0; g.gridx = 0; g.gridy = row; p.add(new Label(lbl), g);
        g.gridx = 1; g.weightx = 1; p.add(field, g);
    }

    /** Reloads skills from DB into the HashSet and refreshes the list display. */
    public void refresh() {
        if (frame.getCurrentUser() == null) return;
        HashSet<Skill> set = service.getSkills(frame.getCurrentUser().getId());
        displayed.clear(); displayed.addAll(set);
        render(); statusLabel.setText("");
    }

    private void render() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < displayed.size(); i++) {
            Skill s = displayed.get(i);
            sb.append(String.format("[%d] %-25s | %-20s | %s%n",
                      i + 1, s.getName(), s.getCategory(), s.getLevel().name()));
        }
        if (displayed.isEmpty()) sb.append("No skills added yet.");
        listArea.setText(sb.toString());
    }

    private void addSkill() {
        String name = nameField.getText().trim();
        String cat  = categoryField.getText().trim();
        Skill.Level lv = Skill.Level.valueOf(levelChoice.getSelectedItem());
        if (name.isEmpty()) { err("Skill name is required."); return; }
        if (cat.isEmpty())  { err("Category is required."); return; }

        Skill saved = service.addSkill(frame.getCurrentUser().getId(), name, cat, lv);
        if (saved == null) { err("Failed to save skill."); return; }
        nameField.setText(""); categoryField.setText("");
        ok("Skill '" + saved.getName() + "' added.");
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
            Skill sk = displayed.get(idx);
            if (service.deleteSkill(sk.getId())) { ok("Skill '" + sk.getName() + "' deleted."); refresh(); }
            else err("Failed to delete skill.");
        } catch (NumberFormatException ex) { err("Select the full line including [N]."); }
    }

    private void ok(String m)  { statusLabel.setForeground(new Color(22,163,74)); statusLabel.setText(m); }
    private void err(String m) { statusLabel.setForeground(Color.RED);            statusLabel.setText(m); }
}
