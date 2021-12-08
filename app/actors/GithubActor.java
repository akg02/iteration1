package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.GithubClient;
import models.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GithubActor extends AbstractActor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GithubClient github;
    private final List<SearchRequest> searchRequests = new ArrayList<>();
    private boolean executing = false;
    private int counter = 0;

    public static Props props(GithubClient github) {
        return Props.create(GithubActor.class, github);
    }

    public GithubActor(GithubClient github) {
        this.github = github;
    }

    @Override
    public void preStart() throws Exception {
        counter = 0;
        executing = false;
        searchRequests.clear();
    }

    private void executePendingRequests() {
        if (!executing && !searchRequests.isEmpty()) {
            executing = true;
            SearchRequest next = searchRequests.remove(0);
            log.info("counter=[{}] : execute query {}", counter++, next.query);
            searchRequests.removeIf(r -> r.query.equals(next.query) && r.topic == next.topic);
            github.searchRepositories(next.query, next.topic)
                    .thenAccept(result -> self().tell(result, next.replyTo));
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, searchRequest -> {
                    searchRequest.replyTo = context().sender();
                    searchRequests.add(searchRequest);
                    executePendingRequests();
                })
                .match(SearchResult.class, searchResult -> {
                    executing = false;
                    if (!searchResult.isSuccess()) {
                        throw new Exception("Failed to search query " + searchResult.getInput());
                    } else {
                        context().sender().tell(searchResult, self());
                        executePendingRequests();
                    }
                })
                .build();
    }

    public static class SearchRequest {
        private final String query;
        private final boolean topic;
        private ActorRef replyTo;

        public SearchRequest(String query, boolean topic) {
            this.query = query;
            this.topic = topic;
        }
    }
}
