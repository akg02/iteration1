package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import models.GithubClient;
import models.Repository;
import models.SearchResult;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class TopicActorTest {
    @Test
    public void testTopic() {
        final GithubClient github = Mockito.mock(GithubClient.class);
        final ActorSystem actorSystem = ActorSystem.create();
        try {
            SearchResult r1 = new SearchResult();
            r1.setInput("reactive");
            r1.setRepositories(Arrays.asList(new Repository("hope", "java", Collections.emptyList())));
            Mockito.when(github.searchRepositories("reactive", true))
                    .thenReturn(CompletableFuture.completedFuture(r1));
            new TestKit(actorSystem) {{
                final Props props = TopicActor.props(actorSystem, Duration.ofSeconds(1), getRef(), github);
                final ActorRef topicActor = actorSystem.actorOf(props);
                topicActor.tell("reactive", getRef());
                within(Duration.ofSeconds(3), () -> {
                    awaitCond(this::msgAvailable);
                    expectMsg("{\"input\":\"reactive\",\"items\":[{\"user\":\"hope\",\"name\":\"java\",\"topics\":[]}]}");
                    expectNoMessage();
                    return null;
                });
                SearchResult r2 = new SearchResult();
                r2.setInput("reactive");
                r2.setRepositories(Arrays.asList(new Repository("concordia", "playframework", Collections.emptyList())));
                Mockito.when(github.searchRepositories("reactive", true))
                        .thenReturn(CompletableFuture.completedFuture(r2));
                within(Duration.ofSeconds(3), () -> {
                    awaitCond(this::msgAvailable);
                    expectMsg("{\"input\":\"reactive\",\"items\":[{\"user\":\"concordia\",\"name\":\"playframework\",\"topics\":[]}]}");
                    expectNoMessage();
                    return null;
                });
            }};
        } finally {
            TestKit.shutdownActorSystem(actorSystem);
        }
    }
}