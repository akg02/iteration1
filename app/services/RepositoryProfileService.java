package services;


import models.GithubClient;
import models.RepositoryProfile;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This is the service file to get the repository details along with latest 20 issues of the given repository and username </br>
 * Both API calls, for getting issues and getting repository details, is handled by this service
 *
 * @author Sagar Sanghani
 * @version 1
 */
@Singleton
public class RepositoryProfileService {
    private static GithubClient github = null;


    private static final RepositoryProfileService instance = new RepositoryProfileService(github);
    /**
     * This is a Parameterised Constructor
     * @param github object of GithubClient, used for making API calls
     */
    @Inject
    public RepositoryProfileService(GithubClient github){
        this.github = github;
    }

    /**
     * This method first calls getIssues method to get list of issues of the repository </br>
     * Then apply asynchronously a stream operation to get the first 20 issues from the issue list <br>
     * Then using this issue list along with user and repo call the getRepositoryDetails to fetch the Repository details </br>
     * Everything is done Asynchronously, i.e, there are no blocking calls.
     *
     * @param user github username of the owner of the repository
     * @param repo repository name
     * @return A CompletionStage object of type RepositoryProfile model which contains all the details of the Repository
     */
    public CompletionStage<RepositoryProfile> getRepoDetails(String user, String repo){
        CompletionStage<RepositoryProfile> repoProfile = github.getIssues(user, repo)
                .thenApply(il -> il.stream().limit(20).collect(Collectors.toList()))
                .thenCompose(il2 -> github.getRepositoryDetails(user, repo, il2));

        return repoProfile;
    }

    public static RepositoryProfileService getInstance(){
        return instance;
    }

}
