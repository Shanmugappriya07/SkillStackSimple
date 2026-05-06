package skillstack;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

/**
 * AppService — all business logic in one class.
 * Replaces: AuthService, SkillService, CertificationService, ResumeService, SerializationService.
 */
public class AppService {

    private final Database db;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public AppService() {
        try { db = Database.getInstance(); }
        catch (Exception e) { throw new RuntimeException("Cannot connect to database: " + e.getMessage(), e); }
    }

    // ── AUTH ─────────────────────────────────────────────────────────────

    /**
     * Registers a new user. Returns the saved User on success, null on failure.
     * Validates name, email format, and minimum password length.
     */
    public User register(String name, String email, String password, String careerGoal) {
        if (name == null || name.trim().isEmpty()) return null;
        if (email == null || !email.contains("@"))  return null;
        if (password == null || password.length() < 4) return null;
        if (db.findUserByEmail(email.trim()) != null) return null;

        User u = new User();
        u.setName(name.trim()); u.setEmail(email.trim());
        u.setPassword(password);
        u.setCareerGoal(careerGoal != null ? careerGoal.trim() : "");
        int id = db.saveUser(u);
        if (id == -1) return null;
        u.setId(id);
        return u;
    }

    /**
     * Authenticates a user by email and password.
     * Returns the matching User on success, null if credentials are wrong.
     */
    public User login(String email, String password) {
        if (email == null || password == null) return null;
        User u = db.findUserByEmail(email.trim());
        if (u == null || !u.getPassword().equals(password)) return null;
        return u;
    }

    // ── SKILLS ───────────────────────────────────────────────────────────

    /**
     * Adds a skill for the given user.
     * Returns the saved Skill with generated ID, or null on validation failure.
     */
    public Skill addSkill(int userId, String name, String category, Skill.Level level) {
        if (name == null || name.trim().isEmpty()) return null;
        if (category == null || category.trim().isEmpty()) return null;
        if (level == null) return null;

        Skill sk = new Skill();
        sk.setUserId(userId);
        sk.setName(name.trim());
        sk.setCategory(category.trim());
        sk.setLevel(level);

        int id = db.saveSkill(sk);
        if (id == -1) return null;
        sk.setId(id);
        return sk;
    }

    /** Returns all skills for a user as a HashSet. */
    public HashSet<Skill> getSkills(int userId) {
        return db.findSkillsByUser(userId);
    }

    /** Returns skills filtered by category. */
    public HashSet<Skill> getSkillsByCategory(int userId, String category) {
        return db.findSkillsByCategory(userId, category);
    }

    /** Deletes a skill by ID; returns true on success. */
    public boolean deleteSkill(int skillId) {
        return db.deleteSkill(skillId);
    }

    // ── CERTIFICATIONS ───────────────────────────────────────────────────

    /**
     * Adds a certification for the given user.
     * Returns the saved Certification with generated ID, or null on validation failure.
     */
    public Certification addCert(int userId, String title, String issuer,
                                 String issueDate, String expiryDate, String credentialId) {
        if (title == null || title.trim().isEmpty()) return null;
        if (issuer == null || issuer.trim().isEmpty()) return null;

        Certification c = new Certification();
        c.setUserId(userId);
        c.setTitle(title.trim());
        c.setIssuer(issuer.trim());
        c.setIssueDate(issueDate  != null ? issueDate.trim()  : "");
        c.setExpiryDate(expiryDate != null ? expiryDate.trim() : "");
        c.setCredentialId(credentialId != null ? credentialId.trim() : "");

        int id = db.saveCert(c);
        if (id == -1) return null;
        c.setId(id);
        return c;
    }

    /** Returns all certifications for a user as a HashSet. */
    public HashSet<Certification> getCerts(int userId) {
        return db.findCertsByUser(userId);
    }

    /** Deletes a certification by ID; returns true on success. */
    public boolean deleteCert(int certId) {
        return db.deleteCert(certId);
    }

    // ── FILE HANDLING — Resume Export ─────────────────────────────────────

