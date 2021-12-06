package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;
//import sun.java2d.cmm.ProfileDeferralInfo;
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
        context().actorSelection("/user/RepoActor_"+id)
                .tell(new RepositoryActor.RegisterMsg(), self());
        context().actorSelection("/user/issueStatisticsActor"+id).tell(new IssueStatisticsActor.RegisterMsg(), self());
        context().actorSelection("/user/userProfileActor_"+id)
        		.tell(new UserProfileActor.RegisterMsg(), self());

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TimeMessage.class, this::sendTime)
                .match(CommitMessage.class, this::sendCommitMessage)
                .match(RepoMessage.class, this::sendRepoMessage)
                .match(IssueStatisticsMessage.class, this::sendIssueStatisticsMessage)
                .match(ProfileMessage.class, this::sendProfileMessage)
                .build();
    }

    static public class CommitMessage {
        public final ArrayList<Map<String,Integer>> list;
        public CommitMessage(ArrayList<Map<String, Integer>> list){
            this.list = list;
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
        public final RepositoryProfile repoProfile;

        public RepoMessage(RepositoryProfile repoProfile){
            this.repoProfile = repoProfile;
        }


    }

    private void sendRepoMessage(RepoMessage rm){
        final ObjectNode response = Json.newObject();
        System.out.println(rm.repoProfile.getName() + "  " + rm.repoProfile.getDescription());
        response.put("name", rm.repoProfile.getName());
        response.put("description", rm.repoProfile.getDescription());
        response.put("starC", rm.repoProfile.getStargazers_count());
        response.put("forkC", rm.repoProfile.getForks_count());
        response.put("topic", rm.repoProfile.getTopics().toString());
        response.put("createDate", rm.repoProfile.getCreated_at().toString());
        response.put("lastUpDate", rm.repoProfile.getUpdated_at().toString());

        ArrayList<Map<String, String>> tempIL = new ArrayList<>();
        List<Issue> il = rm.repoProfile.getIssues();
        for(Issue i: il){
            Map<String, String> temp = new HashMap<>();
            temp.put("issueTitle", i.getTitle());
            temp.put("issueBody", i.getBody());
            temp.put("issueNumber", i.getNumber());
            temp.put("issueLabels", i.getLabelNames().toString());

            tempIL.add(temp);
        }

        response.put("issueList", tempIL.toString());
        ws.tell(response, self());

    }
    
    static public class ProfileMessage {
    	public final ProfileInfo profInfo;
    	
    	public ProfileMessage(ProfileInfo profInfo) {
    		this.profInfo = profInfo;
    	}
    }
    
    private void sendProfileMessage(ProfileMessage msg) {
    	final ObjectNode response = Json.newObject();
    	response.put("id", msg.profInfo.getLogin());
    	response.put("name", msg.profInfo.getName());
    	response.put("company", msg.profInfo.getCompany());
    	response.put("blog", msg.profInfo.getBlog());
    	response.put("location", msg.profInfo.getLocation());
    	response.put("email", msg.profInfo.getEmail());
    	response.put("bio", msg.profInfo.getBio());
    	response.put("twitter_username", msg.profInfo.getTwitter());
    	response.put("followers", msg.profInfo.getFollowers());
    	response.put("following", msg.profInfo.getFollowing());
    	response.put("repository", msg.profInfo.getRepos().toString());
    	
    	ws.tell(response, self());
    	
    }
}
