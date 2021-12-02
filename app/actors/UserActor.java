package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.RepositoryProfile;
import play.libs.Json;

public class UserActor extends AbstractActor {
    private final ActorRef ws;

    public UserActor(final ActorRef wsOut) {
        ws =  wsOut;
    }

    static public Props props(final ActorRef wsOut) {
        return Props.create(UserActor.class, wsOut);
    }

    @Override
    public void preStart() {
        context().actorSelection("/user/timeActor")
                .tell(new TimeActor.RegisterMsg(), self());
        context().actorSelection("/user/repoActor")
                .tell(new RepositoryActor.RegisterMsg(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TimeMessage.class, this::sendTime)
                .match(RepoMessage.class, this::sendRepoMessage)
                .build();
    }

    static public class TimeMessage {
        public final String time;

        public TimeMessage(String time) {
            this.time = time;
        }
    }

    private void sendTime(TimeMessage msg) {
        final ObjectNode response = Json.newObject();
        response.put("time", msg.time);
        ws.tell(response, self());
    }

    static public class RepoMessage{
        public final RepositoryProfile rp;

        public RepoMessage(RepositoryProfile rp){
            this.rp = rp;
        }

        public String getRepoName(){
            return this.rp.getName();
        }

        public String getRepoDescription(){
            return this.rp.getDescription();
        }
    }

    private void sendRepoMessage(RepoMessage rp){
        final ObjectNode response = Json.newObject();
        response.put("name", rp.getRepoName());
        response.put("description", rp.getRepoDescription());
        ws.tell(response, self());

    }
}
