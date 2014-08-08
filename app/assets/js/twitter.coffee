app = angular.module 'TwitterSearch', []

app.controller 'TwitterController', ['$scope', '$http', ($scope, $http) ->
  $scope.tweets = []
  $scope.search = (query) ->
    request = $http.get '/search', params: { query: query }
    request.then (result) ->
      $scope.tweets = result.data
]
