package controllers;

import com.google.inject.Inject;
import models.GithubClient;
import models.SearchResult;
import models.SearchService;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

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
    private final SearchService searchService;

    @Inject
    public SearchController(GithubClient github, FormFactory formFactory, MessagesApi messagesApi) {
        this.github = github;
        this.searchForm = formFactory.form(SearchForm.class);
        this.messagesApi = messagesApi;
        this.searchService = new SearchService(github);
    }

    /**
     * The homepage which displays the search history of the current session
     */
    public CompletionStage<Result> index(Http.Request request) {
        String sessionId = request.session().get(SESSION_ID).orElseGet(() -> UUID.randomUUID().toString());
        List<SearchResult> searchResults = searchService.getHistory(sessionId);
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
            return searchService.searchThenAddToHistory(sessionId, searchInput)
                    .thenApply(rs ->
                            redirect(routes.SearchController.index())
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

        return github.getRepositoryDetails(user, repo).thenApply(rd -> ok(views.html.repository.render(rd, user)));
    }
}
