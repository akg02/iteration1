package models;

import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.mockito.Mockito;
import play.cache.AsyncCacheApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Class GithubClientTest
 * @author Hop Nguyen
 */
public class GithubClientTest {

    /**
     * @author Hop Nguyen
     * @param cachedValue
     * @return a mocked cache api for testing
     */
    private AsyncCacheApi mockCache(Object cachedValue) {
        AsyncCacheApi cache = mock(AsyncCacheApi.class);
        when(cache.getOrElseUpdate(anyString(), any())).thenAnswer(params -> {
            if (cachedValue != null) {
                return cachedValue;
            }
            final Callable<?> provider = params.getArgument(1);
            return provider.call();
        });
        when(cache.getOrElseUpdate(anyString(), any(), anyInt())).thenAnswer(params -> {
            if (cachedValue != null) {
                return cachedValue;
            }
            final Callable<?> provider = params.getArgument(1);
            return provider.call();
        });
        return cache;
    }
    /**
     * This is to test the repositories search
     * @author Hop Nguyen
     */
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
        GithubClient github = new GithubClient(client, mockCache(null), ConfigFactory.load());
        CompletionStage<SearchResult> future = github.searchRepositories("reactive", false);
        SearchResult searchResult = future.toCompletableFuture().get();
        assertEquals("reactive", searchResult.getInput());
        assertEquals(1, searchResult.getRepositories().size());
        assertEquals("ReactiveX", searchResult.getRepositories().get(0).getUser());
        assertEquals("RxJava", searchResult.getRepositories().get(0).getName());
        assertEquals(Arrays.asList("flow", "java"), searchResult.getRepositories().get(0).getTopics());
        Mockito.verify(request).addHeader("Accept", "application/vnd.github.v3+json");
        Mockito.verify(request).addQueryParameter("q", "reactive");
        Mockito.verify(request).addQueryParameter("per_page", "10");
        Mockito.verify(request).addQueryParameter("sort", "updated");
    }

    /**
     * This is to test the search for topics
     * @author Hop Nguyen
     */
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
        GithubClient github = new GithubClient(client, mockCache(null), ConfigFactory.load());
        CompletionStage<SearchResult> future = github.searchRepositories("reactive", true);
        SearchResult searchResult = future.toCompletableFuture().get();
        assertEquals("reactive", searchResult.getInput());
        assertEquals(1, searchResult.getRepositories().size());
        assertEquals("ReactiveX", searchResult.getRepositories().get(0).getUser());
        assertEquals("RxJava", searchResult.getRepositories().get(0).getName());
        assertEquals(Arrays.asList("flow", "java"), searchResult.getRepositories().get(0).getTopics());
        Mockito.verify(request).addHeader("Accept", "application/vnd.github.v3+json");
        Mockito.verify(request).addQueryParameter("q", "topic:reactive");
        Mockito.verify(request).addQueryParameter("per_page", "10");
        Mockito.verify(request).addQueryParameter("sort", "updated");
    }

    /**
     * Test if the cache return already search result
     * @author Hop Nguyen
     */
    @Test
    public void testCacheSearchResultFromGithub() {
        WSClient client = mock(WSClient.class);
        SearchResult result = new SearchResult();
        CompletableFuture<SearchResult> mockResult = CompletableFuture.completedFuture(result);
        AsyncCacheApi cache = mockCache(mockResult);
        GithubClient github = new GithubClient(client, cache, ConfigFactory.load());
        CompletionStage<SearchResult> searchByTerm = github.searchRepositories("reactive", false);
        assertEquals(mockResult, searchByTerm);
        // Do not access Github
        verifyNoInteractions(client);
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
        when(request.addHeader("sort", "created")).thenReturn(request);
        when(request.addHeader("direction", "desc")).thenReturn(request);
        when(request.addHeader("state", "all")).thenReturn(request);
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
        GithubClient github = new GithubClient(client, mockCache(null), ConfigFactory.load());
        List<Issue> actual = github.getIssues("meetmehta1198", "StudentAttendanceManagement").toCompletableFuture().get();
        assertEquals("Help for running this project ",actual.get(0).getTitle());

        assertEquals("Help for running this project ",actual.get(0).getTitle());
    }

    /**
     * Test case for getCommitStatByID() method
     * @author Smit Parmar
     *
     * @throws Exception
     */

    @Test
    public void testGetCommitStatByID() throws Exception {
        WSClient client = mock(WSClient.class);
        WSRequest request = mock(WSRequest.class);
        WSResponse response = mock(WSResponse.class);

        when(client.url("https://api.github.com/repos/smituparmar/MedicoGraph/commits/4477971350127c4edbca5f8acf439ce96fbca93e")).thenReturn(request);
        when(request.addHeader("Authorization", "token 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")).thenReturn(request);
        when(request.addHeader("Accept", "application/vnd.github.v3+json")).thenReturn(request);
        when(request.get()).thenReturn(CompletableFuture.completedFuture(response));

        String responseString = "{\n" +
                "    \"sha\": \"4477971350127c4edbca5f8acf439ce96fbca93e\",\n" +
                "    \"node_id\": \"C_kwDOGU0OHNoAKDQ0Nzc5NzEzNTAxMjdjNGVkYmNhNWY4YWNmNDM5Y2U5NmZiY2E5M2U\",\n" +
                "    \"commit\": {\n" +
                "        \"author\": {\n" +
                "            \"name\": \"smituparmar\",\n" +
                "            \"email\": \"workforsmit@gmail.com\",\n" +
                "            \"date\": \"2021-11-07T12:18:30Z\"\n" +
                "        },\n" +
                "        \"committer\": {\n" +
                "            \"name\": \"smituparmar\",\n" +
                "            \"email\": \"workforsmit@gmail.com\",\n" +
                "            \"date\": \"2021-11-07T12:18:30Z\"\n" +
                "        },\n" +
                "        \"message\": \"bug solving\",\n" +
                "        \"tree\": {\n" +
                "            \"sha\": \"6907d40195379d6be75271640c533a6de6513a96\",\n" +
                "            \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph/git/trees/6907d40195379d6be75271640c533a6de6513a96\"\n" +
                "        },\n" +
                "        \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph/git/commits/4477971350127c4edbca5f8acf439ce96fbca93e\",\n" +
                "        \"comment_count\": 0,\n" +
                "        \"verification\": {\n" +
                "            \"verified\": false,\n" +
                "            \"reason\": \"unsigned\",\n" +
                "            \"signature\": null,\n" +
                "            \"payload\": null\n" +
                "        }\n" +
                "    },\n" +
                "    \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph/commits/4477971350127c4edbca5f8acf439ce96fbca93e\",\n" +
                "    \"html_url\": \"https://github.com/smituparmar/MedicoGraph/commit/4477971350127c4edbca5f8acf439ce96fbca93e\",\n" +
                "    \"comments_url\": \"https://api.github.com/repos/smituparmar/MedicoGraph/commits/4477971350127c4edbca5f8acf439ce96fbca93e/comments\",\n" +
                "    \"author\": {\n" +
                "        \"login\": \"smituparmar\",\n" +
                "        \"id\": 30971669,\n" +
                "        \"node_id\": \"MDQ6VXNlcjMwOTcxNjY5\",\n" +
                "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/30971669?v=4\",\n" +
                "        \"gravatar_id\": \"\",\n" +
                "        \"url\": \"https://api.github.com/users/smituparmar\",\n" +
                "        \"html_url\": \"https://github.com/smituparmar\",\n" +
                "        \"followers_url\": \"https://api.github.com/users/smituparmar/followers\",\n" +
                "        \"following_url\": \"https://api.github.com/users/smituparmar/following{/other_user}\",\n" +
                "        \"gists_url\": \"https://api.github.com/users/smituparmar/gists{/gist_id}\",\n" +
                "        \"starred_url\": \"https://api.github.com/users/smituparmar/starred{/owner}{/repo}\",\n" +
                "        \"subscriptions_url\": \"https://api.github.com/users/smituparmar/subscriptions\",\n" +
                "        \"organizations_url\": \"https://api.github.com/users/smituparmar/orgs\",\n" +
                "        \"repos_url\": \"https://api.github.com/users/smituparmar/repos\",\n" +
                "        \"events_url\": \"https://api.github.com/users/smituparmar/events{/privacy}\",\n" +
                "        \"received_events_url\": \"https://api.github.com/users/smituparmar/received_events\",\n" +
                "        \"type\": \"User\",\n" +
                "        \"site_admin\": false\n" +
                "    },\n" +
                "    \"committer\": {\n" +
                "        \"login\": \"smituparmar\",\n" +
                "        \"id\": 30971669,\n" +
                "        \"node_id\": \"MDQ6VXNlcjMwOTcxNjY5\",\n" +
                "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/30971669?v=4\",\n" +
                "        \"gravatar_id\": \"\",\n" +
                "        \"url\": \"https://api.github.com/users/smituparmar\",\n" +
                "        \"html_url\": \"https://github.com/smituparmar\",\n" +
                "        \"followers_url\": \"https://api.github.com/users/smituparmar/followers\",\n" +
                "        \"following_url\": \"https://api.github.com/users/smituparmar/following{/other_user}\",\n" +
                "        \"gists_url\": \"https://api.github.com/users/smituparmar/gists{/gist_id}\",\n" +
                "        \"starred_url\": \"https://api.github.com/users/smituparmar/starred{/owner}{/repo}\",\n" +
                "        \"subscriptions_url\": \"https://api.github.com/users/smituparmar/subscriptions\",\n" +
                "        \"organizations_url\": \"https://api.github.com/users/smituparmar/orgs\",\n" +
                "        \"repos_url\": \"https://api.github.com/users/smituparmar/repos\",\n" +
                "        \"events_url\": \"https://api.github.com/users/smituparmar/events{/privacy}\",\n" +
                "        \"received_events_url\": \"https://api.github.com/users/smituparmar/received_events\",\n" +
                "        \"type\": \"User\",\n" +
                "        \"site_admin\": false\n" +
                "    },\n" +
                "    \"parents\": [\n" +
                "        {\n" +
                "            \"sha\": \"863523799dfedd15e52af44a037b0cdfaba882ff\",\n" +
                "            \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph/commits/863523799dfedd15e52af44a037b0cdfaba882ff\",\n" +
                "            \"html_url\": \"https://github.com/smituparmar/MedicoGraph/commit/863523799dfedd15e52af44a037b0cdfaba882ff\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"stats\": {\n" +
                "        \"total\": 10,\n" +
                "        \"additions\": 5,\n" +
                "        \"deletions\": 5\n" +
                "    },\n" +
                "    \"files\": [\n" +
                "        {\n" +
                "            \"sha\": \"3f9fd1fae4caf7d20227b915bcab4a5d9d62730b\",\n" +
                "            \"filename\": \"routes/api/auth.js\",\n" +
                "            \"status\": \"modified\",\n" +
                "            \"additions\": 1,\n" +
                "            \"deletions\": 1,\n" +
                "            \"changes\": 2,\n" +
                "            \"blob_url\": \"https://github.com/smituparmar/MedicoGraph/blob/4477971350127c4edbca5f8acf439ce96fbca93e/routes/api/auth.js\",\n" +
                "            \"raw_url\": \"https://github.com/smituparmar/MedicoGraph/raw/4477971350127c4edbca5f8acf439ce96fbca93e/routes/api/auth.js\",\n" +
                "            \"contents_url\": \"https://api.github.com/repos/smituparmar/MedicoGraph/contents/routes/api/auth.js?ref=4477971350127c4edbca5f8acf439ce96fbca93e\",\n" +
                "            \"patch\": \"@@ -212,7 +212,7 @@ router.put('/update', auth\\n                 country: country ? country : req.user.country,\\n             },\\n             avatar: avatar ? avatar : req.user.avatar,\\n-            newUser: newUser ? newUser : req.user.newUser\\n+            newUser: newUser!=null ? newUser : req.user.newUser\\n         }\\n \\n         await User.findByIdAndUpdate(req.user._id,updateUserBody);\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"sha\": \"59bb56375fa164b783b072d48470e9d193736726\",\n" +
                "            \"filename\": \"routes/api/patientMedical.js\",\n" +
                "            \"status\": \"modified\",\n" +
                "            \"additions\": 4,\n" +
                "            \"deletions\": 4,\n" +
                "            \"changes\": 8,\n" +
                "            \"blob_url\": \"https://github.com/smituparmar/MedicoGraph/blob/4477971350127c4edbca5f8acf439ce96fbca93e/routes/api/patientMedical.js\",\n" +
                "            \"raw_url\": \"https://github.com/smituparmar/MedicoGraph/raw/4477971350127c4edbca5f8acf439ce96fbca93e/routes/api/patientMedical.js\",\n" +
                "            \"contents_url\": \"https://api.github.com/repos/smituparmar/MedicoGraph/contents/routes/api/patientMedical.js?ref=4477971350127c4edbca5f8acf439ce96fbca93e\",\n" +
                "            \"patch\": \"@@ -155,10 +155,10 @@ router.put('/update',[\\n                 bloodGroup: bloodGroup ? bloodGroup : patientmedical.bloodGroup,\\n                 height: height ? height : patientmedical.height,\\n                 weight: weight ? weight : patientmedical.weight, \\n-                hasDiabetes: hasDiabetes ? hasDiabetes : patientmedical.hasDiabetes, \\n-                hasHeartDisease: hasHeartDisease ? hasHeartDisease : patientmedical.hasHeartDisease,\\n-                hasArthirtis: hasArthirtis ? hasArthirtis : patientmedical.hasArthirtis,\\n-                hasBloodPressureProblem: hasBloodPressureProblem ? hasBloodPressureProblem : patientmedical.hasBloodPressureProblem,\\n+                hasDiabetes: hasDiabetes!=null ? hasDiabetes : patientmedical.hasDiabetes, \\n+                hasHeartDisease: hasHeartDisease!=null ? hasHeartDisease : patientmedical.hasHeartDisease,\\n+                hasArthirtis: hasArthirtis!=null ? hasArthirtis : patientmedical.hasArthirtis,\\n+                hasBloodPressureProblem: hasBloodPressureProblem!=null ? hasBloodPressureProblem : patientmedical.hasBloodPressureProblem,\\n                 user: req.user.id\\n             };\\n \"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        when(response.asJson()).thenReturn(Json.parse(responseString));
        GithubClient github = new GithubClient(client, mockCache(null), ConfigFactory.load());
        CommitStats actual = github.getCommitStatByID("smituparmar", "MedicoGraph", "4477971350127c4edbca5f8acf439ce96fbca93e").get();
        assertEquals("smituparmar",actual.getName());
        assertEquals(5,actual.getAddition());
    }

    /**
     * test for getAllCommitList
     * @author Smit Parar
     * @throws Exception
     */
    @Test
    public void testGetAllCommitList() throws Exception {
        WSClient client = mock(WSClient.class);
        WSRequest request = mock(WSRequest.class);
        WSResponse response = mock(WSResponse.class);

        when(client.url("https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/commits")).thenReturn(request);
        when(request.addHeader("Authorization", "token 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")).thenReturn(request);
        when(request.addHeader("Accept", "application/vnd.github.v3+json")).thenReturn(request);
        when(request.addQueryParameter("per_page", "100")).thenReturn(request);
        when(request.get()).thenReturn(CompletableFuture.completedFuture(response));

        String responseString = "[\n" +
                "  {\n" +
                "    \"sha\": \"f805f9a977f3e79a222ed45e3c2b036db5c34455\",\n" +
                "    \"node_id\": \"C_kwDOGVy0u9oAKGY4MDVmOWE5NzdmM2U3OWEyMjJlZDQ1ZTNjMmIwMzZkYjVjMzQ0NTU\",\n" +
                "    \"commit\": {\n" +
                "      \"author\": {\n" +
                "        \"name\": \"smituparmar\",\n" +
                "        \"email\": \"workforsmit@gmail.com\",\n" +
                "        \"date\": \"2021-11-07T13:09:18Z\"\n" +
                "      },\n" +
                "      \"committer\": {\n" +
                "        \"name\": \"smituparmar\",\n" +
                "        \"email\": \"workforsmit@gmail.com\",\n" +
                "        \"date\": \"2021-11-07T13:09:18Z\"\n" +
                "      },\n" +
                "      \"message\": \"New code push\",\n" +
                "      \"tree\": {\n" +
                "        \"sha\": \"4fef8f03068cfd7f2506f1c1077f7823ea681962\",\n" +
                "        \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/git/trees/4fef8f03068cfd7f2506f1c1077f7823ea681962\"\n" +
                "      },\n" +
                "      \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/git/commits/f805f9a977f3e79a222ed45e3c2b036db5c34455\",\n" +
                "      \"comment_count\": 0,\n" +
                "      \"verification\": {\n" +
                "        \"verified\": false,\n" +
                "        \"reason\": \"unsigned\",\n" +
                "        \"signature\": null,\n" +
                "        \"payload\": null\n" +
                "      }\n" +
                "    },\n" +
                "    \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/commits/f805f9a977f3e79a222ed45e3c2b036db5c34455\",\n" +
                "    \"html_url\": \"https://github.com/smituparmar/MedicoGraph-Frontend/commit/f805f9a977f3e79a222ed45e3c2b036db5c34455\",\n" +
                "    \"comments_url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/commits/f805f9a977f3e79a222ed45e3c2b036db5c34455/comments\",\n" +
                "    \"author\": {\n" +
                "      \"login\": \"smituparmar\",\n" +
                "      \"id\": 30971669,\n" +
                "      \"node_id\": \"MDQ6VXNlcjMwOTcxNjY5\",\n" +
                "      \"avatar_url\": \"https://avatars.githubusercontent.com/u/30971669?v=4\",\n" +
                "      \"gravatar_id\": \"\",\n" +
                "      \"url\": \"https://api.github.com/users/smituparmar\",\n" +
                "      \"html_url\": \"https://github.com/smituparmar\",\n" +
                "      \"followers_url\": \"https://api.github.com/users/smituparmar/followers\",\n" +
                "      \"following_url\": \"https://api.github.com/users/smituparmar/following{/other_user}\",\n" +
                "      \"gists_url\": \"https://api.github.com/users/smituparmar/gists{/gist_id}\",\n" +
                "      \"starred_url\": \"https://api.github.com/users/smituparmar/starred{/owner}{/repo}\",\n" +
                "      \"subscriptions_url\": \"https://api.github.com/users/smituparmar/subscriptions\",\n" +
                "      \"organizations_url\": \"https://api.github.com/users/smituparmar/orgs\",\n" +
                "      \"repos_url\": \"https://api.github.com/users/smituparmar/repos\",\n" +
                "      \"events_url\": \"https://api.github.com/users/smituparmar/events{/privacy}\",\n" +
                "      \"received_events_url\": \"https://api.github.com/users/smituparmar/received_events\",\n" +
                "      \"type\": \"User\",\n" +
                "      \"site_admin\": false\n" +
                "    },\n" +
                "    \"committer\": {\n" +
                "      \"login\": \"smituparmar\",\n" +
                "      \"id\": 30971669,\n" +
                "      \"node_id\": \"MDQ6VXNlcjMwOTcxNjY5\",\n" +
                "      \"avatar_url\": \"https://avatars.githubusercontent.com/u/30971669?v=4\",\n" +
                "      \"gravatar_id\": \"\",\n" +
                "      \"url\": \"https://api.github.com/users/smituparmar\",\n" +
                "      \"html_url\": \"https://github.com/smituparmar\",\n" +
                "      \"followers_url\": \"https://api.github.com/users/smituparmar/followers\",\n" +
                "      \"following_url\": \"https://api.github.com/users/smituparmar/following{/other_user}\",\n" +
                "      \"gists_url\": \"https://api.github.com/users/smituparmar/gists{/gist_id}\",\n" +
                "      \"starred_url\": \"https://api.github.com/users/smituparmar/starred{/owner}{/repo}\",\n" +
                "      \"subscriptions_url\": \"https://api.github.com/users/smituparmar/subscriptions\",\n" +
                "      \"organizations_url\": \"https://api.github.com/users/smituparmar/orgs\",\n" +
                "      \"repos_url\": \"https://api.github.com/users/smituparmar/repos\",\n" +
                "      \"events_url\": \"https://api.github.com/users/smituparmar/events{/privacy}\",\n" +
                "      \"received_events_url\": \"https://api.github.com/users/smituparmar/received_events\",\n" +
                "      \"type\": \"User\",\n" +
                "      \"site_admin\": false\n" +
                "    },\n" +
                "    \"parents\": [\n" +
                "      {\n" +
                "        \"sha\": \"bf92ff59292801edefc641a96a13950b7c83bf7f\",\n" +
                "        \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/commits/bf92ff59292801edefc641a96a13950b7c83bf7f\",\n" +
                "        \"html_url\": \"https://github.com/smituparmar/MedicoGraph-Frontend/commit/bf92ff59292801edefc641a96a13950b7c83bf7f\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"sha\": \"bf92ff59292801edefc641a96a13950b7c83bf7f\",\n" +
                "    \"node_id\": \"C_kwDOGVy0u9oAKGJmOTJmZjU5MjkyODAxZWRlZmM2NDFhOTZhMTM5NTBiN2M4M2JmN2Y\",\n" +
                "    \"commit\": {\n" +
                "      \"author\": {\n" +
                "        \"name\": \"smituparmar\",\n" +
                "        \"email\": \"workforsmit@gmail.com\",\n" +
                "        \"date\": \"2021-11-06T20:13:00Z\"\n" +
                "      },\n" +
                "      \"committer\": {\n" +
                "        \"name\": \"smituparmar\",\n" +
                "        \"email\": \"workforsmit@gmail.com\",\n" +
                "        \"date\": \"2021-11-06T20:13:00Z\"\n" +
                "      },\n" +
                "      \"message\": \"Initialize project using Create React App\",\n" +
                "      \"tree\": {\n" +
                "        \"sha\": \"8106adbcbce7d92b833ff45be7d5c6c47069a0e8\",\n" +
                "        \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/git/trees/8106adbcbce7d92b833ff45be7d5c6c47069a0e8\"\n" +
                "      },\n" +
                "      \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/git/commits/bf92ff59292801edefc641a96a13950b7c83bf7f\",\n" +
                "      \"comment_count\": 0,\n" +
                "      \"verification\": {\n" +
                "        \"verified\": false,\n" +
                "        \"reason\": \"unsigned\",\n" +
                "        \"signature\": null,\n" +
                "        \"payload\": null\n" +
                "      }\n" +
                "    },\n" +
                "    \"url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/commits/bf92ff59292801edefc641a96a13950b7c83bf7f\",\n" +
                "    \"html_url\": \"https://github.com/smituparmar/MedicoGraph-Frontend/commit/bf92ff59292801edefc641a96a13950b7c83bf7f\",\n" +
                "    \"comments_url\": \"https://api.github.com/repos/smituparmar/MedicoGraph-Frontend/commits/bf92ff59292801edefc641a96a13950b7c83bf7f/comments\",\n" +
                "    \"author\": {\n" +
                "      \"login\": \"smituparmar\",\n" +
                "      \"id\": 30971669,\n" +
                "      \"node_id\": \"MDQ6VXNlcjMwOTcxNjY5\",\n" +
                "      \"avatar_url\": \"https://avatars.githubusercontent.com/u/30971669?v=4\",\n" +
                "      \"gravatar_id\": \"\",\n" +
                "      \"url\": \"https://api.github.com/users/smituparmar\",\n" +
                "      \"html_url\": \"https://github.com/smituparmar\",\n" +
                "      \"followers_url\": \"https://api.github.com/users/smituparmar/followers\",\n" +
                "      \"following_url\": \"https://api.github.com/users/smituparmar/following{/other_user}\",\n" +
                "      \"gists_url\": \"https://api.github.com/users/smituparmar/gists{/gist_id}\",\n" +
                "      \"starred_url\": \"https://api.github.com/users/smituparmar/starred{/owner}{/repo}\",\n" +
                "      \"subscriptions_url\": \"https://api.github.com/users/smituparmar/subscriptions\",\n" +
                "      \"organizations_url\": \"https://api.github.com/users/smituparmar/orgs\",\n" +
                "      \"repos_url\": \"https://api.github.com/users/smituparmar/repos\",\n" +
                "      \"events_url\": \"https://api.github.com/users/smituparmar/events{/privacy}\",\n" +
                "      \"received_events_url\": \"https://api.github.com/users/smituparmar/received_events\",\n" +
                "      \"type\": \"User\",\n" +
                "      \"site_admin\": false\n" +
                "    },\n" +
                "    \"committer\": {\n" +
                "      \"login\": \"smituparmar\",\n" +
                "      \"id\": 30971669,\n" +
                "      \"node_id\": \"MDQ6VXNlcjMwOTcxNjY5\",\n" +
                "      \"avatar_url\": \"https://avatars.githubusercontent.com/u/30971669?v=4\",\n" +
                "      \"gravatar_id\": \"\",\n" +
                "      \"url\": \"https://api.github.com/users/smituparmar\",\n" +
                "      \"html_url\": \"https://github.com/smituparmar\",\n" +
                "      \"followers_url\": \"https://api.github.com/users/smituparmar/followers\",\n" +
                "      \"following_url\": \"https://api.github.com/users/smituparmar/following{/other_user}\",\n" +
                "      \"gists_url\": \"https://api.github.com/users/smituparmar/gists{/gist_id}\",\n" +
                "      \"starred_url\": \"https://api.github.com/users/smituparmar/starred{/owner}{/repo}\",\n" +
                "      \"subscriptions_url\": \"https://api.github.com/users/smituparmar/subscriptions\",\n" +
                "      \"organizations_url\": \"https://api.github.com/users/smituparmar/orgs\",\n" +
                "      \"repos_url\": \"https://api.github.com/users/smituparmar/repos\",\n" +
                "      \"events_url\": \"https://api.github.com/users/smituparmar/events{/privacy}\",\n" +
                "      \"received_events_url\": \"https://api.github.com/users/smituparmar/received_events\",\n" +
                "      \"type\": \"User\",\n" +
                "      \"site_admin\": false\n" +
                "    },\n" +
                "    \"parents\": [\n" +
                "\n" +
                "    ]\n" +
                "  }\n" +
                "]";

        when(response.asJson()).thenReturn(Json.parse(responseString));
        GithubClient github = new GithubClient(client, mockCache(null), ConfigFactory.load());
        List<String> actual = github.getAllCommitList("smituparmar", "MedicoGraph-Frontend", 100);
        assertEquals("f805f9a977f3e79a222ed45e3c2b036db5c34455",actual.get(0));
    }


    /**
     * test case for getRepositoryDetails
     * @author Sagar Sanghani
     */
    @Test
    public void testGetRepositoryDetails() throws Exception {
        WSClient client = mock(WSClient.class);
        WSRequest request = mock(WSRequest.class);
        WSResponse response = mock(WSResponse.class);
        List<Issue> issueList = new ArrayList<>();


        when(client.url("https://api.github.com/repos/Sagar7421/dinosaur-name-generation-rnn")).thenReturn(request);
        when(request.addHeader("Accept", "application/vnd.github.v3+json")).thenReturn(request);
        when(request.get()).thenReturn(CompletableFuture.completedFuture(response));
        String responseString = "{ " +
                " \"name\": \"dinosaur-name-generation-rnn\"," +
                " \"description\": \"A dinosaur name generation using RNN in NumPy.\","+
                " \"created_at\": \"2020-10-17T10:10:38Z\"," +
                " \"updated_at\": \"2021-11-20T16:52:33Z\"," +
                " \"stargazers_count\":" + 1 + "," +
                " \"forks_count\":" + 0 + "," +
                " \"topics\": [\"neural-network\"] }";

        when(response.asJson()).thenReturn(Json.parse(responseString));
        GithubClient github = new GithubClient(client, mockCache(null), ConfigFactory.load());
        CompletionStage<RepositoryProfile> future = github.getRepositoryDetails("Sagar7421", "dinosaur-name-generation-rnn", issueList);
        RepositoryProfile repositoryProfile = future.toCompletableFuture().get();
        assertEquals("dinosaur-name-generation-rnn", repositoryProfile.getName());
        assertEquals("A dinosaur name generation using RNN in NumPy.", repositoryProfile.getDescription());
        assertEquals("Sat Oct 17 06:10:38 EDT 2020", repositoryProfile.getCreated_at().toString());
        assertEquals("Sat Nov 20 11:52:33 EST 2021", repositoryProfile.getUpdated_at().toString());
        assertEquals(1, repositoryProfile.getStargazers_count());
        assertEquals(0, repositoryProfile.getForks_count());
        assertEquals(0, repositoryProfile.getIssues().size());
        assertEquals("neural-network", repositoryProfile.getTopics().get(0));
        Mockito.verify(request).addHeader("Accept", "application/vnd.github.v3+json");
    }
    
    /**
     * test case for displayUserProfile
     * @author Joon Seung Hwang
     */
    @Test
    public void testDisplayUserProfile() throws Exception {
    	 WSClient client = mock(WSClient.class);
         WSRequest request = mock(WSRequest.class);
         WSResponse response = mock(WSResponse.class);
         List<String> repoList = new ArrayList<>();
         
         when(client.url("https://api.github.com/users/mayjoonjuly")).thenReturn(request);
         when(request.addHeader("Accept", "application/vnd.github.v3+json")).thenReturn(request);
         when(request.addHeader("Authorization", "token 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")).thenReturn(request);
         when(request.get()).thenReturn(CompletableFuture.completedFuture(response));
         String responseString = "{\n"
         		+ "  \"login\": \"mayjoonjuly\",\n"
         		+ "  \"id\": 73373615,\n"
         		+ "  \"node_id\": \"MDQ6VXNlcjczMzczNjE1\",\n"
         		+ "  \"avatar_url\": \"https://avatars.githubusercontent.com/u/73373615?v=4\",\n"
         		+ "  \"gravatar_id\": \"\",\n"
         		+ "  \"url\": \"https://api.github.com/users/mayjoonjuly\",\n"
         		+ "  \"html_url\": \"https://github.com/mayjoonjuly\",\n"
         		+ "  \"followers_url\": \"https://api.github.com/users/mayjoonjuly/followers\",\n"
         		+ "  \"following_url\": \"https://api.github.com/users/mayjoonjuly/following{/other_user}\",\n"
         		+ "  \"gists_url\": \"https://api.github.com/users/mayjoonjuly/gists{/gist_id}\",\n"
         		+ "  \"starred_url\": \"https://api.github.com/users/mayjoonjuly/starred{/owner}{/repo}\",\n"
         		+ "  \"subscriptions_url\": \"https://api.github.com/users/mayjoonjuly/subscriptions\",\n"
         		+ "  \"organizations_url\": \"https://api.github.com/users/mayjoonjuly/orgs\",\n"
         		+ "  \"repos_url\": \"https://api.github.com/users/mayjoonjuly/repos\",\n"
         		+ "  \"events_url\": \"https://api.github.com/users/mayjoonjuly/events{/privacy}\",\n"
         		+ "  \"received_events_url\": \"https://api.github.com/users/mayjoonjuly/received_events\",\n"
         		+ "  \"type\": \"User\",\n"
         		+ "  \"site_admin\": false,\n"
         		+ "  \"name\": \"Joon Seung\",\n"
         		+ "  \"company\": \"abc\",\n"
         		+ "  \"blog\": \"www\",\n"
         		+ "  \"location\": \"montreal\",\n"
         		+ "  \"email\": null,\n"
         		+ "  \"hireable\": null,\n"
         		+ "  \"bio\": \"Testing\",\n"
         		+ "  \"twitter_username\": \"123\",\n"
         		+ "  \"public_repos\": 3,\n"
         		+ "  \"public_gists\": 0,\n"
         		+ "  \"followers\": 0,\n"
         		+ "  \"following\": 1,\n"
         		+ "  \"created_at\": \"2020-10-24T02:39:52Z\",\n"
         		+ "  \"updated_at\": \"2021-11-20T22:16:24Z\"\n"
         		+ "}";
         
         when(response.asJson()).thenReturn(Json.parse(responseString));
         GithubClient github = new GithubClient(client, mockCache(null), ConfigFactory.load());
         CompletionStage<ProfileInfo> future = github.displayUserProfile("mayjoonjuly",repoList);
         ProfileInfo profileInfo = future.toCompletableFuture().get();
         assertEquals("mayjoonjuly", profileInfo.getLogin());
         assertEquals("Joon Seung", profileInfo.getName());
         assertEquals("Testing", profileInfo.getBio());
         assertEquals("abc", profileInfo.getCompany());
         assertEquals("www", profileInfo.getBlog());
         assertEquals("montreal", profileInfo.getLocation());
         assertEquals(null, profileInfo.getEmail());
         assertEquals("123", profileInfo.getTwitter());
         assertEquals(0, profileInfo.getFollowers());
         assertEquals(1, profileInfo.getFollowing());
         assertEquals("Desta25", profileInfo.getRepos.get(1));
         Mockito.verify(request).addHeader("Accept", "application/vnd.github.v3+json");
               
    }

}