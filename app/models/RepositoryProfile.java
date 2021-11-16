package models;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * @author Sagar Sanghani
 * @version 1
 */
public class RepositoryProfile {
    public String issues;
    public List<String> topics;
    public String name;
    public String description;
    //public String owner;
    public Date created_at;
    public Date updated_at;
    public int stargazers_count;
    //public Map<String, String> otherDetails;


    public RepositoryProfile(){
    }

    public RepositoryProfile(String name, String description, String owner, Date created_at, Date updated_at, int stargazers_count, String issues, Map<String, String> otherDetails, List<String> topics){
        this.name = name;
        this.description = description;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.stargazers_count = stargazers_count;
        this.topics = topics;
        this.issues = issues;
        //this.otherDetails = otherDetails;
    }

}