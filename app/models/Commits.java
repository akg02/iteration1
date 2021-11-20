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

    public void setUserName(String userName) {
        this.userName = userName;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Commits{" +
                "userName='" + userName + '\'' +
                ", addition=" + addition +
                ", deletion=" + deletion +
                ", count=" + count +
                '}';
    }
}
