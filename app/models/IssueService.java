package models;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import com.google.inject.Inject;

public class IssueService {

	private final GithubClient github;

	@Inject
	public IssueService(GithubClient github) {
		this.github = github;
	}

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

	public CompletionStage<List<Issue>> getIssues(String user, String repo) {
		return github.getIssues(user, repo);

	}
}
