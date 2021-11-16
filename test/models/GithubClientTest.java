package models;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.*;
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
    
    /**
     * Test case for getIssues() method
     * @author Meet Mehta
     * 
     * @throws Exception
     */
    
    @Test
    public void testGetIssues() throws Exception {
    	WSClient client = mock(WSClient.class);
    	WSRequest request = mock(WSRequest.class);
    	WSResponse response = mock(WSResponse.class);
    	
    	when(client.url("https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement/issues")).thenReturn(request);
        when(request.addHeader(eq("Authorization"), anyString())).thenReturn(request);
        when(request.addHeader("Accept", "application/vnd.github.v3+json")).thenReturn(request);
        when(request.get()).thenReturn(CompletableFuture.completedFuture(response));
        String responseString = "[\n"
        		+ "    {\n"
        		+ "        \"url\": \"https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement/issues/2\",\n"
        		+ "        \"repository_url\": \"https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement\",\n"
        		+ "        \"labels_url\": \"https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement/issues/2/labels{/name}\",\n"
        		+ "        \"comments_url\": \"https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement/issues/2/comments\",\n"
        		+ "        \"events_url\": \"https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement/issues/2/events\",\n"
        		+ "        \"html_url\": \"https://github.com/meetmehta1198/StudentAttendanceManagement/issues/2\",\n"
        		+ "        \"id\": 933291341,\n"
        		+ "        \"node_id\": \"MDU6SXNzdWU5MzMyOTEzNDE=\",\n"
        		+ "        \"number\": 2,\n"
        		+ "        \"title\": \"Help for running this project \",\n"
        		+ "        \"user\": {\n"
        		+ "            \"login\": \"imkhaled404\",\n"
        		+ "            \"id\": 31078651,\n"
        		+ "            \"node_id\": \"MDQ6VXNlcjMxMDc4NjUx\",\n"
        		+ "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/31078651?v=4\",\n"
        		+ "            \"gravatar_id\": \"\",\n"
        		+ "            \"url\": \"https://api.github.com/users/imkhaled404\",\n"
        		+ "            \"html_url\": \"https://github.com/imkhaled404\",\n"
        		+ "            \"followers_url\": \"https://api.github.com/users/imkhaled404/followers\",\n"
        		+ "            \"following_url\": \"https://api.github.com/users/imkhaled404/following{/other_user}\",\n"
        		+ "            \"gists_url\": \"https://api.github.com/users/imkhaled404/gists{/gist_id}\",\n"
        		+ "            \"starred_url\": \"https://api.github.com/users/imkhaled404/starred{/owner}{/repo}\",\n"
        		+ "            \"subscriptions_url\": \"https://api.github.com/users/imkhaled404/subscriptions\",\n"
        		+ "            \"organizations_url\": \"https://api.github.com/users/imkhaled404/orgs\",\n"
        		+ "            \"repos_url\": \"https://api.github.com/users/imkhaled404/repos\",\n"
        		+ "            \"events_url\": \"https://api.github.com/users/imkhaled404/events{/privacy}\",\n"
        		+ "            \"received_events_url\": \"https://api.github.com/users/imkhaled404/received_events\",\n"
        		+ "            \"type\": \"User\",\n"
        		+ "            \"site_admin\": false\n"
        		+ "        },\n"
        		+ "        \"labels\": [],\n"
        		+ "        \"state\": \"open\",\n"
        		+ "        \"locked\": false,\n"
        		+ "        \"assignee\": null,\n"
        		+ "        \"assignees\": [],\n"
        		+ "        \"milestone\": null,\n"
        		+ "        \"comments\": 0,\n"
        		+ "        \"created_at\": \"2021-06-30T04:21:35Z\",\n"
        		+ "        \"updated_at\": \"2021-06-30T04:21:35Z\",\n"
        		+ "        \"closed_at\": null,\n"
        		+ "        \"author_association\": \"NONE\",\n"
        		+ "        \"active_lock_reason\": null,\n"
        		+ "        \"body\": \"How to run this project ? any tutorial .\\r\\n\\r\\nits totally working but but I don't know   how to connect database this project.\",\n"
        		+ "        \"reactions\": {\n"
        		+ "            \"url\": \"https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement/issues/2/reactions\",\n"
        		+ "            \"total_count\": 0,\n"
        		+ "            \"+1\": 0,\n"
        		+ "            \"-1\": 0,\n"
        		+ "            \"laugh\": 0,\n"
        		+ "            \"hooray\": 0,\n"
        		+ "            \"confused\": 0,\n"
        		+ "            \"heart\": 0,\n"
        		+ "            \"rocket\": 0,\n"
        		+ "            \"eyes\": 0\n"
        		+ "        },\n"
        		+ "        \"timeline_url\": \"https://api.github.com/repos/meetmehta1198/StudentAttendanceManagement/issues/2/timeline\",\n"
        		+ "        \"performed_via_github_app\": null\n"
        		+ "    }\n"
        		+ "]";
        
        when(response.asJson()).thenReturn(Json.parse(responseString));
        GithubClient github = new GithubClient(client, ConfigFactory.load());
        List<Issue> actual = github.getIssues("meetmehta1198", "StudentAttendanceManagement").toCompletableFuture().get();
        assertEquals("Help for running this project ",actual.get(0).getTitle());

        assertEquals("Help for running this project ",actual.get(0).getTitle());
    }
}