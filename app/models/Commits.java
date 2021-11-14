package models;

import java.util.*;

public class Commits {
    private List<CommitStats> commits;

    public Commits() {
    }

    public Commits(List<CommitStats> commits) {
        this.commits = commits;
    }

    public List<CommitStats> getCommits() {
        return commits;
    }

    public void setCommits(List<CommitStats> commits) {
        this.commits = commits;
    }

    @Override
    public String toString() {
        return "Commits{" +
                "commits=" + commits +
                '}';
    }
}
