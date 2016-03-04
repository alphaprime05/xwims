var myApp = angular.module('myApp', ['ngCookies', 'ngStorage']).config(function ($httpProvider) {
    delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
}).filter('trustAsResourceUrl', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsResourceUrl(val);
    };
}]);




myApp.controller('profileCtrl', function ($scope, $http, $cookies, $window, $sessionStorage) {
    $http.defaults.headers.common['Content-Language'] = 'fr';
	$scope.urlWebappGlobal = urlWebappGlobal;

    console.log($cookies.getAll());
    $scope.cookies = [];
    $scope.cookies.statusUser = $cookies.get('statusUser');
    $scope.cookies.idUser = $cookies.get('idUser');
    $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
    $scope.isUserCertified = ($scope.cookies.statusUser == 'ROOT' || $scope.cookies.statusUser == 'CERTIFIED');
    $scope.connected = $cookies.get('connected');
    if(!$scope.connected)
        $window.location.href = "index.html";
        
$scope.loadWorksheets = function(){
    $http.get(urlRestGlobal + '/users/monProfil')
        .then(function (response) {

            console.log(response.data);
            $scope.user = response.data;
            $scope.user.fullname = $scope.user.lastName +" "+ $scope.user.firstName;
            $scope.user.status = $scope.cookies.statusUser;
        
        for(w = 0; w < $scope.user.worksheetObjects.length; w++){
            var exercises = $scope.user.worksheetObjects[w].exercices_param;
            if (exercises) {

                for (i = 0; i < exercises.length; i++) {
                    var ex = exercises[i];
                    if (ex != null && ex.score != 0 ) {
                        ex.hats = [];
                    if(ex.score === "NaN"){
                        for (j = 0; j < 5; j++) {
                            ex.hats.push("-gris");
                        }
                    }
                        else{
                        for (j = 0; j < Math.floor(ex.score); j++) {
                            ex.hats.push("");
                        }
                        for (j = 0; j < Math.ceil(ex.score - Math.floor(ex.score)); j++) {
                            ex.hats.push("-demi");
                        }
                        for (j = 0; j < (5 - (ex.score + Math.ceil(ex.score - Math.floor(ex.score)))); j++) {
                            ex.hats.push("-gris");
                        }
                        }
                    }
                }
            }
            $scope.user.worksheetObjects[w].exercices_param = exercises;
            }
        });
}
    $scope.loadWorksheets();
    
    
    $scope.deconnect = function(){
        $cookies.remove('statusUser');
        $cookies.remove('idUser');
        $cookies.remove('connected');
        $cookies.remove('btoaUser');
        delete $sessionStorage.worksheet;
        delete $sessionStorage.worksheetToGenerate;
    console.log($cookies.getAll());
        $window.location.href = "index.html";
    }
    
    $scope.deleteWorksheet = function (id) {
         $http.delete(urlRestGlobal + '/worksheet/'+id)
        .then(function (response) {
             $scope.loadWorksheets();
         });
    }
    
    
    

});
