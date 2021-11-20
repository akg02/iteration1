package models;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * @author Sagar Sanghani
 * @version 1
 */
public class RepositoryProfile {
    public List<String> topics;
    public String name;
    public String description;
    public Date created_at;
    public Date updated_at;
    public int stargazers_count;
    public List<Issue> issues;
    public int forks_count;


    public RepositoryProfile(){
    }


    public RepositoryProfile(String name, String description, Date created_at, Date updated_at, int stargazers_count, List<Issue> issues, List<String> topics, int forks_count){
        this.name = name;
        this.description = description;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.stargazers_count = stargazers_count;
        this.topics = topics;
        this.issues = issues;
        this.forks_count = forks_count;
    }

}
