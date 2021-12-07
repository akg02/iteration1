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

/**
 * @author Sagar Sanghani
 *
 * Class for repository actor, it uses timer to do a refresh
 */
public class RepositoryActor extends AbstractActorWithTimers {

    private Set<ActorRef> myUserActors;
    public RepositoryProfileService rpService = RepositoryProfileService.getInstance();

    @Inject
    private RepositoryActor(){
        this.myUserActors = new HashSet<>();
    }

    /**
     * A class to represent Tick message which has repository name and user name
     */
    static public class Tick{
        public String name;
        public String repo;

        /**
         * Parameterized constructor for Tick class
         * @param name user name
         * @param repo name of the repository
         */
        public Tick(String name, String repo) {
            this.name = name;
            this.repo = repo;
        }

    }

    /**
     * Class to represent RegisterMsg, used when registering the Actors
     */
    static public class RegisterMsg{
    }

    /**
     * Function to create a new actor of Repository Class
     * @return a repository actor
     */
    static public Props getProps() {
        return Props.create(RepositoryActor.class, ()-> new RepositoryActor());
    }

    @Override
    public void preStart() {
        Logger.info("RepoActor {} started", self());
    }

    /**
     * Function to refresh the RepositoryActor with new data using Tick messages
     * @param name name of the user
     * @param repo name of the repository
     */
    public void fiveSecondRefresh(String name, String repo){
        getTimers().startPeriodicTimer("RepoTimer", new Tick(name, repo), Duration.create(5, TimeUnit.SECONDS));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, msg -> {
                    fiveSecondRefresh(msg.name, msg.repo);
                    notifyClients(msg.name, msg.repo);
                })
                .match(RegisterMsg.class, msg -> myUserActors.add(sender()))
                .match(String.class, msg -> {
                    myUserActors.forEach(u -> {
                        u.tell("{\"name\":\"justADummyRepo\",\"description\":\"This is just a dummy repository, deleting it.\",\"starC\":1,\"forkC\":0,\"topic\":\"[]\",\"createDate\":\"Sat Nov 20 18:19:38 EST 2021\",\"lastUpDate\":\"Fri Dec 03 00:42:46 EST 2021\",\"issueList\":\"[]\"}",u);
                    });
                })
                .build();
    }


    /**
     * Function to fetch the details of the repo and pass it on to the user actor
     * @param name name of the user
     * @param repo name of the repository
     */
    private void notifyClients(String name, String repo) {
        rpService.getRepoDetails(name, repo).thenAccept(r -> {
            UserActor.RepoMessage rMsg = new UserActor.RepoMessage(r);
            myUserActors.forEach(ar -> ar.tell(rMsg, self()));
        });
    }

}
