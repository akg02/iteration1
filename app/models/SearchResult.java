package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 * The SearchResult class to hold the search results from GitHub
 */
public class SearchResult {
    /** The input query string */
    public String input;

    /** The list of repositories */
    @JsonProperty("items")
    public List<Repository> repositories;

    /** The empty constructor for Json */
    public SearchResult() {
    }

    /** The parameterized constructor */
    public SearchResult(String input, List<Repository> repositories) {
        this.input = input;
        this.repositories = repositories;
    }
}
