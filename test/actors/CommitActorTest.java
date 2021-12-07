package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.Test;
import org.mockito.Mockito;
import services.CommitService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommitActorTest {
    @Test
    public void testTopic() {
        final CommitService commitService = Mockito.mock(CommitService.class);
        final ActorSystem actorSystem = ActorSystem.create();
        try {

            ArrayList<Map<String, Integer>> result1 = new ArrayList<>();
            Mockito.when(commitService.getCommitStats("smituparmar", "medicograph"))
                    .thenReturn(CompletableFuture.completedFuture(result1));

            new TestKit(actorSystem) {{
                System.out.println(actorSystem);
                final Props props = CommitActor.props();
                final ActorRef commitActorRef = actorSystem.actorOf(props);

                commitActorRef.tell(new CommitActor.RegisterMsg(), getRef());

                commitActorRef.tell("data",getRef());

                within(Duration.ofSeconds(5), () -> {
                    awaitCond(this::msgAvailable);
                    expectMsg("{\"list\":[]}");
                    expectNoMessage();
                    return null;
                });

            }};
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            TestKit.shutdownActorSystem(actorSystem);
        }
    }
}