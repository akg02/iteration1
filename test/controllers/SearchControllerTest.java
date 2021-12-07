package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
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
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.mvc.Result;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClient;
import play.shaded.ahc.org.asynchttpclient.DefaultAsyncHttpClientConfig;
import play.shaded.ahc.org.asynchttpclient.netty.ws.NettyWebSocket;
import play.test.Helpers;
import play.test.TestServer;
import play.test.WithApplication;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.awaitility.Awaitility.await;
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
     * This is to test the search page
     * @author Hop Nguyen
     */
    @Test
    public void testSearchPage() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
        // Homepage contains a search box (see search.scala.html)
        assertTrue(Helpers.contentAsString(result)
                .contains(" <input type=\"text\" class=\"form-control\" placeholder=\"Enter search terms\" id=\"search-input\" name=\"search-input\">"));
        assertTrue(result.session().get(SearchController.SESSION_ID).isPresent());
    }

    /**
     * This is to test the search
     * @author Hop Nguyen
     */
    @Test
    public void testSearchWebSocket() {
        TestServer server = Helpers.testServer(app);
        Helpers.running(server, () -> {
            AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().setMaxRequestRetry(0).build();
            try (AsyncHttpClient httpClient = new DefaultAsyncHttpClient(config)) {
                WebSocketClient webSocketClient = new WebSocketClient(httpClient);
                List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
                WebSocketClient.LoggingListener listener = new WebSocketClient.LoggingListener(receivedMessages::add);
                NettyWebSocket webSocket = webSocketClient.call(webSocketURL(server, "/websocket/search"), listener);

                // Await until we receive the response
                webSocket.sendTextFrame("java programming");
                await().until(() -> receivedMessages.size() == 1);

                ArrayNode searchHistory = Json.fromJson(Json.parse(receivedMessages.get(0)), ArrayNode.class);
                assertEquals(1, searchHistory.size());
                SearchResult searchResult = Json.fromJson(searchHistory.get(0), SearchResult.class);
                assertEquals("java programming", searchResult.getInput());
                assertEquals(2, searchResult.getRepositories().size());
                assertEquals("hope", searchResult.getRepositories().get(0).getUser());
                assertEquals("java", searchResult.getRepositories().get(0).getName());
                assertEquals("concordia", searchResult.getRepositories().get(1).getUser());
                assertEquals("android", searchResult.getRepositories().get(1).getName());
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
    }

    /**
     * This is to test for topics
     * @author Hop Nguyen
     */
    @Test
    public void testTopicPage() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(Helpers.GET)
                .uri("/topic/android");
        Result result = Helpers.route(app, request);
        assertEquals(Http.Status.OK, result.status());
    }

    /**
     * @author Hop Nguyen
     */
    @Test
    public void testTopicWebSocket() {
        TestServer server = Helpers.testServer(app);
        Helpers.running(server, () -> {
            AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder().setMaxRequestRetry(0).build();
            try (AsyncHttpClient httpClient = new DefaultAsyncHttpClient(config)) {
                WebSocketClient webSocketClient = new WebSocketClient(httpClient);
                List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
                WebSocketClient.LoggingListener listener = new WebSocketClient.LoggingListener(receivedMessages::add);
                NettyWebSocket webSocket = webSocketClient.call(webSocketURL(server, "/websocket/topic"), listener);
                // Await until the web socket is connected
                await().until(webSocket::isOpen);
                webSocket.sendTextFrame("android");
                // Await until we receive the response
                await().until(() -> receivedMessages.size() > 0);
                SearchResult searchResult = Json.fromJson(Json.parse(receivedMessages.get(0)), SearchResult.class);
                assertEquals("android", searchResult.getInput());
                assertEquals(2, searchResult.getRepositories().size());
                assertEquals("hope", searchResult.getRepositories().get(0).getUser());
                assertEquals("java", searchResult.getRepositories().get(0).getName());
                assertEquals("concordia", searchResult.getRepositories().get(1).getUser());
                assertEquals("android", searchResult.getRepositories().get(1).getName());
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
    }

    private String webSocketURL(TestServer server, String endpoint) {
        return "ws://localhost:" + server.getRunningHttpPort().getAsInt() + endpoint;
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
    public void testProfileNoRepo() {
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
        assertTrue(html.contains("<li>Count: 5</li>"));
    }
}