var myApp = angular.module('myApp', []).config(function ( $httpProvider) {        
        delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
    });

myApp.controller('userListCtrl', function ($scope, $http) {
  $http.get('http://localhost:8080/xwims-webservice/users')
      .then(function(response) {
      console.log(response.data);
    $scope.members = response.data;
      

  });
    

});



myApp.controller('customersCtrl', function($scope, $http) {
    $http.get("http://www.w3schools.com/angular/customers.php")
    .then(function (response) {
        $scope.names = response.data.records;
                              console.log($scope.names);
    });
});

