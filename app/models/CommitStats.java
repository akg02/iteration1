package models;

public class CommitStats {
    private String name;
    private String email;
    private int addition;
    private int deletion;

    public CommitStats() {
    }

    public CommitStats(String author, String email, int addition, int deletion) {
        this.name = author;
        this.email = email;
        this.addition = addition;
        this.deletion = deletion;
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

    @Override
    public String toString() {
        return "CommitStats{" +
                "author='" + name + '\'' +
                ", email='" + email + '\'' +
                ", addition=" + addition +
                ", deletion=" + deletion +
                '}';
    }
}
