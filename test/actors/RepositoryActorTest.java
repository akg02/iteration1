package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import models.RepositoryProfile;
import org.junit.Test;
import org.mockito.Mockito;
import services.RepositoryProfileService;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Test class to test repository actor
 * @author Sagar Sanghani
 */
public class RepositoryActorTest {

    @Test
    public void testRepository() {
        final RepositoryProfileService repoService = Mockito.mock(RepositoryProfileService.class);
        final ActorSystem actorSystem = ActorSystem.create();
        try {
            RepositoryProfile res1 = new RepositoryProfile();
            Mockito.when(repoService.getRepoDetails("Sagar7421", "justADummyRepo"))
                    .thenReturn(CompletableFuture.completedFuture(res1));


            new TestKit(actorSystem) {{
                final Props props = RepositoryActor.getProps();
                final ActorRef repoActorRef = actorSystem.actorOf(props);

                repoActorRef.tell(new RepositoryActor.RegisterMsg(), getRef());


                repoActorRef.tell("data", getRef());
                within(Duration.ofSeconds(5), () -> {
                    awaitCond(this::msgAvailable);
                    expectMsg("{\"name\":\"justADummyRepo\",\"description\":\"This is just a dummy repository, deleting it.\",\"starC\":1,\"forkC\":0,\"topic\":\"[]\",\"createDate\":\"Sat Nov 20 18:19:38 EST 2021\",\"lastUpDate\":\"Fri Dec 03 00:42:46 EST 2021\",\"issueList\":\"[]\"}");
                    expectNoMessage();
                    return null;
                });


            }};
        } finally {
            TestKit.shutdownActorSystem(actorSystem);
        }
    }
}
