package actors;


import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import play.Logger;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TimeActor extends AbstractActorWithTimers {
    private Set<ActorRef> userActors;

    private static final class Tick{

    }

    static public class RegisterMsg{

    }

    static public Props props(){
        return Props.create(TimeActor.class, () -> new TimeActor());
    }

    private TimeActor(){
        this.userActors = new HashSet<>();
    }

    public void preStart(){
        Logger.info("TimeActor {} started", self());
        getTimers().startPeriodicTimer("Timer", new Tick(), Duration.create(5, TimeUnit.SECONDS));
    }

    public Receive createReceive(){
        return receiveBuilder()
                .match(Tick.class, msg -> notifyClients())
                .match(RegisterMsg.class, msg -> userActors.add(sender()))
                .build();
    }

    private void notifyClients(){
        UserActor.TimeMessage tMsg = new UserActor.TimeMessage(LocalDateTime.now().toString());
        userActors.forEach(ar -> ar.tell(tMsg, self()));
    }
}
