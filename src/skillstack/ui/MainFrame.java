package skillstack.ui;

import skillstack.model.User;
import skillstack.service.AppService;

import java.awt.*;

/** Main AWT Frame that hosts all panels via CardLayout */
public class MainFrame extends Frame {

    public static final String CARD_LOGIN     = "LOGIN";
    public static final String CARD_REGISTER  = "REGISTER";
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_SKILLS    = "SKILLS";
    public static final String CARD_CERTS     = "CERTS";

    private final CardLayout  cardLayout = new CardLayout();
    private final Panel       cardPanel  = new Panel(cardLayout);

    private User currentUser;

    private final AppService service   = new AppService();
    private final LoginPanel      login     ;
    private final RegisterPanel   register  ;
    private final DashboardPanel  dashboard ;
    private final SkillPanel      skills    ;
    private final CertificationPanel certs  ;

    public MainFrame() {
        super("SkillStack — Skill & Certification Manager");
        setSize(750, 580);
        setLocationRelativeTo(null);
        setBackground(new Color(240, 244, 248));
        setLayout(new BorderLayout());

        login     = new LoginPanel(this, service);
        register  = new RegisterPanel(this, service);
        dashboard = new DashboardPanel(this, service);
        skills    = new SkillPanel(this, service);
        certs     = new CertificationPanel(this, service);

        cardPanel.add(login,     CARD_LOGIN);
        cardPanel.add(register,  CARD_REGISTER);
        cardPanel.add(dashboard, CARD_DASHBOARD);
        cardPanel.add(skills,    CARD_SKILLS);
        cardPanel.add(certs,     CARD_CERTS);
        add(cardPanel, BorderLayout.CENTER);

        showCard(CARD_LOGIN);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); }
        });
    }

    /** Switches the visible card and calls refresh() on data-dependent panels. */
    public void showCard(String name) {
        cardLayout.show(cardPanel, name);
        if (CARD_DASHBOARD.equals(name)) dashboard.refresh();
        if (CARD_SKILLS.equals(name))    skills.refresh();
        if (CARD_CERTS.equals(name))     certs.refresh();
    }

    public void setCurrentUser(User u) { this.currentUser = u; }
    public User getCurrentUser()       { return currentUser; }

    public void logout() {
        this.currentUser = null;
        showCard(CARD_LOGIN);
    }
}
