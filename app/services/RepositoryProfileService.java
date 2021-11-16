package services;


import models.GithubClient;
import models.RepositoryProfile;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * @author Sagar Sanghani
 * @version 1
 */
public class RepositoryProfileService {
    private final GithubClient github;
    private final Map<String, RepositoryProfile> sessions;

    @Inject
    public RepositoryProfileService(GithubClient github){
        this.sessions = new HashMap<>();
        this.github = github;
    }

}
