package skillstack;

import java.sql.SQLException;

/** Application entry point — initialises the database and launches the AWT MainFrame. */
public class Main {

    public static void main(String[] args) {
        try {
            Database.getInstance(); // creates tables on first run
        } catch (SQLException e) {
            System.err.println("FATAL: Cannot connect to database.");
            System.err.println(e.getMessage());
            System.err.println("Ensure sqlite-jdbc JAR is on the classpath.");
            System.exit(1);
        }
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
    }
}
