package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;

public class CommitActorTest {
    static ActorSystem actorSystem;
    private ActorRef commitActor;

    @Before
    public  void setup() {
        actorSystem = ActorSystem.create();
    }
    @After
    public void teardown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

//    @Test
//    public void testGreeterActorSendingOfGreeting() {
//        final TestKit testProbe = new TestKit(actorSystem);
//
//    }

}
