package actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import models.GithubClient;
import models.SearchHistory;
import models.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActor extends AbstractActorWithTimers {
    public static Props props(ActorRef out, GithubClient github, SearchHistory history) {
        return Props.create(SearchActor.class, out, github, history);
    }

    private final Object TICK = new Object();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ActorRef out;
    private final ActorRef github;
    private final SearchHistory history;
    private int lastSentVersion = 0;
    private String addingQuery;

    public SearchActor(ActorRef out, GithubClient github, SearchHistory history) {
        this.out = out;
        this.github = context().actorOf(GithubActor.props(github));
        this.history = history;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        getTimers().startTimerAtFixedRate("timer", TICK, Duration.ofSeconds(0), Duration.ofSeconds(5));
    }

    private void refreshSearchResults() {
        List<String> queries = history.getResults().stream().map(SearchResult::getInput).collect(Collectors.toList());
        log.info("refresh search results for queries {}", queries);
        for (String query : queries) {
            github.tell(new GithubActor.SearchRequest(query, false), self());
        }
    }

    private void replyLatestResults() {
        if (lastSentVersion < history.getVersion()) {
            lastSentVersion = history.getVersion();
            log.info("search results have updated to version {}", lastSentVersion);
            out.tell(Json.stringify(Json.toJson(history.getResults())), self());
        } else {
            log.info("search results haven't changed");
        }
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
                10,
                Duration.ofMinutes(1),
                DeciderBuilder.matchAny(e -> {
                    log.info("restarting Github Actor");
                    return (SupervisorStrategy.Directive) SupervisorStrategy.restart();
                }).build());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(TICK, m -> refreshSearchResults())
                .match(String.class, query -> {
                    addingQuery = query;
                    github.tell(new GithubActor.SearchRequest(query, false), self());
                })
                .match(SearchResult.class, result -> {
                    if (result.getInput().equals(addingQuery)) {
                        addingQuery = null;
                        history.addToHistory(result);
                    }
                    history.updateHistory(result);
                    replyLatestResults();
                })
                .build();
    }
}
