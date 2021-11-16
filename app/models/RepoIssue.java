package models;

import java.util.List;

public class RepoIssue {

    public String title;
    public List<String> labels;
    public int number;
    public String state;

    public RepoIssue(){
    }

    public RepoIssue(String title, String state, List<String> labels, int number){
        this.title = title;
        this.state = state;
        this.labels = labels;
        this.number = number;
    }
}
