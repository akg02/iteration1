package services;

import models.GithubClient;
import models.ProfileInfo;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Service file to obtain public details of a github user
 * in addition to the list of repositories the user owns.
 * 
 * @author Joon Seung Hwang
 *
 */
public class ProfileInfoService {
	private static GithubClient github = null;
    //private final Map<String, ProfileInfo> info;
    private static final ProfileInfoService instance = new ProfileInfoService(github);

    /**
     * Parmeterized Constructor
     * @param github to make API calls
     */
    @Inject
    public ProfileInfoService(GithubClient github){
        //this.info = new HashMap<>();
        this.github = github;
    }

    /**
     * Gathers list of repositories and asynchronously calls 
     * displayUserProfile to fetch details of profile information
     * 
     * @param user
     * @return Completionstage object of type ProfileInfo 
     */
    public CompletionStage<ProfileInfo> getRepoList(String user) {
    	
    	CompletionStage<ProfileInfo> pInfo = github.getAllRepoList(user)
    			.thenApply(i -> i.stream().collect(Collectors.toList()))
    			.thenCompose(i2 -> github.displayUserProfile(user, i2));
    			
        return pInfo;
    }
    
    public static ProfileInfoService getInstance() {
    	return instance;
    }

}
