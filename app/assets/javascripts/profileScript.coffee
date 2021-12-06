$ ->
  ws = new WebSocket $("body").data("ws-url")
  ws.onmessage = (event) ->
    message = JSON.parse event.data
    $('#id').html(message.id)
    $('#name').html(message.name)
    $('#company').html(message.company)
    $('#blog').html(message.blog)
    $('#location').html(message.location)
    $('#email').html(message.email)
    $('#bio').html(message.bio)
    $('#twitter_username').html(message.twitter_username)
    $('#followers').html(message.followers)
    $('#following').html(message.following)

    user = message.id
    repoList = message.repository.split(',')
    repo = ""
    if repoList.length > 0
      for r of repoList
        if r == 0
            repoList[r] = repoList[r].slice(1)
        if r == repoList.length-1
            repoList[r] = repoList[r].slice(0, -1)
       
        repo += "<a href='/repository/" + user + "/" + repoList[r] + "'>"
      console.log(repo)
      $('#repository').html(repo)
    else
      $('#repository').html("No repository")