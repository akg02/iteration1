package actors;

import java.util.*;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.http.impl.engine.ws.WebSocket;
import com.google.inject.Inject;
import models.GithubClient;
import play.Logger;
import scala.concurrent.duration.Duration;
import services.CommitService;

public class CommitActor extends AbstractActorWithTimers {

    private Set<ActorRef> userActors;
    private static GithubClient githubClient;
    private  CommitService commitService = CommitService.getInstance();

    private static final class Tick{

    }

    static public class RegisterMsg{

    }

    static public Props props(){
        return Props.create(CommitActor.class, () -> new CommitActor());
    }

    @Inject
    private CommitActor(){
        this.userActors = new HashSet<>();
//        this.githubClient = githubClient;
//        this.commitService = new CommitService(githubClient);
    }

    public void preStart(){
        Logger.info("CommitActor {} started", self());

        getTimers().startPeriodicTimer("Timer", new Tick(), Duration.create(25, TimeUnit.SECONDS));
    }

    public Receive createReceive(){
        return receiveBuilder()
                .match(Tick.class, msg -> notifyClients())
                .match(RegisterMsg.class, msg -> userActors.add(sender()))
                .build();
    }

    private void notifyClients(){
        commitService.getCommitStats("facebook","react").thenAcceptAsync(list -> {
            UserActor.CommitMessage tMsg = new UserActor.CommitMessage(list);
            userActors.forEach(ar -> ar.tell(tMsg, self()));
        });

    }
}
