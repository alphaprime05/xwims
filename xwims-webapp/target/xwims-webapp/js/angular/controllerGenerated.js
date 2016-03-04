var myApp = angular.module('myApp', ['ngCookies', 'ngStorage']).config(function ($httpProvider) {
    delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
}).filter('trustAsResourceUrl', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsResourceUrl(val);
    };
}]);




myApp.controller('WorksheetGeneratedCtrl', function ($scope, $http, $cookies, $sessionStorage, $window) {
    $http.defaults.headers.common['Content-Language'] = 'fr';
    $http.defaults.headers.common['Content-Type'] = 'application/json';
    
    $http.post(urlRestGlobal + '/worksheet/code', JSON.parse($window.sessionStorage.getItem("worksheetToGenerate")))
        .then(function (response) {
            $scope.worksheetCode = response.data;
        });

});
