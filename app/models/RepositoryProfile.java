package models;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.CompletionStage;

public class RepositoryProfile {
    private List<String> issues;
    private String name;
    private String description;
    private String owner;
    private Date createdDate;
    private Date lastUpdated;
    public RepositoryProfile(){
    }
}
