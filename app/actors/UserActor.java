package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.RepositoryProfile;
import play.libs.Json;

public class UserActor extends AbstractActor {
    private final ActorRef ws;
    public String id;

    public UserActor(final ActorRef wsOut, String id) {
        ws =  wsOut;
        this.id = id;
    }

    static public Props props(final ActorRef wsOut, String id) {
        return Props.create(UserActor.class, wsOut, id);
    }

    @Override
    public void preStart() {
        context().actorSelection("/user/timeActor")
                .tell(new TimeActor.RegisterMsg(), self());
        context().actorSelection("/user/myrepoActor_"+id)
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
        public String name;
        public String desc;
        public int star_count;

        public RepoMessage(RepositoryProfile rp){
            this.rp = rp;
            this.name = rp.getName();
            this.desc = rp.getDescription();
            this.star_count = rp.getStargazers_count();
        }


    }

    private void sendRepoMessage(RepoMessage rp){
        final ObjectNode response = Json.newObject();
        System.out.println(rp.name + "  " + rp.desc);
        response.put("name", rp.name);
        response.put("description", rp.desc);
        response.put("starC", rp.star_count);
        ws.tell(response, self());

    }
}
