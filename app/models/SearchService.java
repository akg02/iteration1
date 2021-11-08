package models;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 */
public class SearchService {
    private static final int MAX_HISTORY = 10;
    private final GithubClient github;
    private final Map<String, List<SearchResult>> sessions;

    @Inject
    public SearchService(GithubClient github) {
        this.sessions = new HashMap<>();
        this.github = github;
    }

    private synchronized void addToHistory(String sessionId, SearchResult result) {
        sessions.compute(sessionId, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            } else if (v.size() >= MAX_HISTORY) {
                v = new ArrayList<>(v.subList(0, MAX_HISTORY - 1));
            }
            v.add(0, result);
            return v;
        });
    }

    public CompletionStage<Void> searchThenAddToHistory(String sessionId, String query) {
        return github.searchRepositories(query, false)
                .thenApply(result -> {
                    addToHistory(sessionId, result);
                    return null;
                });
    }

    public synchronized List<SearchResult> getHistory(String sessionId) {
        return sessions.getOrDefault(sessionId, new ArrayList<>());
    }
}
