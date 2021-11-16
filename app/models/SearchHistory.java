package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements a history service to retain the search history for each session.
 */
public class SearchHistory {
    /** The maximum history should be showed on the search results */
    private static final int MAX_HISTORY = 10;

    /** a map from session_id to its search history */
    private final Map<String, List<SearchResult>> sessions = new HashMap<>();

    /**
     * The method addToHistory, add the search result with its sessionId to the history
     * @param sessionId the sessionId
     * @param result the given search result
     */
    public synchronized void addToHistory(String sessionId, SearchResult result) {
        sessions.compute(sessionId, (k, curr) -> {
            if (curr == null) {
                curr = new ArrayList<>();
            } else if (curr.size() >= MAX_HISTORY) {
                curr = new ArrayList<>(curr.subList(0, MAX_HISTORY - 1));
            }
            curr.add(0, result);
            return curr;
        });
    }

    /**
     * The method getHistory
     * @param sessionId
     * @return
     */
    public synchronized List<SearchResult> getHistory(String sessionId) {
        return sessions.getOrDefault(sessionId, new ArrayList<>());
    }
}
