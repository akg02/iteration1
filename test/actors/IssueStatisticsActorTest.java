package actors;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import akka.testkit.javadsl.TestKit;


import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import services.IssueService;

public class IssueStatisticsActorTest {

	@Test
	public void testIssueStatistics() {
		final IssueService issueService = Mockito.mock(IssueService.class);
		final ActorSystem actorSystem = ActorSystem.create();
		try {
			Map<String,Integer> stats = new HashMap<String,Integer>();
			Mockito.when(issueService.getIssueStatistics("meetmehta1198", "StudentAttendanceManagement"))
	        .thenReturn(CompletableFuture.completedFuture(stats));
			new TestKit(actorSystem) {{
                System.out.println(actorSystem);
                final Props props = IssueStatisticsActor.props();
                final ActorRef issueStatisticsActorRef = actorSystem.actorOf(props);

                issueStatisticsActorRef.tell(new IssueStatisticsActor.RegisterMsg(), getRef());

                issueStatisticsActorRef.tell("data",getRef());

                within(Duration.ofSeconds(1), () -> {
                    awaitCond(this::msgAvailable);
                    expectMsg("{\"message\":[]}");
                    expectNoMessage();
                    return null;
                });

            }};
        
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
            TestKit.shutdownActorSystem(actorSystem);

		}
		
	
	}
}
