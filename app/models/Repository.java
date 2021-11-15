package models;

import java.util.List;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 * The Repository class to hold the content of a repository
 */
public class Repository {
    public String user;
    public String name;
    public List<String> topics;

    /** The empty constructor for json */
    public Repository() {
    }

    /** The parameterized constructor */
    public Repository(String user, String name, List<String> topics) {
        this.user = user;
        this.name = name;
        this.topics = topics;
    }

    /** The owner setter, it is needed for json */
    public void setOwner(Owner owner) {
        this.user = owner.login;
    }

    /** the static class Owner */
    public static class Owner {
        public String login;
    }
}
