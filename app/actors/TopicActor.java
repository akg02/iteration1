package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import models.GithubClient;
import models.SearchResult;
import play.libs.Json;

import java.time.Duration;

public class TopicActor extends ActorWithRefresh {
    public static Props props(ActorSystem actorSystem, Duration refreshInterval, ActorRef out, GithubClient github) {
        return Props.create(TopicActor.class, actorSystem, refreshInterval, out, github);
    }
    private final ActorRef out;
    private final GithubClient github;

    private SearchResult lastResult;

    public TopicActor(ActorSystem actorSystem, Duration refreshInterval, ActorRef out, GithubClient github) {
        super(actorSystem, refreshInterval);
        this.out = out;
        this.github = github;
    }

    @Override
    protected void doRefresh() {
        if (lastResult != null) {
            String query = lastResult.getInput();
            log.info("refreshing the results of topic [{}]", query);
            github.searchRepositories(query, true)
                    .thenAccept(result -> {
                        if (lastResult.getRepositories().equals(result.getRepositories())) {
                            log.info("results of topic [{}] haven't changed", query);
                        } else {
                            lastResult = result;
                            log.info("sending new results of topic [{}]", query);
                            String resp = Json.stringify(Json.toJson(lastResult));
                            out.tell(resp, self());
                        }
                    });
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(TICK, m -> doRefresh())
                .match(String.class, query ->
                        github.searchRepositories(query, true)
                                .thenAccept(result -> {
                                    log.info("sending search results of topic [{}]", query);
                                    String resp = Json.stringify(Json.toJson(result));
                                    out.tell(resp, self());
                                    lastResult = result;
                                })
                )
                .build();
    }
}
