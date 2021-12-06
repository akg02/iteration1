package actors;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import play.Logger;
import scala.concurrent.duration.Duration;
import services.RepositoryProfileService;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RepositoryActor extends AbstractActorWithTimers {

    private Set<ActorRef> myUserActors;
    public RepositoryProfileService rpService = RepositoryProfileService.getInstance();

    @Inject
    private RepositoryActor(){
        this.myUserActors = new HashSet<>();
    }


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

    static public Props getProps() {
        return Props.create(RepositoryActor.class, ()-> new RepositoryActor());
    }

    @Override
    public void preStart() {
        Logger.info("RepoActor {} started", self());
        //getTimers().startPeriodicTimer("Timer", new Tick("a", "a"), Duration.create(5, TimeUnit.SECONDS));
    }

    public void fiveSecondRefresh(String name, String repo){
        getTimers().startPeriodicTimer("RepoTimer", new Tick(name, repo), Duration.create(300, TimeUnit.SECONDS));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, msg -> {
                    fiveSecondRefresh(msg.name, msg.repo);
                    notifyClients(msg.name, msg.repo);
                })
                .match(RegisterMsg.class, msg -> myUserActors.add(sender()))
                .build();
    }

    private void notifyClients(String name, String repo) {
        rpService.getRepoDetails(name, repo).thenAccept(r -> {
            UserActor.RepoMessage rMsg = new UserActor.RepoMessage(r);
            myUserActors.forEach(ar -> ar.tell(rMsg, self()));
        });
    }



}
