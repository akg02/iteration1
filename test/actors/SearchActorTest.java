package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import models.GithubClient;
import models.Repository;
import models.SearchHistory;
import models.SearchResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class SearchActorTest {
    @Test
    public void testSearch() {
        final GithubClient github = Mockito.mock(GithubClient.class);
        final ActorSystem actorSystem = ActorSystem.create();
        try {
            SearchHistory searchHistory = new SearchHistory();
            SearchResult r1 = new SearchResult();
            r1.setInput("reactive");
            r1.setRepositories(Arrays.asList(new Repository("hope", "java", Collections.emptyList())));
            Mockito.when(github.searchRepositories("reactive", false))
                    .thenReturn(CompletableFuture.completedFuture(r1));

            new TestKit(actorSystem) {{
                final Props props = SearchActor.props(actorSystem, Duration.ofSeconds(1), getRef(), github, searchHistory);
                final ActorRef searchActorRef = actorSystem.actorOf(props);
                searchActorRef.tell("reactive", getRef());
                within(Duration.ofSeconds(1), () -> {
                    awaitCond(this::msgAvailable);
                    expectMsg("[{\"input\":\"reactive\",\"items\":[{\"user\":\"hope\",\"name\":\"java\",\"topics\":[]}]}]");
                    expectNoMessage();
                    return null;
                });

                SearchResult r2 = new SearchResult();
                r2.setInput("reactive");
                r2.setRepositories(Arrays.asList(new Repository("concordia", "soen", Collections.emptyList())));
                Mockito.when(github.searchRepositories("reactive", false))
                        .thenReturn(CompletableFuture.completedFuture(r2));

                // The SearchActor automatically sends out the history on init
                within(Duration.ofSeconds(3), () -> {
                    awaitCond(this::msgAvailable);
                    expectMsg("[{\"input\":\"reactive\",\"items\":[{\"user\":\"concordia\",\"name\":\"soen\",\"topics\":[]}]}]");
                    expectNoMessage();
                    return null;
                });

                assertEquals(1, searchHistory.getResults().size());
            }};
        } finally {
            TestKit.shutdownActorSystem(actorSystem);
        }
    }
}