myApp.controller('ModalDemoCtrl', function ($scope, $uibModal, $log, $http,$cookies) {
    $http.defaults.headers.common['Content-Language'] = 'fr';
	if($cookies.get('btoaUser'))
    		$http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
else
	$http.defaults.headers.common['Authorization'] = null;
   $scope.exercise = {};
    var paramIdExercise = getParameterByName('id');
     $http.get(urlRestGlobal + '/exercises/'+paramIdExercise+'/categories').then(function (response) {
                       console.log(response.data);
            $scope.exercise.categories = response.data;
        });

    $scope.openCategoryModal = function () {

        var modalInstance = $uibModal.open({
            animation: false,
            templateUrl: 'categoryModalContent.html',
            controller: 'ModalInstanceCtrl',
            resolve: {
                categories: function () {
                    return $scope.exercise.categories;
                }
            }
        });

        /*modalInstance.result.then(function (selectedItem) {

            $scope.selected = selectedItem;
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
        });*/


    };
    
    $scope.addVote = function(nameCat){
        var data = {
            id_exercise: paramIdExercise,
            found_step: 1,
            keyword: nameCat,
            keyword_en: ""
        }

        $http.post(urlRestGlobal + '/categories/addVote', data);
    }



});

// Please note that $uibModalInstance represents a modal window (instance) dependency.
// It is not the same as the $uibModal service used above.

myApp.controller('ModalInstanceCtrl', function ($scope, $uibModalInstance, $http, categories, $cookies) {
    $scope.exercise = {};
    $scope.exercise.categories = categories;
    var paramIdExercise = getParameterByName('id');
    $http.defaults.headers.common['Content-Language'] = 'fr';
    $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');

    $scope.categoryAdded = false;
    $scope.asyncSelected = "";
    $scope.asyncSelectedTrad = "";
    $scope.stepAddCategory = 1;
    
    
    
    $scope.ok = function (step) {
        switch (step) {
            case 1:
                console.log("async" + $scope.asyncSelected);
                var found = $.inArray($scope.asyncSelected, $scope.autocomplete) > -1;
                if (found) {
                    console.log($scope.asyncSelected);
                    $scope.stepAddCategory = 1;
                    var data = {
                        id_exercise: paramIdExercise,
                        found_step: 1,
                        category: $scope.asyncSelected,
                        category_en: "",
                        id_parent_category: -1
                    }

                    $http.post(urlRestGlobal + '/categories/addVote', data).then(function (response) {
                        $uibModalInstance.close($scope.asyncSelected);
                    });
                } else {
                    $scope.stepAddCategory = 2;
                }
                break;
            case 2:
                console.log($scope.asyncSelectedTrad);
                console.log($scope.autocompleteTrad);
                var foundTrad = $.inArray($scope.asyncSelectedTrad, $scope.autocompleteTrad) > -1;
                if (foundTrad) {
                    console.log($scope.asyncSelectedTrad);
                    console.log("foundTrad" + foundTrad);
                    var data = {
                        id_exercise: paramIdExercise,
                        found_step: 2,
                        category: $scope.asyncSelected,
                        category_en: $scope.asyncSelectedTrad,
                        id_parent_category: -1
                    }
                    $http.post(urlRestGlobal + '/categories/addVote', data).then(function (response) {
                        console.log(response.status);
                        if (response.status == 400)
                            console.log(response.data.info);
                        else {
                            $scope.categoryAdded = true;
                        }
                        $uibModalInstance.close($scope.asyncSelectedTrad);
                    });
                } else {
                    $scope.stepAddCategory = 3;
                }
                break;
            case 3:
                console.log("step3");
                if(treeIdCategories.length > 0)
                    var prevCatId = treeIdCategories[treeIdCategories.length - 1];
                else
                    var prevCatId = -1;

                    var data = {
                        id_exercise: paramIdExercise,
                        found_step: 3,
                        category: $scope.asyncSelected,
                        category_en: $scope.asyncSelectedTrad,
                        id_parent_category: prevCatId
                    }
                $http.post(urlRestGlobal + '/categories/create', data).then(function (response) {
                    console.log(response.status);
                    if (response.status == 400)
                        console.log(response.data.info);
                    else {
                        $scope.categoryAdded = true;
                    }
                    $uibModalInstance.close();
                });
                break;
        }
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
    
    $http.get(urlRestGlobal + '/categories')
        .then(function (response) {
            $scope.categories = response.data;

        });
    var treeIdCategories = [];

    $scope.deepTree = function (cat) {
        treeIdCategories.push(cat.id);
        console.log(treeIdCategories);
        $http.get(urlRestGlobal + '/categories/' + cat.id + '/subcategories')
            .then(function (response) {
                $scope.categories = response.data;

            });
    };


    $scope.backTree = function () {
        treeIdCategories.pop();
        console.log(treeIdCategories);
        if (treeIdCategories.length > 0) {
            var prevCatId = treeIdCategories[treeIdCategories.length - 1];
            $http.get(urlRestGlobal + '/categories/' + prevCatId + '/subcategories')
                .then(function (response) {
                    $scope.categories = response.data;

                });
        } else {
            $http.get(urlRestGlobal + '/categories')
                .then(function (response) {
                    $scope.categories = response.data;

                });
        }
    }

    $scope.autocomplete = [];
    // Any function returning a promise object can be used to load values asynchronously
    $scope.getCategories = function (val) {
        return $http.get(urlRestGlobal + '/categories/autocompleteUserLang?category=' + val).then(function (response) {
            $scope.autocomplete = response.data;
            return $scope.autocomplete;
        });
    };

    $scope.autocompleteTrad = [];
    // Any function returning a promise object can be used to load values asynchronously
    $scope.getCategoriesTrad = function (val) {
        return $http.get(urlRestGlobal + '/categories/autocomplete?category=' + val).then(function (response) {
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