    /**
     * Generates a plain-text resume using BufferedWriter / FileWriter.
     * Saves to resumes/<Name>_resume.txt. Returns the absolute file path.
     * Throws IOException on any file error (FILE HANDLING requirement).
     */
    public String generateResume(User user, HashSet<Skill> skills,
                                 HashSet<Certification> certs) throws IOException {
        File dir = new File("resumes");
        if (!dir.exists() && !dir.mkdirs())
            throw new IOException("Could not create resumes/ directory.");

        String safeName = user.getName().replaceAll("[^a-zA-Z0-9_]", "_");
        String path = "resumes" + File.separator + safeName + "_resume.txt";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write("=========================================================="); bw.newLine();
            bw.write("  SKILLSTACK RESUME"); bw.newLine();
            bw.write("  Generated: " + LocalDate.now()); bw.newLine();
            bw.write("=========================================================="); bw.newLine();
            bw.newLine();

            bw.write("PERSONAL INFORMATION"); bw.newLine();
            bw.write("--------------------"); bw.newLine();
            bw.write("Name        : " + user.getName()); bw.newLine();
            bw.write("Email       : " + user.getEmail()); bw.newLine();
            bw.write("Career Goal : " + (user.getCareerGoal() != null ? user.getCareerGoal() : "N/A"));
            bw.newLine(); bw.newLine();

            bw.write("TECHNICAL SKILLS (" + skills.size() + " total)"); bw.newLine();
            bw.write("--------------------"); bw.newLine();
            if (skills.isEmpty()) { bw.write("  No skills added yet."); bw.newLine(); }
            else for (Skill s : skills) {
                bw.write(String.format("  %-30s | %-20s | %s", s.getName(), s.getCategory(), s.getLevel().name()));
                bw.newLine();
            }
            bw.newLine();

            bw.write("CERTIFICATIONS (" + certs.size() + " total)"); bw.newLine();
            bw.write("--------------------"); bw.newLine();
            if (certs.isEmpty()) { bw.write("  No certifications added yet."); bw.newLine(); }
            else for (Certification c : certs) {
                bw.write("  Title      : " + c.getTitle()); bw.newLine();
                bw.write("  Issuer     : " + c.getIssuer()); bw.newLine();
                bw.write("  Issued     : " + c.getIssueDate()); bw.newLine();
                bw.write("  Expires    : " + (c.getExpiryDate().isEmpty() ? "No Expiry" : c.getExpiryDate())); bw.newLine();
                bw.write("  Credential : " + c.getCredentialId()); bw.newLine();
                bw.write("  --"); bw.newLine();
            }
            bw.newLine();
            bw.write("=========================================================="); bw.newLine();
            bw.write("  Generated by SkillStack"); bw.newLine();
            bw.write("=========================================================="); bw.newLine();
        }
        return new File(path).getAbsolutePath();
    }

    // ── SERIALIZATION — Profile Snapshots ────────────────────────────────

    /**
     * Serializes the User object to snapshots/user_<id>_<timestamp>.ser
     * using ObjectOutputStream. Returns the absolute file path.
     * Throws IOException on any error (SERIALIZATION requirement).
     */
    public String saveSnapshot(User user) throws IOException {
        File dir = new File("snapshots");
        if (!dir.exists() && !dir.mkdirs())
            throw new IOException("Could not create snapshots/ directory.");

        String path = "snapshots" + File.separator +
                      "user_" + user.getId() + "_" + LocalDateTime.now().format(FMT) + ".ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(user);
        }
        return new File(path).getAbsolutePath();
    }

    /**
     * Deserializes a User from the given .ser file path using ObjectInputStream.
     * Throws IOException or ClassNotFoundException on error (SERIALIZATION requirement).
     */
    public User loadSnapshot(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (!(obj instanceof User))
                throw new ClassNotFoundException("File does not contain a User snapshot.");
            return (User) obj;
        }
    }

    /** Returns the most recent .ser snapshot file for a userId, or null if none exists. */
    public File findLatestSnapshot(int userId) {
        File dir = new File("snapshots");
        if (!dir.exists()) return null;
        File[] files = dir.listFiles((d, n) -> n.startsWith("user_" + userId + "_") && n.endsWith(".ser"));
        if (files == null || files.length == 0) return null;
        File latest = files[0];
        for (File f : files) if (f.lastModified() > latest.lastModified()) latest = f;
        return latest;
    }
}
