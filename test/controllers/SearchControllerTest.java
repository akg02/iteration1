package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import models.GithubClient;
import models.Repository;
import models.SearchResult;

import org.junit.Test;
import play.Application;
import play.cache.AsyncCacheApi;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class SearchControllerTest
 * @author Hop Nguyen
 */
public class SearchControllerTest extends WithApplication {
    /**
     * Class FakeGithubClient
     * @author Hop Nguyen
     */
    public static class FakeGithubClient extends GithubClient {
        @Inject
        public FakeGithubClient(WSClient client, AsyncCacheApi cache, Config config) {
            super(client, cache, config);
        }

        @Override
        public CompletionStage<SearchResult> searchRepositories(String query, boolean isTopic) {
            if ((query.equals("java programming") && !isTopic) || (query.equals("android") && isTopic)) {
                SearchResult result = new SearchResult();
                result.setRepositories(Arrays.asList(
                        new Repository("hope", "java", Arrays.asList("android", "security")),
                        new Repository("concordia", "android", Collections.emptyList())));
                result.setInput(query);
                return CompletableFuture.completedFuture(result);
            }
            throw new AssertionError("Unknown query");
        }
    }

    /**
     * @author Hop Nguyen
     */
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .bindings(Bindings.bind(GithubClient.class).to(FakeGithubClient.class))
                .build();
    }

    /**
     * This is to test the empty home page
     * @author Hop Nguyen
     */
    @Test
    public void testEmptyHomePage() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        // Homepage contains a search box (see index.scala.html)
        assertTrue(Helpers.contentAsString(result)
                .contains(" <input type=\"text\" class=\"form-control\" placeholder=\"Enter search terms\" id=\"input\" name=\"input\">"));
        assertTrue(result.session().get(SearchController.SESSION_ID).isPresent());
    }

    /**
     * This is to test when doing the search without input
     * @author Hop Nguyen
     */
    @Test
    public void testSearchWithoutInput() {
        Http.RequestBuilder searchRequest = new Http.RequestBuilder()
                .method(Helpers.POST)
                .uri("/search");
        Result result = Helpers.route(app, searchRequest);
        // Without input, we just redirect to the index page
        assertEquals(Http.Status.SEE_OTHER, result.status());
    }
    /**
     * This is to test the search
     * @author Hop Nguyen
     */
    @Test
    public void testSearch() {
        // 1. Create a POST request containing a search query
        // 2. Execute a search request
        // 3. Verify a search response is redirected to the index page and contain a SESSION_ID
        // 4. Create a GET / request with the session id from the response from the search request
        // 5. Verify that the search history should be returned in the index page
        Http.RequestBuilder searchRequest = new Http.RequestBuilder()
                .method(Helpers.POST)
                .uri("/search")
                // simulate the search input from the search box
                .bodyForm(Collections.singletonMap("input", "java programming"))
                .session(SearchController.SESSION_ID, "session_1");
        Result searchResult = Helpers.route(app, searchRequest);
        // once the search action is finished, we redirect to the index page
        assertEquals(Http.Status.SEE_OTHER, searchResult.status());
        assertEquals(Optional.of("session_1"), searchResult.session().get(SearchController.SESSION_ID));

        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/")
                .session(SearchController.SESSION_ID, "session_1");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());

        String html = Helpers.contentAsString(result);
        // Two users
        assertTrue(html.contains("<a href=\"/profile/hope\">hope</a>"));
        assertTrue(html.contains("<a href=\"/profile/concordia\">concordia</a>"));
        // Two repositories
        assertTrue(html.contains("<a href=\"/repository/hope/java\">java</a>"));
        assertTrue(html.contains("<a href=\"/repository/concordia/android\">android</a>"));
        // Two topics
        assertTrue(html.contains("href=\"/topic/android\""));
        assertTrue(html.contains("href=\"/topic/security\""));
    }

    /**
     * This is to test for topics
     * @author Hop Nguyen
     */
    @Test
    public void testTopic() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/topic/android");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        String html = Helpers.contentAsString(result);
        // Two users
        assertTrue(html.contains("<a href=\"/profile/hope\">hope</a>"));
        assertTrue(html.contains("<a href=\"/profile/concordia\">concordia</a>"));
        // Two repositories
        assertTrue(html.contains("<a href=\"/repository/hope/java\">java</a>"));
        assertTrue(html.contains("<a href=\"/repository/concordia/android\">android</a>"));
        // Two topics
        assertTrue(html.contains("href=\"/topic/android\""));
        assertTrue(html.contains("href=\"/topic/security\""));
    }

    /**
     * Test case for profile() method
     * @author Joon Seung Hwang
     */
    @Test
    public void testProfile() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/profile/mayjoonjuly");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        String html = Helpers.contentAsString(result);
        
        assertTrue(html.contains("mayjoonjuly"));
        assertTrue(html.contains("Joon"));
        assertTrue(html.contains("Testing"));
        assertTrue(html.contains("abc"));
        assertTrue(html.contains("www"));
        assertTrue(html.contains("montreal"));
        assertTrue(html.contains("123"));
        assertTrue(html.contains("0"));
        assertTrue(html.contains("1"));
        assertTrue(html.contains("Desta25"));
        
    }
    
    /**
     * Test case for profile() method when no repo list
     * @author Joon Seung Hwang
     */
    @Test
    public void testProfile() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/profile/gloria0112");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        String html = Helpers.contentAsString(result);
        
        assertTrue(html.contains("gloria0112"));
        assertTrue(html.contains("No repos"));
        
    }

    /**
     *  Test case for repository() method
     *
     * @author Sagar Sanghani
     */

    @Test
    public void testRepository() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/repository/Sagar7421/dinosaur-name-generation-rnn");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        String html = Helpers.contentAsString(result);

        assertTrue(html.contains("Repository Details"));
        assertTrue(html.contains("dinosaur-name-generation-rnn"));
        assertTrue(html.contains("Sagar7421"));
        assertTrue(html.contains("1"));
        assertTrue(html.contains("0"));
        assertTrue(html.contains("Issues"));
        assertTrue(html.contains("Sat Oct 17 06:10:38 EDT 2020 "));
        assertTrue(html.contains("A dinosaur name generation using RNN in NumPy."));
        assertTrue(html.contains("This is an open issue. Just for demo stuff."));
        assertTrue(html.contains("Dinosaurs died long ago. Would love and be equally scared to meet a velociraptor."));
        assertTrue(html.contains(" Issue Number : 2"));
    }

    /**
     * Test case for when there is no topic list and no issue list in the repository
     *
     * @author Sagar Sanghani
     */
    @Test
    public void testRepositoryNoIssueNoTopic() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/repository/Sagar7421/justADummyRepo");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        String html = Helpers.contentAsString(result);

        assertTrue(html.contains("Repository Details"));
        assertTrue(html.contains("justADummyRepo"));
        assertTrue(html.contains("Sagar7421"));
        assertTrue(html.contains("No Topics"));
        assertTrue(html.contains("No Issues"));
    }
    
    /**
     * Test case issueStatistics() method
     * 
     * @author Meet Mehta
     * 
     */
    @Test
    public void testIssueStatistics() {
    	Http.RequestBuilder request = new Http.RequestBuilder().method(Helpers.GET).uri("/issueStatistics/meetmehta1198/StudentAttendanceManagement");
    	Result result = Helpers.route(app, request);
    	assertEquals(Http.Status.OK,result.status());
    	String html = Helpers.contentAsString(result);
    	assertTrue(html.contains("<li>running : 1</li>"));
    	assertTrue(html.contains("<li>Help : 1</li>"));
    	assertTrue(html.contains("<li>this : 1</li>"));
    	assertTrue(html.contains("<li>for : 1</li>"));
    	assertTrue(html.contains("<li>project : 1</li>"));
    }

    /**
     *
     * @author Smit Parmar
     */
    @Test
    public void testCommitStatistics() {
        Http.RequestBuilder request = new Http.RequestBuilder().method(Helpers.GET)
                .uri("/commits/smituparmar/MedicoGraph");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        String html = Helpers.contentAsString(result);
        assertTrue(html.contains("<a href=\"/profile/smituparmar\">smituparmar</a>"));
        assertTrue(html.contains("<li>Count: 19</li>"));
    }
}