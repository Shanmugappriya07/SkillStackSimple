package skillstack;

import java.io.Serializable;
import java.util.Objects;

/** Represents a certification; equals/hashCode on (userId, credentialId) for HashSet deduplication. */
public class Certification implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id, userId;
    private String title, issuer, issueDate, expiryDate, credentialId;

    public Certification() {}

    public Certification(int id, int userId, String title, String issuer,
                         String issueDate, String expiryDate, String credentialId) {
        this.id = id; this.userId = userId; this.title = title;
        this.issuer = issuer; this.issueDate = issueDate;
        this.expiryDate = expiryDate; this.credentialId = credentialId;
    }

    public int getId()               { return id; }
    public void setId(int v)         { this.id = v; }
    public int getUserId()           { return userId; }
    public void setUserId(int v)     { this.userId = v; }
    public String getTitle()         { return title; }
    public void setTitle(String v)   { this.title = v; }
    public String getIssuer()        { return issuer; }
    public void setIssuer(String v)  { this.issuer = v; }
    public String getIssueDate()     { return issueDate; }
    public void setIssueDate(String v)  { this.issueDate = v; }
    public String getExpiryDate()    { return expiryDate; }
    public void setExpiryDate(String v) { this.expiryDate = v; }
    public String getCredentialId()     { return credentialId; }
    public void setCredentialId(String v){ this.credentialId = v; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Certification)) return false;
        Certification c = (Certification) o;
        return userId == c.userId && Objects.equals(credentialId, c.credentialId);
    }

    @Override public int hashCode() { return Objects.hash(userId, credentialId); }

    @Override public String toString() { return title + " by " + issuer + " (" + issueDate + ")"; }
}
