package actors;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.google.inject.Inject;
import play.Logger;
import scala.concurrent.duration.Duration;
import services.ProfileInfoService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UserDataActor extends AbstractActorWithTimers {

    private Set<ActorRef> userActors;
    private  ProfileInfoService profileInfoService = ProfileInfoService.getInstance();

    static public class Tick{
        public String name;

        public Tick(String name) {
            this.name = name;
        }

    }

    static public class RegisterMsg{

    }

    static public Props props(){
        return Props.create(UserDataActor.class, () -> new UserDataActor());
    }

    @Inject
    private UserDataActor(){
        this.userActors = new HashSet<>();
    }

    @Override
    public void preStart() {
        Logger.info("UserData {} started", self());
        //getTimers().startPeriodicTimer("Timer", new Tick("a", "a"), Duration.create(5, TimeUnit.SECONDS));
    }

    public void fiveSecondRefresh(String name){
        getTimers().startPeriodicTimer("UserData Actor", new Tick(name), Duration.create(15, TimeUnit.SECONDS));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Tick.class, msg -> {
                    fiveSecondRefresh(msg.name);
                    notifyClients(msg.name);
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

    private void notifyClients(String userName){
        profileInfoService.getRepoList(userName).thenAcceptAsync(list -> {
            UserActor.UserProfileMessage tMsg = new UserActor.UserProfileMessage(list);
            userActors.forEach(ar -> ar.tell(tMsg, self()));
        });

    }
}