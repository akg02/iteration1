package models;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubClientTest {

    @Test
    public void testSearchRepositories() throws Exception {
        WSClient client = mock(WSClient.class);
        WSRequest request = mock(WSRequest.class);
        WSResponse response = mock(WSResponse.class);

        when(client.url("https://api.github.com/search/repositories")).thenReturn(request);
        when(request.addHeader(eq("Authorization"), anyString())).thenReturn(request);
        when(request.addHeader("Accept", "application/vnd.github.v3+json")).thenReturn(request);
        when(request.addQueryParameter("q", "reactive")).thenReturn(request);
        when(request.addQueryParameter("per_page", "10")).thenReturn(request);
        when(request.addQueryParameter("sort", "updated")).thenReturn(request);
        when(request.get()).thenReturn(CompletableFuture.completedFuture(response));
        String responseString = "{\n" +
                "  \"items\": [" +
                "    {\n" +
                "      \"name\": \"RxJava\"," +
                "      \"owner\": {\n" +
                "        \"login\": \"ReactiveX\"" +
                "      },\n" +
                "      \"topics\": [" +
                "        \"flow\"," +
                "        \"java\"" +
                "      ]" +
                "    }" +
                "  ]}";
        when(response.asJson()).thenReturn(Json.parse(responseString));
        GithubClient github = new GithubClient(client, ConfigFactory.load());
        CompletionStage<SearchResult> future = github.searchRepositories("reactive", false);
        SearchResult searchResult = future.toCompletableFuture().get();
        assertEquals("reactive", searchResult.input);
        assertEquals(1, searchResult.repositories.size());
        assertEquals("ReactiveX", searchResult.repositories.get(0).user);
        assertEquals("RxJava", searchResult.repositories.get(0).name);
        assertEquals(Arrays.asList("flow", "java"), searchResult.repositories.get(0).topics);
        Mockito.verify(request).addHeader("Accept", "application/vnd.github.v3+json");
        Mockito.verify(request).addQueryParameter("q", "reactive");
        Mockito.verify(request).addQueryParameter("per_page", "10");
        Mockito.verify(request).addQueryParameter("sort", "updated");
    }

    @Test
    public void testSearchTopic() throws Exception {
        WSClient client = mock(WSClient.class);
        WSRequest request = mock(WSRequest.class);
        WSResponse response = mock(WSResponse.class);

        when(client.url("https://api.github.com/search/repositories")).thenReturn(request);
        when(request.addHeader(eq("Authorization"), anyString())).thenReturn(request);
        when(request.addHeader("Accept", "application/vnd.github.v3+json")).thenReturn(request);
        when(request.addQueryParameter("q", "topic:reactive")).thenReturn(request);
        when(request.addQueryParameter("per_page", "10")).thenReturn(request);
        when(request.addQueryParameter("sort", "updated")).thenReturn(request);
        when(request.get()).thenReturn(CompletableFuture.completedFuture(response));
        String responseString = "{\n" +
                "  \"items\": [" +
                "    {\n" +
                "      \"name\": \"RxJava\"," +
                "      \"owner\": {\n" +
                "        \"login\": \"ReactiveX\"" +
                "      },\n" +
                "      \"topics\": [" +
                "        \"flow\"," +
                "        \"java\"" +
                "      ]" +
                "    }" +
                "  ]}";
        when(response.asJson()).thenReturn(Json.parse(responseString));
        GithubClient github = new GithubClient(client, ConfigFactory.load());
        CompletionStage<SearchResult> future = github.searchRepositories("reactive", true);
        SearchResult searchResult = future.toCompletableFuture().get();
        assertEquals("reactive", searchResult.input);
        assertEquals(1, searchResult.repositories.size());
        assertEquals("ReactiveX", searchResult.repositories.get(0).user);
        assertEquals("RxJava", searchResult.repositories.get(0).name);
        assertEquals(Arrays.asList("flow", "java"), searchResult.repositories.get(0).topics);
        Mockito.verify(request).addHeader("Accept", "application/vnd.github.v3+json");
        Mockito.verify(request).addQueryParameter("q", "topic:reactive");
        Mockito.verify(request).addQueryParameter("per_page", "10");
        Mockito.verify(request).addQueryParameter("sort", "updated");
    }
}