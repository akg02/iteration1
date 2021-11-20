package controllers;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import models.GithubClient;
import models.Repository;
import models.SearchResult;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import play.Application;
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

public class SearchControllerTest extends WithApplication {

    public static class FakeGithubClient extends GithubClient {
        @Inject
        public FakeGithubClient(WSClient client, Config config) {
            super(client, config);
        }

        @Override
        public CompletionStage<SearchResult> searchRepositories(String query, boolean isTopic) {
            if ((query.equals("java programming") && !isTopic) || (query.equals("android") && isTopic)) {
                SearchResult result = new SearchResult();
                result.repositories = Arrays.asList(
                        new Repository("hope", "java", Arrays.asList("android", "security")),
                        new Repository("concordia", "android", Collections.emptyList()));
                result.input = query;
                return CompletableFuture.completedFuture(result);
            }
            throw new AssertionError("Unknown query");
        }
    }
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .bindings(Bindings.bind(GithubClient.class).to(FakeGithubClient.class))
                .build();
    }

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

    @Test
    public void testSearchWithoutInput() {
        Http.RequestBuilder searchRequest = new Http.RequestBuilder()
                .method(Helpers.POST)
                .uri("/search");
        Result result = Helpers.route(app, searchRequest);
        // Without input, we just redirect to the index page
        assertEquals(Http.Status.SEE_OTHER, result.status());
    }

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

    @Test
    public void testProfile() {
        // TODO: Individual task
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/profile/concordia");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
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
                .uri("/repository/octocat/Hello-World");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        String html = Helpers.contentAsString(result);

        System.out.println(html);
        assertTrue(html.contains("Repository Details"));
        assertTrue(html.contains("Hello-World"));
        assertTrue(html.contains("1710"));
        assertTrue(html.contains("1643"));
        assertTrue(html.contains("Issues"));
        assertTrue(html.contains("Wed Jan 26 14:01:12 EST 2011 "));
        assertTrue(html.contains("My first repository on GitHub!"));
        assertTrue(html.contains("I created this issue using Octokit!"));
        assertTrue(html.contains(" Issue Number : 1943"));
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