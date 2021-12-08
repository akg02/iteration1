
function repo_to_html(repo) {
    let html_topics;
    if (repo.topics) {
        html_topics = "Topics:" + repo.topics.map(t => `<a href="/topic/${t}" class="badge badge-secondary">${t}</a>`);
    } else {
        html_topics = `<span class="badge badge-pill badge-light">No topic</span>`;
    }
    return `
     <li style="margin-bottom: 10px;">
            User: <a href="/userSocket/${repo.user}">${repo.user}</a>
            /
            repository: <a href="/repoSocket/${repo.user}/${repo.name}">${repo.name}</a><br/>
            ${html_topics}
     </li>
	`;
}

function search_result_to_html(title, search_result) {
    let html_repos;
    if (search_result.items) {
        html_repos = '<ol>' + search_result.items.map(repo_to_html).join(" ") + '</ol>';
    } else {
        html_repos = `<p class=font-weight-bold">No repositories matched</p>`;
    }
    return `
	<div class="border" style="margin-top:20px;">
		<h4 class="h5" style="margin-left: 20px; margin-top:10px;"> ${title}: ${search_result.input} </h4>
		<hr class="my-1">
		${html_repos}
	</div>`;
}