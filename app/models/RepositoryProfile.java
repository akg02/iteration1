package models;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.CompletionStage;

/**
 * @author Sagar Sanghani
 * @version 1
 */
public class RepositoryProfile {
    private List<String> issues;
    private String name;
    private String description;
    private String owner;
    private Date createdDate;
    private Date lastUpdated;
    private int numberOfStars;


    public RepositoryProfile(){
    }
}
