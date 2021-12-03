package actors;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.RepositoryProfile;
import scala.concurrent.duration.Duration;
import services.RepositoryProfileService;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RepositoryActor extends AbstractActorWithTimers {

    private Set<ActorRef> myUserActors;
    public RepositoryProfileService rpService = RepositoryProfileService.getInstance();
    private String name;
    private String repo;

    @Inject
    private RepositoryActor(String name, String repo){
        this.myUserActors = new HashSet<>();
        this.name = name;
        this.repo = repo;

    }

    static public class Tick{

    }

    static public class RegisterMsg{
    }

//    static public class FetchRepo{
//        private String name;
//        private String repo;
//
//        public FetchRepo(String name, String repo) {
//            this.name = name;
//            this.repo = repo;
//        }
//    }

    static public Props getProps(String n, String r) {
        return Props.create(RepositoryActor.class, n, r);
    }

    @Override
    public void preStart() {
        getTimers().startPeriodicTimer("Timer", new Tick(), Duration.create(5, TimeUnit.SECONDS));
        //new FetchRepo("Sagar7421", "justADummyRepo");
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, msg -> notifyClients())
                .match(RegisterMsg.class, msg -> myUserActors.add(sender()))
//                .match(FetchRepo.class, msg -> gettingRepoDetails(msg.name, msg.repo))
                .build();
    }

    private void notifyClients() {
        rpService.getRepoDetails(this.name, this.repo).thenAccept(r -> {
            UserActor.RepoMessage rMsg = new UserActor.RepoMessage(r);
            myUserActors.forEach(ar -> ar.tell(rMsg, self()));
        });
    }



}
