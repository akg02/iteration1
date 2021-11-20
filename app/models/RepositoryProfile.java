package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Model Class to represent Repository Details data
 * @author Sagar Sanghani
 * @version 1.2
 */

public class RepositoryProfile {

    @JsonProperty("topics")
    private List<String> topics;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_at")
    private Date created_at;

    @JsonProperty("updated_at")
    private Date updated_at;

    @JsonProperty("stargazers_count")
    private int stargazers_count;

    private List<Issue> issues;

    @JsonProperty("forks_count")
    private int forks_count;

    /**
     * Default constructor for json
     */
    public RepositoryProfile(){
    }

    /**
     *  Gets the topics list of the repository
     * @return List of topics
     */
    public List<String> getTopics() {
        return topics;
    }

    /**
     *  Gets the name of the repository
     * @return Name of the repository
     */
    public String getName() {
        return name;
    }

    /**
     *  Gets the description of the repository
     * @return description of the repository
     */
    public String getDescription() {
        return description;
    }

    /**
     *  Gets the creation date of the repository
     * @return creation date of the repository
     */
    public Date getCreated_at() {
        return created_at;
    }

    /**
     * Gets the last update date of the repository
     * @return last update date of the repository
     */
    public Date getUpdated_at() {
        return updated_at;
    }

    /**
     * Gets the number of stars on the repository
     * @return number of stars on the repository
     */
    public int getStargazers_count() {
        return stargazers_count;
    }

    /**
     * Gets the list of Issues object which has title, number, state, labels and body of the issue.
     * @return list of issue object
     */
    public List<Issue> getIssues() {
        return issues;
    }

    /**
     * Setter to set the issue attribute
     * @param issues List of issue object
     */
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    /**
     * Gets the number of forks on the repository
     * @return fork count of the repository
     */
    public int getForks_count() {
        return forks_count;
    }

}
