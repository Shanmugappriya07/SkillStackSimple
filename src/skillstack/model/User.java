package skillstack.model;

import java.io.Serializable;
import java.util.HashSet;

/** Represents a registered user; holds HashSet of skills and certifications for deduplication. */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name, email, password, careerGoal;
    private HashSet<Skill> skills           = new HashSet<>();
    private HashSet<Certification> certs    = new HashSet<>();

    public User() {}

    public User(int id, String name, String email, String password, String careerGoal) {
        this.id = id; this.name = name; this.email = email;
        this.password = password; this.careerGoal = careerGoal;
    }

    public int getId()               { return id; }
    public void setId(int v)         { this.id = v; }
    public String getName()          { return name; }
    public void setName(String v)    { this.name = v; }
    public String getEmail()         { return email; }
    public void setEmail(String v)   { this.email = v; }
    public String getPassword()      { return password; }
    public void setPassword(String v){ this.password = v; }
    public String getCareerGoal()    { return careerGoal; }
    public void setCareerGoal(String v){ this.careerGoal = v; }
    public HashSet<Skill> getSkills()             { return skills; }
    public void setSkills(HashSet<Skill> v)        { this.skills = v; }
    public HashSet<Certification> getCerts()       { return certs; }
    public void setCerts(HashSet<Certification> v) { this.certs = v; }

    @Override public String toString() { return "User{id=" + id + ", name='" + name + "'}"; }
}
