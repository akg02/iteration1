package controllers;

//import actors.MyUserActor;
import actors.RepositoryActor;
import actors.TimeActor;
import actors.UserActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.google.inject.Inject;

import models.GithubClient;
import models.SearchHistory;
import models.SearchResult;
import play.cache.AsyncCacheApi;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.CommitService;
import services.IssueService;
import services.RepositoryProfileService;
import views.html.repository;
import services.ProfileInfoService;

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
    private final RepositoryProfileService repositoryProfileService;
    private final ProfileInfoService profileInfoService;

    private AsyncCacheApi cache;

    @Inject
    private ActorSystem actorSystem;

    @Inject
    private Materializer materializer;

    private ActorRef repoActor;
    

    /** The SearchController constructor
     * @author Hop Nguyen
     */
    @Inject
    public SearchController(GithubClient github, FormFactory formFactory, MessagesApi messagesApi, AsyncCacheApi asyncCacheApi, ActorSystem system) {
        this.github = github;
        this.searchForm = formFactory.form(SearchForm.class);
        this.messagesApi = messagesApi;
        this.searchHistory = new SearchHistory();
        this.issueService  = new IssueService(github);
        this.commitService = new CommitService(github);
        this.repositoryProfileService = new RepositoryProfileService(github);
        this.cache = asyncCacheApi;
        this.profileInfoService = new ProfileInfoService(github);

        system.actorOf(RepositoryActor.getProps(), "repoActor");

        //system.actorOf(TimeActor.getProps(), "timeActor");

    }

    public Result timeMe(Http.Request request) {
        return ok(views.html.timer.render(request));
    }

    public WebSocket ws() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(UserActor::props, actorSystem, materializer));
    }
    /**
     * The homepage which displays the search history of the current session
     * @author Hop Nguyen
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
     * @author Hop Nguyen
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
                    .thenApplyAsync(nullResult -> redirect(routes.SearchController.index())
                            .addingToSession(request, SESSION_ID, sessionId));
        }
    }

    /**
     * Route for topic
     * @author Hop Nguyen
     */
    public CompletionStage<Result> topic(String topic) {
        return github.searchRepositories(topic, true).thenApplyAsync(rs -> ok(views.html.topic.render(rs)));
    }

   /**
    * Controller Method for api: /profile/:user
    * displays details of the users public profile page and hyperlinks to repositories
    * @author Joon Seung Hwang
    * @param user username of github
    * @return user profile page
    */
    public CompletionStage<Result> profile(String user) {
        CompletionStage<Result> cache = this.cache
        		.getOrElseUpdate("repository." + user, () -> profileInfoService.getRepoList(user)
        				.thenApply(r -> ok(views.html.profile.render(r))));
        return cache;        
    }

    /**
     * Controller Method for api : /repository/:user/:repo </br>
     * This page displays repository details and latest 20 issues of the repository
     * @author Sagar Sanghani
     * @param user username of github repository
     * @param repo repository name
     * @return Returns repository page
     */
    public CompletionStage<Result> repository(String user, String repo){
        CompletionStage<Result> cache = this.cache.getOrElseUpdate("repository." + user + "." + repo, () -> repositoryProfileService.getRepoDetails(user, repo).thenApply(rd -> ok(repository.render(rd, user))));
//      CompletionStage<Result> result = repositoryProfileService.getRepoDetails(user, repo).thenApply(rd -> ok(views.html.repository.render(rd, user)));
        return cache;
    }

    /**
     * 
     * Controller Method for api : /issueStatistics
     * @author Meet Mehta
     * @param user username of github repository
     * @param repo repository name
     * @param request Http.Request 
     * @return page displaying word count of issues title in descending order. 
     */
  
    public CompletionStage<Result> issueStatistics(String user, String repo,Http.Request request){
    	CompletionStage<Result> result = this.cache.getOrElseUpdate("issueStat."+user+"."+repo,()->issueService.getIssueStatistics(user, repo).thenApplyAsync(
    			op -> ok(views.html.issuesStatistics.render(op, request)).withHeader(CACHE_CONTROL, "max-age=3600")));
    	 
    			
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
    public CompletionStage<Result> commits(String user, String repo, Http.Request request)  throws Exception {
        CompletionStage<Result> resultCompletionStage = null;

        resultCompletionStage =  this.cache.getOrElseUpdate("commits." + user + "." + repo,() -> commitService.getCommitStats(user,repo)
                .thenApplyAsync(output -> ok(views.html.commits.render(output, request))));

        return resultCompletionStage;
    }
}
