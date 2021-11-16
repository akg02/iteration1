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

import static org.junit.Assert.*;
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
        GithubClient github = new GithubClient(client, ConfigFactory.load());
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
        GithubClient github = new GithubClient(client, ConfigFactory.load());
        List<String> actual = github.getAllCommitList("smituparmar", "MedicoGraph-Frontend", 100);
        assertEquals("f805f9a977f3e79a222ed45e3c2b036db5c34455",actual.get(0));
    }

}