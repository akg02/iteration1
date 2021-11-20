package models;

/**
 * Model class of commit data for counting statistics
 * @author Smit Parmar
 */
public class Commits {
    private String userName;
    private int addition;
    private int deletion;
    private int count;

    /**
     * This is parametrised constructor
     */
    public Commits(String userName, int addition, int deletion) {
        this.userName = userName;
        this.addition = addition;
        this.deletion = deletion;
        this.count = 1;
    }

    public String getUserName() {
        return userName;
    }

    public int getAddition() {
        return addition;
    }

    public int getDeletion() {
        return deletion;
    }

}
