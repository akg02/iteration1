package models;

import org.junit.Test;
import services.HistoryService;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The class SearchHistoryTest
 * @author Hop Nguyen
 */
public class SearchHistoryTest {
    /**
     * This is to test the history of the search
     * @author Hop Nguyen
     */
    @Test
    public void testHistory() {
        HistoryService historyService = new HistoryService();
        SearchHistory history = historyService.getHistory("session_1");
        assertTrue(history.getResults().isEmpty());

        // Add up to 10 items
        for (int i = 1; i <= 10; i++) {
            String query = "query-" + i;
            String user = "user-" + i;
            String repo = "repo-" + i;
            SearchResult result = new SearchResult(query,
                    Arrays.asList(new Repository(user, repo, Collections.emptyList())));
            history.addToHistory(result);
            assertEquals(i, history.getResults().size());
        }
        assertEquals(10, historyService.getHistory("session_1").getResults().size());

        // Add more items should discard old items
        for (int i = 1; i <= 10; i++) {
            String query = "new-query-" + i;
            String user = "user-" + i;
            String repo = "repo-" + i;
            SearchResult result = new SearchResult(query,
                    Arrays.asList(new Repository(user, repo, Collections.emptyList())));
            history.addToHistory(result);
            assertEquals(10, history.getResults().size());
        }
        assertEquals(10, historyService.getHistory("session_1").getResults().size());
        assertTrue(historyService.getHistory("session_2").getResults().isEmpty());
    }
}