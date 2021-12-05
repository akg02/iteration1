package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import models.GithubClient;
import models.SearchHistory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchActor extends ActorWithRefresh {
    public static Props props(ActorSystem system, Duration refreshInterval,
                              ActorRef out, GithubClient github, SearchHistory history) {
        return Props.create(SearchActor.class, system, refreshInterval, out, github, history);
    }

    private final ActorRef out;
    private final GithubClient github;
    private final SearchHistory history;
    private int lastSentVersion = 0;

    public SearchActor(ActorSystem actorSystem, Duration refreshInterval,
                       ActorRef out, GithubClient github, SearchHistory history) {
        super(actorSystem, refreshInterval);
        this.out = out;
        this.github = github;
        this.history = history;
    }

    @Override
    protected void doRefresh() {
        final List<String> queries = history.getQueries();
        log.info("refresh search results for queries {}", queries);
        CompletableFuture<?>[] futures = new CompletableFuture[queries.size()];
        for (int i = 0; i < queries.size(); i++) {
            futures[i] = github.searchRepositories(queries.get(i), false)
                    .thenAccept(history::updateHistory)
                    .toCompletableFuture();
        }
        CompletableFuture.allOf(futures).thenAccept(nullValue -> replyLatestResults());
    }

    private void replyLatestResults() {
        if (lastSentVersion < history.getVersion()) {
            lastSentVersion = history.getVersion();
            log.info("search results have updated to version {}", lastSentVersion);
            out.tell(history.toJson(), self());
        } else {
            log.info("search results haven't changed");
        }
    }

    private void searchQuery(String query) {
        github.searchRepositories(query, false)
                .thenAccept(result -> {
                    log.info("search repositories for query [{}]", query);
                    history.addToHistory(result);
                    replyLatestResults();
                });
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(TICK, m -> doRefresh())
                .match(String.class, this::searchQuery)
                .build();
    }
}
