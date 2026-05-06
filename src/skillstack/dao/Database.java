package skillstack.dao;

import skillstack.model.Certification;
import skillstack.model.Skill;
import skillstack.model.User;

import java.sql.*;
import java.util.HashSet;

/**
 * Database — Singleton JDBC connection + all DAO operations in one class.
 * Replaces: DatabaseConnection, GenericDAO, UserDAO, SkillDAO, CertificationDAO.
 * Creates the SQLite schema on first run. All queries use PreparedStatement.
 */
public class Database {

    private static final String URL = "jdbc:sqlite:skillstack.db";
    private static Database instance;
    private final Connection conn;

    // ── Singleton ────────────────────────────────────────────────────────

    private Database() throws SQLException {
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException e) {
            throw new SQLException("sqlite-jdbc driver not found. Add the JAR to the classpath.", e);
        }
        conn = DriverManager.getConnection(URL);
        conn.setAutoCommit(true);
        createTables();
    }

    public static synchronized Database getInstance() throws SQLException {
        if (instance == null || instance.conn.isClosed())
            instance = new Database();
        return instance;
    }

    private Connection c() { return conn; }

    // ── Schema ───────────────────────────────────────────────────────────

    private void createTables() throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users(" +
                      "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "name TEXT NOT NULL, email TEXT NOT NULL UNIQUE," +
                      "password TEXT NOT NULL, career_goal TEXT)");
            s.execute("CREATE TABLE IF NOT EXISTS skills(" +
                      "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "user_id INTEGER NOT NULL, name TEXT NOT NULL," +
                      "category TEXT NOT NULL, level TEXT NOT NULL," +
                      "FOREIGN KEY(user_id) REFERENCES users(id))");
            s.execute("CREATE TABLE IF NOT EXISTS certifications(" +
                      "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "user_id INTEGER NOT NULL, title TEXT NOT NULL," +
                      "issuer TEXT NOT NULL, issue_date TEXT," +
                      "expiry_date TEXT, credential_id TEXT," +
                      "FOREIGN KEY(user_id) REFERENCES users(id))");
        }
    }

    // ── User DAO ─────────────────────────────────────────────────────────

    /** Saves a new user and returns the generated ID, or -1 on failure. */
    public int saveUser(User u) {
        String sql = "INSERT INTO users(name,email,password,career_goal) VALUES(?,?,?,?)";
        try (PreparedStatement ps = c().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName()); ps.setString(2, u.getEmail());
            ps.setString(3, u.getPassword()); ps.setString(4, u.getCareerGoal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { System.err.println("saveUser: " + e.getMessage()); }
        return -1;
    }

    /** Finds a user by email; returns null if not found. */
    public User findUserByEmail(String email) {
        try (PreparedStatement ps = c().prepareStatement("SELECT * FROM users WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapUser(rs); }
        } catch (SQLException e) { System.err.println("findUserByEmail: " + e.getMessage()); }
        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("password"),
                        rs.getString("career_goal"));
    }

    // ── Skill DAO ────────────────────────────────────────────────────────

    /** Saves a new skill and returns the generated ID, or -1 on failure. */
    public int saveSkill(Skill sk) {
        String sql = "INSERT INTO skills(user_id,name,category,level) VALUES(?,?,?,?)";
        try (PreparedStatement ps = c().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sk.getUserId()); ps.setString(2, sk.getName());
            ps.setString(3, sk.getCategory()); ps.setString(4, sk.getLevel().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { System.err.println("saveSkill: " + e.getMessage()); }
        return -1;
    }

    /** Returns all skills for a user as a HashSet (deduplicates by userId+name). */
    public HashSet<Skill> findSkillsByUser(int userId) {
        HashSet<Skill> set = new HashSet<>();
        try (PreparedStatement ps = c().prepareStatement("SELECT * FROM skills WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(mapSkill(rs));
            }
        } catch (SQLException e) { System.err.println("findSkillsByUser: " + e.getMessage()); }
        return set;
    }

    /** Returns skills for a user filtered by category. */
    public HashSet<Skill> findSkillsByCategory(int userId, String category) {
        HashSet<Skill> set = new HashSet<>();
        try (PreparedStatement ps = c().prepareStatement(
                "SELECT * FROM skills WHERE user_id=? AND category=?")) {
            ps.setInt(1, userId); ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(mapSkill(rs));
            }
        } catch (SQLException e) { System.err.println("findSkillsByCategory: " + e.getMessage()); }
        return set;
    }

    /** Deletes a skill by ID; returns true on success. */
    public boolean deleteSkill(int id) {
        try (PreparedStatement ps = c().prepareStatement("DELETE FROM skills WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("deleteSkill: " + e.getMessage()); }
        return false;
    }

    private Skill mapSkill(ResultSet rs) throws SQLException {
        return new Skill(rs.getInt("id"), rs.getInt("user_id"),
                         rs.getString("name"), rs.getString("category"),
                         Skill.Level.valueOf(rs.getString("level")));
    }

    // ── Certification DAO ────────────────────────────────────────────────

    /** Saves a new certification and returns the generated ID, or -1 on failure. */
    public int saveCert(Certification c) {
        String sql = "INSERT INTO certifications(user_id,title,issuer,issue_date,expiry_date,credential_id)" +
                     " VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getUserId()); ps.setString(2, c.getTitle());
            ps.setString(3, c.getIssuer()); ps.setString(4, c.getIssueDate());
            ps.setString(5, c.getExpiryDate()); ps.setString(6, c.getCredentialId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { System.err.println("saveCert: " + e.getMessage()); }
        return -1;
    }

    /** Returns all certifications for a user as a HashSet (deduplicates by userId+credentialId). */
    public HashSet<Certification> findCertsByUser(int userId) {
        HashSet<Certification> set = new HashSet<>();
        try (PreparedStatement ps = c().prepareStatement(
                "SELECT * FROM certifications WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(mapCert(rs));
            }
        } catch (SQLException e) { System.err.println("findCertsByUser: " + e.getMessage()); }
        return set;
    }

    /** Deletes a certification by ID; returns true on success. */
    public boolean deleteCert(int id) {
        try (PreparedStatement ps = c().prepareStatement("DELETE FROM certifications WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("deleteCert: " + e.getMessage()); }
        return false;
    }

    private Certification mapCert(ResultSet rs) throws SQLException {
        return new Certification(rs.getInt("id"), rs.getInt("user_id"),
                                 rs.getString("title"), rs.getString("issuer"),
                                 rs.getString("issue_date"), rs.getString("expiry_date"),
                                 rs.getString("credential_id"));
    }
}
