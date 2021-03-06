package actors;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.inject.Inject;
import play.Logger;
import scala.concurrent.duration.Duration;
import services.CommitService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This is the CommitActor
 *
 * @author Smit Parmar
 */
public class CommitActor extends AbstractActorWithTimers {

    private Set<ActorRef> userActors;
    private  CommitService commitService = CommitService.getInstance();

    /**
     * This is Tick class which is responsible for making data dynamic
     */
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

    /**
     * This will create new object of CommitActor
     * @return new CommitActor object
     */
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
//        getTimers().startPeriodicTimer("Timer", new Tick(userName, repoName), Duration.create(5, TimeUnit.SECONDS));
    }

    public void fiveSecondRefresh(String name, String repo){
        getTimers().startPeriodicTimer("commitActor", new Tick(name, repo), Duration.create(5, TimeUnit.SECONDS));
    }


    /**
     * Here, we will decide action according to message class
     * @return course of action
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, msg -> {
                    fiveSecondRefresh(msg.name, msg.repo);
                    notifyClients(msg.name, msg.repo);
                })
                .match(RegisterMsg.class, msg -> userActors.add(sender()))
                .match(String.class, msg -> {
                    System.out.println("inside here");
                    System.out.println(userActors);
                    userActors.forEach(u -> {
                        System.out.println(u.toString());
                        u.tell("{\"list\":[]}",u);
                    });
                })
                .build();
    }

    /**
     * We will get data from service and will tell useractor about it
     * @param userName username
     * @param repoName repository name
     */
    private void notifyClients(String userName, String repoName){
        commitService.getCommitStats(userName,repoName).thenAcceptAsync(list -> {
            UserActor.CommitMessage tMsg = new UserActor.CommitMessage(list);
            userActors.forEach(ar -> ar.tell(tMsg, self()));
        });

    }
}