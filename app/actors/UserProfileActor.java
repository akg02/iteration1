package actors;

import akka.actor.ActorRef;
import akka.actor.Props;

import akka.actor.AbstractActorWithTimers;
import play.Logger;
import scala.concurrent.duration.Duration;
import services.ProfileInfoService;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UserProfileActor extends AbstractActorWithTimers {
	
    private Set<ActorRef> myUserActors;
    public ProfileInfoService piService = ProfileInfoService.getInstance();
    
    @Inject
    private UserProfileActor() {
    	this.myUserActors = new HashSet<>();
    }
    
    static public class Tick{
        public String name;
       // public String repo;


        public Tick(String name) {
            this.name = name;
          //  this.repo = repo;
        }

    }

    static public class RegisterMsg{
    }
    
    static public Props getProps(){
        return Props.create(UserProfileActor.class, () -> new UserProfileActor());
    }

    @Override
    public void preStart() {
        Logger.info("UserProfileActor {} started", self());
        //getTimers().startPeriodicTimer("Timer", new Tick("a", "a"), Duration.create(5, TimeUnit.SECONDS));
    }

    public void fiveSecondRefresh(String name){
        getTimers().startPeriodicTimer("UserProfileTimer", new Tick(name), Duration.create(300, TimeUnit.SECONDS));
    }
    
    @Override
    public Receive createReceive() {
    	return receiveBuilder()
    			.match(Tick.class, msg -> {
    				fiveSecondRefresh(msg.name);
    				notifyClients(msg.name);
    			})
    			.match(RegisterMsg.class, msg -> myUserActors.add(sender()))
    			.build();
    }
    
    private void notifyClients(String user){
        piService.getRepoList(user).thenAcceptAsync(p -> {
            UserActor.ProfileMessage pMsg = new UserActor.ProfileMessage(p);
            myUserActors.forEach(ar -> ar.tell(pMsg, self()));
        });

    }


}
