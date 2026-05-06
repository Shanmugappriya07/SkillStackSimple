package skillstack;

import java.io.Serializable;
import java.util.Objects;

/** Represents a technical skill; equals/hashCode on (userId, name) for HashSet deduplication. */
public class Skill implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Level { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }

    private int id, userId;
    private String name, category;
    private Level level;

    public Skill() {}

    public Skill(int id, int userId, String name, String category, Level level) {
        this.id = id; this.userId = userId;
        this.name = name; this.category = category; this.level = level;
    }

    public int getId()           { return id; }
    public void setId(int id)    { this.id = id; }
    public int getUserId()       { return userId; }
    public void setUserId(int v) { this.userId = v; }
    public String getName()      { return name; }
    public void setName(String v){ this.name = v; }
    public String getCategory()      { return category; }
    public void setCategory(String v){ this.category = v; }
    public Level getLevel()          { return level; }
    public void setLevel(Level v)    { this.level = v; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Skill)) return false;
        Skill s = (Skill) o;
        return userId == s.userId && Objects.equals(name, s.name);
    }

    @Override public int hashCode() { return Objects.hash(userId, name); }

    @Override public String toString() { return name + " [" + category + "] - " + level; }
}
