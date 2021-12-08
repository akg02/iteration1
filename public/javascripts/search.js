var last_version = -1;
function display_search_history(search_results) {
    const view = $("#results");
    view.empty(); // clear the current display and replace with the new results
    for (let result of search_results) {
        view.append(search_result_to_html("Search terms", result));
    }
}

// Called when the page is loaded
$(document).ready(() => {
    const url = $("body").data("ws-url");
    const ws = new WebSocket(url);
    ws.onerror = () => {
        alert("Websocket has failed! Reload to the page to continue");
    };
    ws.onmessage = event => {
        console.log("receive new results " + event.data)
        const search_history = JSON.parse(event.data);
        display_search_history(search_history);
    };
    $("#search-btn").click(() => {
        const input = $("#search-input");
        const query = input.val();
        if (query) {
            input.val('');  // clear the search box
            ws.send(query);
        }
    });
});