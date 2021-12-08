package actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import models.GithubClient;
import models.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;

import java.time.Duration;

public class TopicActor extends AbstractActorWithTimers {
    public static Props props(ActorRef out, GithubClient github) {
        return Props.create(TopicActor.class, out, github);
    }

    private final Object TICK = new Object();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ActorRef out;
    private final ActorRef github;
    private SearchResult lastSentResult;

    public TopicActor(ActorRef out, GithubClient github) {
        this.out = out;
        this.github = context().actorOf(GithubActor.props(github));
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        getTimers().startTimerAtFixedRate("timer", TICK, Duration.ofSeconds(0), Duration.ofSeconds(5));
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

    private void doRefresh() {
        if (lastSentResult != null) {
            String query = lastSentResult.getInput();
            log.info("refreshing the results of topic [{}]", query);
            github.tell(new GithubActor.SearchRequest(query, true), self());
        }
    }

    private void replyLatestResult(SearchResult newResult) {
        String query = newResult.getInput();
        if (lastSentResult != null && lastSentResult.getRepositories().equals(newResult.getRepositories())) {
            log.info("results of topic [{}] haven't changed", query);
        } else {
            lastSentResult = newResult;
            log.info("sending new results of topic [{}]", query);
            String resp = Json.stringify(Json.toJson(lastSentResult));
            out.tell(resp, self());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(TICK, m -> doRefresh())
                .match(String.class, query -> {
                    log.info("searching the results of topic [{}]", query);
                    github.tell(new GithubActor.SearchRequest(query, true), self());
                })
                .match(SearchResult.class, this::replyLatestResult)
                .build();
    }
}
