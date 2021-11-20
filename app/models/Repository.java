package models;

import java.util.List;

/**
 * Hold a repository consisting of name, owner, and list of topics.
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 * The Repository class to hold the content of a repository
 */
public class Repository {
    private String user;
    private String name;
    private List<String> topics;

    /**
     * The empty constructor for json
     * @author Hop Nguyen
     */
    public Repository() {
    }

    /**
     * The parameterized constructor
     * @author Hop Nguyen
     */
    public Repository(String user, String name, List<String> topics) {
        this.user = user;
        this.name = name;
        this.topics = topics;
    }

    /**
     * The owner setter, it is needed for json. The name owner is used for Jackson
     * @author Hop Nguyen
     */
    public void setOwner(Owner owner) {
        this.user = owner.login;
    }

    /**
     * The getter of user
     *
     * @author Hop Nguyen
     */
    public String getUser() {
        return user;
    }

    /**
     * The getter of name
     *
     * @author Hop Nguyen
     */
    public String getName() {
        return name;
    }

    /**
     * The setter of name
     *
     * @author Hop Nguyen
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The getter of topic
     *
     * @author Hop Nguyen
     */
    public List<String> getTopics() {
        return topics;
    }

    /**
     * The setter of topic
     *
     * @author Hop Nguyen
     */
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    /**
     * the static class Owner
     *
     * @author Hop Nguyen
     */
    public static class Owner {
        private String login;

        /**
         * Set the owner of the repository
         *
         * @author Hop Nguyen
         */
        public void setLogin(String login) {
            this.login = login;
        }
    }
}
