package actors;

import java.util.*;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.inject.Inject;
import play.Logger;
import scala.concurrent.duration.Duration;
import services.CommitService;

public class CommitActor extends AbstractActorWithTimers {

    private Set<ActorRef> userActors;
    private  CommitService commitService = CommitService.getInstance();

    static public class Tick{
        public String name;
        public String repo;

        public Tick(String name, String repo) {
            this.name = name;
            this.repo = repo;
        }

    }

    static public class RegisterMsg{

    }

    static public Props props(){
        return Props.create(CommitActor.class, () -> new CommitActor());
    }

    @Inject
    private CommitActor(){
        this.userActors = new HashSet<>();
    }

    @Override
    public void preStart() {
        Logger.info("CommitActor {} started", self());
        //getTimers().startPeriodicTimer("Timer", new Tick("a", "a"), Duration.create(5, TimeUnit.SECONDS));
    }

    public void fiveSecondRefresh(String name, String repo){
        getTimers().startPeriodicTimer("commitActor", new Tick(name, repo), Duration.create(15, TimeUnit.SECONDS));
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
        commitService.getCommitStats(userName,repoName).thenAcceptAsync(list -> {
            UserActor.CommitMessage tMsg = new UserActor.CommitMessage(list);
            userActors.forEach(ar -> ar.tell(tMsg, self()));
        });

    }
}