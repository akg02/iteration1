package services;

import models.GithubClient;
import models.Repository;
import models.ProfileInfo;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;


public class ProfileInfoService {
	private final GithubClient github;
    private final Map<String, ProfileInfo> info;

    @Inject
    public ProfileInfoService(GithubClient github){
        this.info = new HashMap<>();
        this.github = github;
    }

    public CompletionStage<ProfileInfo> getRepoList(String user) {
    	
    	CompletionStage<ProfileInfo> pInfo = github.getAllRepoList(user)
    			.thenApply(i -> i.stream().collect(Collectors.toList()))
    			.thenCompose(i2 -> github.displayUserProfile(user, i2));
    			
//        List<String> repoList = github.getAllRepoList(user).toCompletableFuture().get();
//        CompletionStage<ProfileInfo> pInfo = github.getUserProfile(user, repoList);
        return pInfo;
    }

}
