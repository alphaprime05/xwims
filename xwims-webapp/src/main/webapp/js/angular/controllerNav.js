var myApp = angular.module('myApp', ['ngCookies']).config(function ($httpProvider) {
    delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
});




myApp.controller('NavCtrl', function ($scope, $rootScope) {
        
});
