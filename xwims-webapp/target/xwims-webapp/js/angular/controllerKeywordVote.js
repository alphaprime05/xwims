myApp.controller('addKeywordCtrl', function ($scope, $uibModal, $log, $http,$cookies) {
    $http.defaults.headers.common['Content-Language'] = 'fr';
	if($cookies.get('btoaUser'))
    		$http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
else
	$http.defaults.headers.common['Authorization'] = null;

$scope.exercise = {};
    var paramIdExercise = getParameterByName('id');
     $http.get(urlRestGlobal + '/exercises/'+paramIdExercise+'/keywords').then(function (response) {
console.log("keyword");
                       console.log(response.data);
            $scope.exercise.keywords = response.data;
                    });

    $scope.openKeywordModal = function () {

        var modalInstance = $uibModal.open({
            animation: false,
            templateUrl: 'keywordModalContent.html',
            controller: 'addKeywordInstanceCtrl',
            resolve: {
                keywords: function () {
                    return $scope.exercise.keywords;
                }
            }
        });

        /*modalInstance.result.then(function (selectedItem) {

            $scope.selected = selectedItem;
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
        });*/


    };
    
    $scope.addVote = function(nameKeyword){
        var data = {
            id_exercise: paramIdExercise,
            found_step: 1,
            keyword: nameKeyword,
            keyword_en: ""
        }

        $http.post(urlRestGlobal + '/keywords/addVote', data);
    }



});

// Please note that $uibModalInstance represents a modal window (instance) dependency.
// It is not the same as the $uibModal service used above.

myApp.controller('addKeywordInstanceCtrl', function ($scope, $uibModalInstance, $http,keywords, $cookies) {
    $scope.exercise = {};
    $scope.exercise.keywords = keywords;
    var paramIdExercise = getParameterByName('id');
    $http.defaults.headers.common['Content-Language'] = 'fr';
    $http.defaults.headers.post['Content-Type'] =  'application/json';
    $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
    
    
    $scope.categoryAdded = false;
    $scope.asyncSelected = "";
    $scope.asyncSelectedTrad = "";
    $scope.stepAddKeyword = 1;
    
    
    $scope.ok = function (step) {
        switch (step) {
            case 1:
                console.log("async" + $scope.asyncSelected);
                console.log($scope.autocomplete);
                var found = $.inArray($scope.asyncSelected, $scope.autocomplete) > -1;
                if (found) {
                    $scope.stepAddKeyword = 1;
                    var data = {
                        id_exercise: paramIdExercise,
                        found_step: 1,
                        keyword: $scope.asyncSelected,
                        keyword_en: ""
                    }

                    $http.post(urlRestGlobal + '/keywords/addVote', data).then(function (response) {
                        $uibModalInstance.close($scope.asyncSelected);
                    });
                } else {
                    $scope.stepAddKeyword = 2;
                }
                break;
            case 2:
                console.log($scope.asyncSelectedTrad);
                console.log($scope.autocompleteTrad);
                var foundTrad = $.inArray($scope.asyncSelectedTrad, $scope.autocompleteTrad) > -1;
                if (foundTrad) {
                    console.log("foundTrad" + foundTrad);
                    var data = {
                        id_exercise: paramIdExercise,
                        found_step: 2,
                        keyword: $scope.asyncSelected,
                        keyword_en: $scope.asyncSelectedTrad
                    }
                    $http.post(urlRestGlobal + '/keywords/addVote', data).then(function (response) {
                        console.log(response.status);
                        if (response.status == 400)
                            console.log(response.data.info);
                        else {
                            $scope.categoryAdded = true;
                        }
                        $uibModalInstance.close($scope.asyncSelectedTrad);
                    });
                } else {
                    $scope.stepAddKeyword = 3;
                }
                break;
            case 3:
                console.log("step3");
                var data = {
                    id_exercise: paramIdExercise,
                    found_step: 3,
                    keyword: $scope.asyncSelected,
                    keyword_en: $scope.asyncSelectedTrad
                }
                $http.post(urlRestGlobal + '/keywords/create', data).then(function (response) {
                    $uibModalInstance.close($scope.asyncSelectedTrad);
                });
                break;
        }


    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };

console.log(keywords);


    $scope.autocomplete = [];
    // Any function returning a promise object can be used to load values asynchronously
    $scope.getCategories = function (val) {
        return $http.get(urlRestGlobal + '/keywords/autocompleteUserLang?keyword=' + val).then(function (response) {
            $scope.autocomplete = response.data;
            return $scope.autocomplete;
        });
    };

    $scope.autocompleteTrad = [];
    // Any function returning a promise object can be used to load values asynchronously
    $scope.getCategoriesTrad = function (val) {
        console.log("trad" + val);
        return $http.get(urlRestGlobal + '/keywords/autocomplete?keyword=' + val).then(function (response) {
            $scope.autocompleteTrad = response.data;
            return $scope.autocompleteTrad;
        });
    };


    var _selected;
    $scope.ngModelOptionsSelected = function (value) {
        if (arguments.length) {
            _selected = value;
        } else {
            return _selected;
        }
    };

    $scope.modelOptions = {
        debounce: {
            default: 0,
            blur: 250
        },
        getterSetter: true
    };

});
