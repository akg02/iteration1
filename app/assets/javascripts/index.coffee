$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    $('#name').html(message.name + "<br/>")
    $('#desc').html(message.description + "<br/>")