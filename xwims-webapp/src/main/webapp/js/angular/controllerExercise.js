/*jslint plusplus: true */
var myApp = angular.module('myApp', ['ngCookies', 'ui.bootstrap', 'ngAnimate','ngStorage']).config(function ($httpProvider) {
    delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
}).filter('trustAsResourceUrl', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsResourceUrl(val);
    };
}]);




myApp.controller('ExerciseCtrl', function ($scope, $http, $window, $cookies, $sessionStorage) {
    $http.defaults.headers.common['Content-Language'] = 'fr';
    
    $scope.Math = Math;

    $scope.cookies = [];
    $scope.cookies.statusUser = $cookies.get('statusUser');
    $scope.cookies.idUser = $cookies.get('idUser');
	if($cookies.get('btoaUser'))
    		$http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
else
	$http.defaults.headers.common['Authorization'] = null;
    $scope.isUserCertified = ($scope.cookies.statusUser == 'ROOT' || $scope.cookies.statusUser == 'CERTIFIED');
    $scope.hasVoted = false;
           $scope.min = 0;
             $scope.max = 5;
             $scope.value = 3;
    
    
    var paramIdExercise = getParameterByName('id');
    $scope.exercise = {};
    $http.get(urlRestGlobal+'/exercises/'+paramIdExercise)
        .then(function (response) {
            $scope.exercise = response.data;
        $window.document.title = "xWims - "+$scope.exercise.wimsTitle;
         $scope.exercise.hats = [];
        for (j = 0; j < Math.floor($scope.exercise.score); j++) {
            $scope.exercise.hats.push("");
        }
        for (j = 0; j < Math.ceil($scope.exercise.score - Math.floor($scope.exercise.score)); j++) {
            $scope.exercise.hats.push("-demi");
        }
        for (j = 0; j < (5 - ($scope.exercise.score + Math.ceil($scope.exercise.score - Math.floor($scope.exercise.score)))); j++) {
            $scope.exercise.hats.push("-gris");
        }
    });
    
    
    $scope.changeEquivalent = function(idEquivalent){
	$window.location.href = urlWebappGlobal+"/exercice.html?id="+idEquivalent;
}

     $http.get(urlRestGlobal + '/exercises/'+paramIdExercise+'/equivalents').then(function (response) {
console.log("equi");
                       console.log(response.data);
            $scope.exercise.equivalents = response.data;
                    });
    
    $http.get(urlRestGlobal + '/exercises/'+paramIdExercise+'/previousCategories').then(function (response) {
                       console.log(response.data);
            $scope.exercise.previousCategories = response.data;
                    });
    
    $http.get(urlRestGlobal + '/exercises/'+paramIdExercise+'/nextCategories').then(function (response) {
                       console.log(response.data);
            $scope.exercise.nextCategories = response.data;
                    });
    $scope.i=4;
    $scope.$watch('rangeScope', function() {
       console.log($scope.rangeScope);
    });
    $scope.voteScore = function(score){
	if($scope.rangeScore === undefined)
		$scope.rangeScore = 3;
        $http.post(urlRestGlobal + '/exercises/'+paramIdExercise+'/addScore?score='+score).then(function (response) {
                       console.log(response.data);
            $scope.hasVoted = true;
        });
    }
    
    
    $scope.togglePaper = function (exercise, $event) {
        if(!$sessionStorage.worksheet)
            $sessionStorage.worksheet = [];
        
        if ($event.currentTarget.alt == "Ajouter") {
            $event.currentTarget.src = "img/addedPaper.png";
            $event.currentTarget.alt = "Supprimer";

            $sessionStorage.worksheet.push(exercise);

        } else {

            $event.currentTarget.src = "img/addPaper.png";
            $event.currentTarget.alt = "Ajouter";

            var i = functiontofindIndexByKeyValue($sessionStorage.worksheet, "id", exercise.id);
            if (i != -1) {
                $sessionStorage.worksheet.splice(i, 1);
            }
        }
    }
    
    
    
    
}).directive('ngMin', function() {
			return {
				restrict: 'A',
				require: 'ngModel',
				link: function(scope, elem, attr) { elem.attr('min', attr.ngMin); }
			};
		}).directive('ngMax', function() {
			return {
				restrict: 'A',
				require: 'ngModel',
				link: function(scope, elem, attr) { elem.attr('max', attr.ngMax); }
			};
		});		


myApp.controller('TypeaheadCtrl', function($scope, $http) {
 var _selected;

  $scope.selected = undefined;
    
  // Any function returning a promise object can be used to load values asynchronously
  $scope.getLocation = function(val) {
    return $http.get(urlRestGlobal+'/categories').then(function(response){
        console.log($scope.categories);
        return $scope.categories;
    });
  };

  $scope.ngModelOptionsSelected = function(value) {
    if (arguments.length) {
      _selected = value;
    } else {
      return _selected;
    }
  };

  $scope.modelOptions = {
    debounce: {
      default: 500,
      blur: 250
    },
    getterSetter: true
  };

  
});

function functiontofindIndexByKeyValue(arraytosearch, key, valuetosearch) {
        for (var i = 0; i < arraytosearch.length; i++) {
            if (arraytosearch[i][key] == valuetosearch) {
                return i;
            }
        }
        return -1;
    }

