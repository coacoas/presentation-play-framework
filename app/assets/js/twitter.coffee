app = angular.module 'TwitterSearch', []

app.controller 'TwitterController', ['$scope', '$http', ($scope, $http) ->
  $scope.active = false
  $scope.tweets = []

  $scope.search = (query) ->
    $http.get '/search', params: { query: query }
      .then (result) ->
        $scope.tweets = result.data

  $scope.addMsg = (msg) ->
    console.log "Received: " + msg.data
    $scope.$apply($scope.tweets?.push(JSON.parse(msg.data)))
    if (msg.id == "CLOSE")
      $scope.tweetFeed.close

  $scope.listen = () ->
    if ($scope.active)
      console.log("Closing feed")
      $scope.tweetFeed?.close()
      $scope.active = false
    else
      if ($scope.query)
        console.log "Creating event source connection for " + encodeURI($scope.query)
        $scope.tweetFeed = new EventSource('/stream/' + encodeURI($scope.query))
        $scope.tweetFeed.onmessage = $scope.addMsg
        $scope.tweetFeed.onerror = () ->
          $scope.tweetFeed?.close()
          $scope.active = true
        $scope.active = true
      else
        alert('A search term must be specified')
]
