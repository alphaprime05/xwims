/*jslint plusplus: true */
var myApp = angular.module('myApp', ['ui.bootstrap', 'ngCookies', 'ngStorage']).config(function ($httpProvider) {
    delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
}).filter('trustAsResourceUrl', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsResourceUrl(val);
    };
}]);




myApp.controller('WorksheetCtrl', function ($scope, $http, $window, $cookies, $sessionStorage) {
    $http.defaults.headers.common['Content-Language'] = 'fr';

    $scope.Math = Math;
    $scope.cookies = [];
    $scope.cookies.statusUser = $cookies.get('statusUser');
    $scope.cookies.idUser = $cookies.get('idUser');
    $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
    $scope.isUserCertified = ($scope.cookies.statusUser == 'ROOT' || $scope.cookies.statusUser == 'CERTIFIED');
    $scope.connected = $cookies.get('connected');

    $scope.worksheet = {};

    if (!$.isEmptyObject($sessionStorage.worksheet))
        $scope.worksheet.exercises = $sessionStorage.worksheet;
    else
        $scope.worksheet.exercises = [];


    $scope.worksheet.title = "";
    $scope.worksheet.description = "";

    function isEmpty(str) {
        return (!str || 0 === str.length);
    }
    
    





    //Affichage du score sous forme de chapeaux
    var exercises = $scope.worksheet.exercises;
    if (exercises) {

        for (i = 0; i < exercises.length; i++) {
            var ex = exercises[i];
            if (ex != null && ex.score != 0) {
                ex.hats = [];
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
    $scope.worksheet.exercises = exercises;

    console.log($scope.worksheet.exercises);






    $scope.deleteFromWorksheet = function (id) {
        var i = functiontofindIndexByKeyValue($scope.worksheet.exercises, "id", id);
        if (i != -1) {
            $scope.worksheet.exercises.splice(i, 1);
        }
        $sessionStorage.worksheet = $scope.worksheet.exercises;
    }

    //Transformation du tableau des exercices dans l'ordre en objet JSON
    function transformWorksheetForRequest() {
        var exercises = [];
        for (var i = 0; i < $scope.worksheet.exercises.length; i++) {
            exercises.push({
                id_exercise: parseInt($scope.toArray()[i])
            });
        }
        return exercises;

    }

    $scope.generateWorksheet = function () {
        var exercises = transformWorksheetForRequest();

        console.log($scope.toArray());
        console.log(exercises);

        $scope.data = {
            title: $scope.worksheet.title,
            description: $scope.worksheet.description,
            exercices_param: exercises

        };


        $window.sessionStorage.setItem("worksheetToGenerate",  JSON.stringify($scope.data));

        if ($scope.connected) {
            $http.post(urlRestGlobal + '/worksheet/save', $scope.data).then(function (response) {

                $window.location.href = "worksheetGenerated.html";
            });
        }
        else
                $window.location.href = "worksheetGenerated.html";        

    };

    $scope.saveWorksheet = function () {
        var exercises = [];
        for (var i = 0; i < $scope.worksheet.exercises.length; i++) {
            exercises.push({
                id_exercise: parseInt($scope.toArray()[i])
            });
        }

        $scope.data = {
            title: $scope.worksheet.title,
            description: $scope.worksheet.description,
            exercices_param: exercises

        };


        $http.post(urlRestGlobal + '/worksheet/save', $scope.data, {
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(function (response) {
            $scope.messageInfo = "Votre feuille a bien été sauvergardée !";
            $('.alert').fadeOut('fast');
            $('.alert').fadeIn('fast');
        });

    };

    
    $scope.clearWorksheet = function () {
        delete $sessionStorage.worksheetToGenerate;
        delete $sessionStorage.worksheet;
        $scope.worksheet = {};
    }



}).directive('sortable', function () {
    //Directive pour obtenir la liste des exercices dans l'ordre du sortable
    return {
        link: function (scope, element) {
            element.sortable();
            scope.toArray = function () {
                return element.sortable("toArray");
            }
            scope.serialize = function () {
                return element.sortable("serialize", {
                    key: "ex"
                });
            }
        }
    }
});

