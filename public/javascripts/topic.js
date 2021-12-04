function display_search_result(search_result) {
    const view = $("#results");
    view.empty(); // clear the current display and replace with the new results
    view.append(search_result_to_html("Search Topic", search_result));
}

$(document).ready(() => {
    const ws = new WebSocket($("body").data("ws-url"));
    ws.onmessage = event => {
        const results = JSON.parse(event.data);
        display_search_result(results);
    };
    ws.onopen = () => {
        // Immediately send the topic request after the connection is open
        const topic = $("#topic").val();
        ws.send(topic);
    };
});