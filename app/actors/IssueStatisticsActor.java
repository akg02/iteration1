package actors;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import play.Logger;

import com.google.inject.Inject;

import actors.CommitActor.Tick;
import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import services.IssueService;
import scala.concurrent.duration.Duration;


public class IssueStatisticsActor extends AbstractActorWithTimers {

	private Set<ActorRef> userActors;
	private IssueService issueService = IssueService.getInstance();

	static public class Tick {
		public String name;
		public String repo;

		public Tick(String name, String repo) {
			this.name = name;
			this.repo = repo;
		}

	}
	
	static public class RegisterMsg{

    }

	static public Props props() {
		return Props.create(IssueStatisticsActor.class, () -> new IssueStatisticsActor());
	}

	@Inject
	private IssueStatisticsActor() {
		this.userActors = new HashSet<>();
	}

	@Override
	public void preStart() {
		//logger.info("IssueStatistics Actor {} started", self());
		Logger.info("Issue {} started", self());
	}
	
	public void fiveSecondRefresh(String name, String repo){
        getTimers().startPeriodicTimer("Issue statistics Actor", new Tick(name, repo), Duration.create(15, TimeUnit.SECONDS));
    }
	
	@Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, msg -> {
                    fiveSecondRefresh(msg.name, msg.repo);
                    notifyClients(msg.name, msg.repo);
                })
                .match(RegisterMsg.class, msg -> userActors.add(sender()))
                .build();
    }
	
	private void notifyClients(String userName, String repoName){
		issueService.getIssueStatistics(userName,repoName).thenAcceptAsync(list -> {
            UserActor.IssueStatisticsMessage tMsg = new UserActor.IssueStatisticsMessage(list);
            userActors.forEach(ar -> ar.tell(tMsg, self()));
        });

    }

}
