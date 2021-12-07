package controllers;

import actors.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.google.inject.Inject;
import models.GithubClient;
import models.SearchHistory;
import play.cache.AsyncCacheApi;
import services.HistoryService;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.CommitService;
import services.IssueService;
import services.ProfileInfoService;
import services.RepositoryProfileService;
import views.html.repository;

import java.util.UUID;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Hop Nguyen
 * @version 1: Hop Nguyen implements the project framework, search, and topic feature.
 */
public class SearchController extends Controller {
    public static final String SESSION_ID = "session_id";

    private final GithubClient github;
    private final HistoryService historyService;
    private final IssueService issueService;

    private final CommitService commitService;
    private final RepositoryProfileService repositoryProfileService;
    private final ProfileInfoService profileInfoService;

    private final AsyncCacheApi cache;
    private final ActorSystem actorSystem;
    private final Materializer materializer;


    private ActorRef commitActor;
    private ActorRef repoActor;
    private ActorRef issueStatisticsActor;
    private ActorRef userProfileActor;
    public String fSessionId;

    /** The SearchController constructor
     * @author Hop Nguyen
     */
    @Inject
    public SearchController(GithubClient github, AsyncCacheApi asyncCacheApi, ActorSystem actorSystem, Materializer materializer) {
        this.github = github;
        this.cache = asyncCacheApi;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.historyService = new HistoryService();
        this.issueService  = new IssueService(github);
        this.commitService = new CommitService(github);
        this.repositoryProfileService = new RepositoryProfileService(github);
        this.profileInfoService = new ProfileInfoService(github);

        this.commitActor = actorSystem.actorOf(CommitActor.props(), "commitActor");
        actorSystem.actorOf(TimeActor.props(), "timeActor");
        //this.repoActor = system.actorOf(RepositoryActor.getProps(), "myrepoActor");

        //system.actorOf(TimeActor.getProps(), "timeActor");

    }

    public Result timeMe(Http.Request request) {
        return ok(views.html.timer.render(request));
    }

    public WebSocket ws() {
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(f -> UserActor.props(f, fSessionId), actorSystem, materializer));
    }

    public Result mytestRepo(Http.Request request, String name, String repo){
        fSessionId = request.session().get(SESSION_ID).orElseGet(() -> UUID.randomUUID().toString());
        repoActor = actorSystem.actorOf(RepositoryActor.getProps(), "myrepoActor_"+fSessionId);

        actorSystem.actorSelection("/user/myrepoActor_"+fSessionId).tell(new RepositoryActor.Tick(name, repo), repoActor);
        return ok(views.html.repo2.render(request));
    }
    /**
     * The homepage which displays the search history of the current session
     * @author Hop Nguyen
     */
    public CompletionStage<Result> search(Http.Request request) {
        String sessionId = request.session().get(SESSION_ID).orElseGet(() -> UUID.randomUUID().toString());
        return CompletableFuture.completedFuture(
                ok(views.html.search.render(request)).addingToSession(request, SESSION_ID, sessionId));
    }

    public WebSocket searchWebSocket() {
        return WebSocket.Text.accept(request -> {
            String sessionId = request.session().get(SESSION_ID).orElseGet(() -> UUID.randomUUID().toString());
            SearchHistory searchHistory = historyService.getHistory(sessionId);
            return ActorFlow.actorRef(
                    out -> SearchActor.props(actorSystem, Duration.ofSeconds(10), out, github, searchHistory),
                    actorSystem,
                    materializer);
        });
    }

    /**
     * Route for topic
     * @author Hop Nguyen
     */
    public CompletionStage<Result> topic(String topic, Http.Request request) {
        return CompletableFuture.completedFuture(ok(views.html.topic.render(topic, request)));
    }

    public WebSocket topicWebSocket() {
        return WebSocket.Text.accept(request ->
                ActorFlow.actorRef(
                        out -> TopicActor.props(actorSystem, Duration.ofSeconds(5), out, github),
                        actorSystem,
                        materializer));
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

    public WebSocket commitSocket(){
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(f -> UserActor.props(f, fSessionId), actorSystem, materializer));
    }

    public WebSocket userDataSocket(){
        return WebSocket.Json.accept(request -> ActorFlow.actorRef(f -> UserActor.props(f, fSessionId), actorSystem, materializer));
    }

    public WebSocket issueStatisticsSocket() {
    	return WebSocket.Json.accept(request -> ActorFlow.actorRef(f->UserActor.props(f, fSessionId), actorSystem, materializer));
    }

    public Result issueStatisticsPage(Http.Request request,String name, String repo) {
        fSessionId = request.session().get(SESSION_ID).orElseGet(() -> UUID.randomUUID().toString());
        issueStatisticsActor = actorSystem.actorOf(IssueStatisticsActor.props(),"issueStatisticsActor"+fSessionId);

        actorSystem.actorSelection("/user/issueStatisticsActor"+fSessionId).tell(new IssueStatisticsActor.Tick(name, repo),issueStatisticsActor);
        return ok(views.html.issueActor.render(request));
    }


    public Result commitSocketPage(Http.Request request, String name, String repo){
        fSessionId = UUID.randomUUID().toString();
        commitActor = actorSystem.actorOf(CommitActor.props(), "commitActor"+fSessionId);

        actorSystem.actorSelection("/user/commitActor"+fSessionId).tell(new CommitActor.Tick(name, repo), commitActor);
        return ok(views.html.actor.render(request));
    }

    public Result UserSocketPage(Http.Request request, String name){
        fSessionId = UUID.randomUUID().toString();
        userProfileActor = actorSystem.actorOf(UserDataActor.props(), "userProfileActor"+fSessionId);

        actorSystem.actorSelection("/user/userProfileActor"+fSessionId).tell(new UserDataActor.Tick(name), userProfileActor);
        return ok(views.html.profileActor.render(request));
    }

}
