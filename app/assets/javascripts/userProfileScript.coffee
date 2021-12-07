$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    $('#userName').html message.userName + "<br/>"
    $('#bio').html message.bio + "<br/>"

    data = ''
    repos = message.repos.split(',')
    for i of repos
        if i == '0'
            repos[i] = repos[i].slice(1)
        if i == JSON.stringify(repos.length-1)
            repos[i] = repos[i].slice(0,-1)


        data += "<a href='/repo/"+ (repos[i]) + "'>"
        data += repos[i] + "<br/>"
    console.log(data)
    $('#repos').html data
