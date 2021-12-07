package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import models.*;
import java.util.*;

public class UserActor extends AbstractActor {
    private final ActorRef ws;
    private String id;

    public UserActor(ActorRef ws, String id) {
        this.ws = ws;
        this.id = id;
        Logger.debug("New User Actor {} for websocket {}; timeActor {}",self(), ws);
    }

    static public Props props(ActorRef ws, String id){
        return Props.create(UserActor.class, ws, id);
    }

    @Override
    public void preStart() throws Exception {
//        context().actorSelection("/user/timeActor")
//                .tell(new TimeActor.RegisterMsg(), self());
        context().actorSelection("/user/commitActor"+id)
                .tell(new CommitActor.RegisterMsg(), self());
        context().actorSelection("/user/userProfileActor"+id)
                .tell(new UserDataActor.RegisterMsg(), self());
        context().actorSelection("/user/myrepoActor_"+id)
                .tell(new RepositoryActor.RegisterMsg(), self());
        context().actorSelection("/user/issueStatisticsActor"+id).tell(new IssueStatisticsActor.RegisterMsg(), self());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TimeMessage.class, this::sendTime)
                .match(CommitMessage.class, this::sendCommitMessage)
                .match(RepoMessage.class, this::sendRepoMessage)
                .match(IssueStatisticsMessage.class, this::sendIssueStatisticsMessage)
                .match(UserProfileMessage.class, this::sendUserProfileInfo)
                .build();
    }

    static public class CommitMessage {
        public final ArrayList<Map<String,Integer>> list;
        public CommitMessage(ArrayList<Map<String, Integer>> list){
            this.list = list;
        }
    }

    static public class UserProfileMessage{
        public final ProfileInfo profileInfo;
        public UserProfileMessage(ProfileInfo profileInfo){
            this.profileInfo = profileInfo;
        }
    }
    
    static public class IssueStatisticsMessage {
    	public final Map<String,Integer> message;
    	public IssueStatisticsMessage(Map<String,Integer> message) {
    		this.message = message;
    	}
    }

    private void sendCommitMessage(CommitMessage msg) {
        final ObjectNode response = Json.newObject();
        response.put("maxAddition", msg.list.get(0).get("maxAddition"));
        response.put("minAddition", msg.list.get(1).get("minAddition"));
        response.put("avgAddition", msg.list.get(2).get("avgAddition"));
        response.put("maxDeletion", msg.list.get(3).get("maxDeletion"));
        response.put("minDeletion", msg.list.get(4).get("minDeletion"));
        response.put("avgDeletion", msg.list.get(5).get("avgDeletion"));

        response.put("topCommitters", msg.list.get(6).toString());
//        response.put("list", msg.list.toString());
        ws.tell(response, self());
    }
    
    private void sendIssueStatisticsMessage(IssueStatisticsMessage message) {
        final ObjectNode response = Json.newObject();
        response.put("message", message.message.toString());
    	ws.tell(response, self());
    }

    private void sendUserProfileInfo(UserProfileMessage message) {
        final ObjectNode response = Json.newObject();
        response.put("userName", message.profileInfo.getName());
        response.put("bio", message.profileInfo.getBio());
        response.put("repos", message.profileInfo.getRepos().toString());
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
