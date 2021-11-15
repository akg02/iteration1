package services;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import models.GithubClient;
import models.Issue;

/**
 * Provides method to work with Issues of a github repository
 * @author Meet Mehta
 * @version 1 
 *
 */
public class IssueService {

	private final GithubClient github;

	@Inject
	public IssueService(GithubClient github) {
		this.github = github;
	}

	/**
	 * Method to get word-level statistics of issue title for given repository
	 * @author Meet Mehta
	 * @param user username of the repository
	 * @param repo repository name 
	 * @return CompletionStage object of Map having count of each word in issue titles sorted in descending order
	 */
	public CompletionStage<Map<String, Integer>> getIssueStatistics(String user, String repo) {
		CompletionStage<List<Issue>> issues = this.getIssues(user, repo);
		
		CompletionStage<List<String>> titles = issues.thenApplyAsync(issue -> issue.stream()
				.map(s -> s.getTitle().split(" ")).flatMap(Arrays::stream).collect(Collectors.toList()));
		CompletionStage<Map<String, Integer>> issueStatistics = titles
				.thenApply(title -> title.parallelStream().collect(Collectors.toMap(w -> w, w -> 1, Integer::sum)));
		issueStatistics = issueStatistics.thenApply(statistics -> statistics.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)));
		return issueStatistics;
	}

	/**
	 * Method to fetch issues
	 * @author Meet Mehta
	 * @param user username of the repository
	 * @param repo repository name
	 * @return CompletionStage of list of issues
	 */
	public CompletionStage<List<Issue>> getIssues(String user, String repo) {
		return github.getIssues(user, repo);

	}
}
