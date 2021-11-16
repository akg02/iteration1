package models;

/**
 * Model class which stored data after making an API call
 * @author Smit Parmar
 */
public class CommitStats {
    private String name;
    private String email;
    private int addition;
    private int deletion;
    private String sha;

    /**
     * Default Constructor
     */
    public CommitStats() {
    }

    /**
     * This is parameterised Constructor
     */
    public CommitStats(String author, String email, int addition, int deletion, String sha) {
        this.name = author;
        this.email = email;
        this.addition = addition;
        this.deletion = deletion;
        this.sha = sha;
    }

    public String getName() {
        return name;
    }

    public void setName(String author) {
        this.name = author;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAddition() {
        return addition;
    }

    public void setAddition(int addition) {
        this.addition = addition;
    }

    public int getDeletion() {
        return deletion;
    }

    public void setDeletion(int deletion) {
        this.deletion = deletion;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    @Override
    public String toString() {
        return "CommitStats{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", addition=" + addition +
                ", deletion=" + deletion +
                ", sha='" + sha + '\'' +
                "}\n";
    }
}
