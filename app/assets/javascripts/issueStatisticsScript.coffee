$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data

    data = ''
    issues = message.message.split(',')
    for i of issues
        issues[i] = issues[i].split("=")
        if i == '0'
            issues[i][0] = issues[i][0].slice(1)
            console.log(issues[i][0])
        if i == JSON.stringify(issues.length-1)
            issues[i][1] = issues[i][1].slice(0,-1)

        data += (issues[i][0])
        data += " Count:"
        data += (issues[i][1]) + "<br/>"
        console.log(data)
    $('#issueData').html data
