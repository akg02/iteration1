package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;

import java.util.*;

public class UserActor extends AbstractActor {
    private final ActorRef ws;

    public UserActor(ActorRef ws) {
        this.ws = ws;
        Logger.debug("New User Actor {} for websocket {}; timeActor {}",self(), ws);
    }

    static public Props props(ActorRef ws){
        return Props.create(UserActor.class, ws);
    }

    @Override
    public void preStart() throws Exception {
        context().actorSelection("/user/timeActor")
                .tell(new TimeActor.RegisterMsg(), self());
        context().actorSelection("/user/commitActor")
                .tell(new CommitActor.RegisterMsg(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TimeMessage.class, this::sendTime)
                .match(CommitMessage.class, this::sendCommitMessage)
                .build();
    }

    static public class CommitMessage {
        public final ArrayList<Map<String,Integer>> list;
        public CommitMessage(ArrayList<Map<String, Integer>> list){
            this.list = list;
        }
    }

    private void sendCommitMessage(CommitMessage msg) {
        final ObjectNode response = Json.newObject();
        response.put("list", msg.list.toString());
        ws.tell(response, self());
    }

    static public class TimeMessage {
        public final String time;
        public TimeMessage(String time){
            this.time = time;
        }
    }

    private void sendTime(TimeMessage msg) {
        final ObjectNode response = Json.newObject();
        response.put("time", msg.time);
        ws.tell(response, self());
    }

}
