package models;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchHistoryTest {

    @Test
    public void testHistory() {
        SearchHistory searchHistory = new SearchHistory();
        assertTrue(searchHistory.getHistory("session_1").isEmpty());

        // Add up to 10 items
        for (int i = 1; i <= 10; i++) {
            String query = "query-" + i;
            String user = "user-" + i;
            String repo = "repo-" + i;
            SearchResult result = new SearchResult(query,
                    Arrays.asList(new Repository(user, repo, Collections.emptyList())));
            searchHistory.addToHistory("session_1", result);
            List<SearchResult> history = searchHistory.getHistory("session_1");
            assertEquals(i, history.size());
        }
        assertEquals(10, searchHistory.getHistory("session_1").size());

        // Add more items should discard old items
        for (int i = 1; i <= 10; i++) {
            String query = "new-query-" + i;
            String user = "user-" + i;
            String repo = "repo-" + i;
            SearchResult result = new SearchResult(query,
                    Arrays.asList(new Repository(user, repo, Collections.emptyList())));
            searchHistory.addToHistory("session_1", result);
            List<SearchResult> history = searchHistory.getHistory("session_1");
            assertEquals(10, history.size());
        }
        assertEquals(10, searchHistory.getHistory("session_1").size());
        assertTrue(searchHistory.getHistory("session_2").isEmpty());
    }
}