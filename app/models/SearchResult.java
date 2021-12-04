package models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 * The SearchResult class to hold the search results from GitHub
 */
public class SearchResult {

    private String input;

    private List<Repository> repositories;

    /**
     * The empty constructor for Json
     *
     * @author Hop Nguyen
     */
    public SearchResult() {
    }

    /**
     * The parameterized constructor
     *
     * @author Hop Nguyen
     */
    public SearchResult(String input, List<Repository> repositories) {
        this.input = input;
        this.repositories = repositories;
    }

    /**
     * Returns the input query
     *
     * @return the input query
     * @author Hop Nguyen
     */
    public String getInput() {
        return input;
    }

    /**
     * Set the input query
     *
     * @param input input query
     * @author Hop Nguyen
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Return the list of repositories
     *
     * @return the repository list
     * @author Hop Nguyen
     */
    @JsonGetter("items")
    public List<Repository> getRepositories() {
        return repositories;
    }

    /**
     * Set the repositories list
     *
     * @param repositories the repositories list
     * @author Hop Nguyen
     */
    @JsonSetter("items")
    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }
}
