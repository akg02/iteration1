package services;

import models.SearchHistory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements a history service to retain the search history for each session.
 */
public class HistoryService {
    /**
     * a map from session_id to its search history
     */
    private final Map<String, SearchHistory> sessions = new HashMap<>();

    /**
     * Returns the existing search history or a new one for the given client
     */
    public synchronized SearchHistory getHistory(String sessionId) {
        return sessions.compute(sessionId, (k, curr) -> {
            if (curr == null) {
                curr = new SearchHistory();
            }
            return curr;
        });
    }
}
