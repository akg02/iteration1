$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    $('#name').html(message.name)
    $('#desc').html(message.description)
    $('#stars').html(message.starC)
    $('#forks').html(message.forkC)
    $('#createAt').html(message.createDate)
    $('#lastupdate').html(message.lastUpDate)

    topicList = message.topic.split(',')
    topicMess = ""
    if topicList.length > 0
      for t of topicList
        if t == 0 and t == JSON.stringify(topicList.length-1)
          topicList[t] = topicList[t].slice(1, -1)

        if t == 0
          topicList[t] = topicList[t].slice(1)

        if t == JSON.stringify(topicList.length-1)
           topicList[t] = topicList[t].slice(0, -1)

        topicMess += "<a href='/topic/" + topicList[t] + "'>"
        topicMess += "<Strong>"+topicList[t]+"</Strong>   "

      console.log(topicMess)
      $('#topics').html(topicMess)
    else
      $('#topics').html("No Topics")

    issueList2 = message.issueList.split(",")
    if issueList2.length > 0
      for i in issueList2
        il = i.split(",")
        for j in il
          $('#issues').append j + "<br/>"
        $('#issues'). append +"<br/>"
    else
      $('#issues').html("No Issues")



