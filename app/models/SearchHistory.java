package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds search history for one client
 */
public class SearchHistory {
    /**
     * The maximum history should be showed on the search results
     */
    private static final int MAX_HISTORY = 10;
    // The search history
    private  final List<SearchResult> results = new ArrayList<>();
    // version is increased when any result in the history is updated
    private int version = 0;

    /**
     * The method addToHistory, add the search result with its sessionId to the history
     *
     * @param result the given search result
     */
    public void addToHistory(SearchResult result) {
        while (results.size() >= MAX_HISTORY) {
            results.remove(results.size() - 1);
        }
        results.add(0, result);
        version++;
    }

    /**
     * Update the history with the new search result
     */
    public void updateHistory(SearchResult newResult) {
        for (int i = 0; i < results.size(); i++) {
            SearchResult curr = results.get(i);
            if (curr.getInput().equals(newResult.getInput())) {
                if (!curr.getRepositories().equals(newResult.getRepositories())) {
                    results.set(i, newResult);
                    version++;
                }
            }
        }
    }

    /**
     * Returns the current changed version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns a copy of the current search results.
     */
    public List<SearchResult> getResults() {
        return new ArrayList<>(results);
    }
}
