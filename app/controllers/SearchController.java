package controllers;

import com.google.inject.Inject;
import models.GithubClient;
import models.IssueService;
import models.SearchHistory;
import models.SearchResult;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.CommitService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 */
public class SearchController extends Controller {
    public static final String SESSION_ID = "session_id";

    private final GithubClient github;
    private final Form<SearchForm> searchForm;
    private final MessagesApi messagesApi;
    private final SearchHistory searchHistory;
    private final IssueService issueService;

    private final CommitService commitService;

    @Inject
    public SearchController(GithubClient github, FormFactory formFactory, MessagesApi messagesApi) {
        this.github = github;
        this.searchForm = formFactory.form(SearchForm.class);
        this.messagesApi = messagesApi;
        this.searchHistory = new SearchHistory();
        this.issueService  = new IssueService(github);
        this.commitService = new CommitService(github);
    }

    /**
     * The homepage which displays the search history of the current session
     */
    public CompletionStage<Result> index(Http.Request request) {
        String sessionId = request.session().get(SESSION_ID).orElseGet(() -> UUID.randomUUID().toString());
        List<SearchResult> searchResults = searchHistory.getHistory(sessionId);
        return CompletableFuture.completedFuture(
                ok(views.html.index.render(searchResults, searchForm, request, messagesApi.preferred(request)))
                        .addingToSession(request, SESSION_ID, sessionId));
    }

    /**
     * An endpoint that performs a search and adds the result to the history for the current session
     */
    public CompletionStage<Result> search(Http.Request request) {
        Form<SearchForm> boundForm = searchForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(redirect(routes.SearchController.index()));
        } else {
            String searchInput = boundForm.get().getInput();
            String sessionId = request.session().get(SESSION_ID).orElseGet(() -> UUID.randomUUID().toString());
            return github.searchRepositories(searchInput, false)
                    .thenAccept(searchResult -> searchHistory.addToHistory(sessionId, searchResult))
                    .thenApply(nullResult -> redirect(routes.SearchController.index())
                            .addingToSession(request, SESSION_ID, sessionId));
        }
    }

    /**
     * Route for topic
     */
    public CompletionStage<Result> topic(String topic) {
        return github.searchRepositories(topic, true).thenApply(rs -> ok(views.html.topic.render(rs)));
    }

    /**
     * Route for profile
     */
    public CompletionStage<Result> profile(String user) {
        return CompletableFuture.completedFuture(ok(views.html.profile.render(user)));
    }

    /**
     * Route for repository
     */
    public CompletionStage<Result> repository(String user, String repo) {
        String fullName = user + "/" + repo;
        return CompletableFuture.completedFuture(ok(views.html.repository.render(fullName)));
    }

    
    public CompletionStage<Result> issueStatistics(String user, String repo,Http.Request request){
    	CompletionStage<Result> result = issueService.getIssueStatistics(user, repo).thenApplyAsync(
    			op -> ok(views.html.issuesStatistics.render(op, request)));
    			
    	return result;
    }


    /**
     * Return Commit Statistics Page.
     * This page contains number of commits and max, min and average deletion by top 10 committers in latest 100 commits.
     * @param user gihub username of owner of repository
     * @param repo repository name
     * @param request request parameter
     * @return commit statistcs page
     * @author Smit Parmar
     *
     */
    public CompletionStage<Result> commits(String user, String repo, Http.Request request)  {
        CompletionStage<Result> resultCompletionStage = null;
        try{
            resultCompletionStage =  commitService.getCommitStats(user,repo)
                    .thenApplyAsync(output -> ok(views.html.commits.render(output, request)));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return resultCompletionStage;
    }
}
