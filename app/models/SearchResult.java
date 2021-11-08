package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 */
public class SearchResult {
    public String input;

    @JsonProperty("items")
    public List<Repository> repositories;

    // empty constructor for json
    public SearchResult() {
    }

    public SearchResult(String input, List<Repository> repositories) {
        this.input = input;
        this.repositories = repositories;
    }
}
