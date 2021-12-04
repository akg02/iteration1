$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    $('#list').html message.list + "<br/>"
    $('#maxAddition').html message.maxAddition + "<br/>"
    $('#minAddition').html message.minAddition + "<br/>"
    $('#avgAddition').html message.avgAddition + "<br/>"
    $('#maxDeletion').html message.maxDeletion + "<br/>"
    $('#minDeletion').html message.minDeletion + "<br/>"
    $('#avgDeletion').html message.avgDeletion + "<br/>"

#     parsedMessage = JSON.parse message.topCommitters
    data = ''
    committers = message.topCommitters.split(',')
    for i of committers

        committers[i] = committers[i].split("=")
        if i == '0'
            committers[i][0] = committers[i][0].slice(1)
            console.log(committers[i][0])
        if i == JSON.stringify(committers.length-1)
            committers[i][1] = committers[i][1].slice(0,-1)


        data += "<a href='/profile/"+ (committers[i][0]) + "'>"
        data += committers[i][0]  + "</a> Count:"
        data += (committers[i][1]) + "<br/>"
        console.log(data)
    $('#top10committers').html data
