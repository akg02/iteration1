package models;

import java.util.List;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 */
public class Repository {
    public String user;
    public String name;
    public List<String> topics;

    // empty constructor for json
    public Repository() {

    }

    public Repository(String user, String name, List<String> topics) {
        this.user = user;
        this.name = name;
        this.topics = topics;
    }

    // This custom setter is needed for json
    public void setOwner(Owner owner) {
        this.user = owner.login;
    }

    public static class Owner {
        public String login;
    }
}
