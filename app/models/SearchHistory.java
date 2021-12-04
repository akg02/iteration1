package models;

import play.libs.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public synchronized void addToHistory(SearchResult result) {
        updateHistory(result);
        while (results.size() >= MAX_HISTORY) {
            results.remove(results.size() - 1);
        }
        results.add(0, result);
        version++;
    }

    public synchronized void updateHistory(SearchResult newResult) {
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
     * Returns the list of queries of this client
     */
    public synchronized List<String> getQueries() {
        return results.stream().map(SearchResult::getInput).distinct().collect(Collectors.toList());
    }

    /**
     * Returns the current changed version
     */
    public synchronized int getVersion() {
        return version;
    }

    /**
     * Returns a copy of the current search results.
     */
    public synchronized List<SearchResult> getResults() {
        return new ArrayList<>(results);
    }

    /**
     * Returns JSON presentation of the search results of this history
     */
    public String toJson() {
        return Json.stringify(Json.toJson(getResults()));
    }
}
