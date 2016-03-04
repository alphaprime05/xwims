myApp.controller('nextCategoryModalCtrl', function ($scope, $uibModal, $log, $http,$cookies) {
    $http.defaults.headers.common['Content-Language'] = 'fr';
	if($cookies.get('btoaUser'))
    		$http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
else
	$http.defaults.headers.common['Authorization'] = null;
    var paramIdExercise = getParameterByName('id');
     $http.get(urlRestGlobal + '/exercises/'+paramIdExercise+'/categories').then(function (response) {
                       console.log(response.data);
            $scope.exercise.categories = response.data;
        });

    $scope.openNextCategoryModal = function () {

        var modalInstance = $uibModal.open({
            animation: false,
            templateUrl: 'nextCategoryModalContent.html',
            controller: 'nextCategoryModalInstanceCtrl',
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
    
     $scope.addVote = function(idNextCat){

        $http.post(urlRestGlobal + '/categories/'+idNextCat+'/addNextCategoryForExercise?exerciseId='+paramIdExercise);
    }
    

});

// Please note that $uibModalInstance represents a modal window (instance) dependency.
// It is not the same as the $uibModal service used above.

myApp.controller('nextCategoryModalInstanceCtrl', function ($scope, $uibModalInstance, $http, categories, $cookies) {
    $scope.exercise = {};
    $scope.exercise.categories = categories;
    var paramIdExercise = getParameterByName('id');
    $http.defaults.headers.common['Content-Language'] = 'fr';
    $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');

    $scope.categoryAdded = false;
    $scope.asyncSelected = "";
    $scope.asyncSelectedTrad = "";
    $scope.stepAddCategory = 1;
    
    
    
    $scope.ok = function () {
            var categoryChoosedId = treeIdCategories[treeIdCategories.length - 1];
            var categoryChoosedName = treeNameCategories[treeNameCategories.length - 1];
        console.log("categoryChoosed"+categoryChoosedName);
        

                    $http.post(urlRestGlobal + '/categories/'+categoryChoosedId+'/addNextCategoryForExercise?exerciseId='+paramIdExercise).then(function (response) {
                        $uibModalInstance.close();
                    });
        
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
    
    $http.get(urlRestGlobal + '/categories')
        .then(function (response) {
            $scope.categories = response.data;

        });
    var treeIdCategories = [];
    var treeNameCategories = [];

    $scope.deepTree = function (cat) {
        treeIdCategories.push(cat.id);
        treeNameCategories.push(cat.xwims_translation);
        console.log(treeIdCategories);
        $http.get(urlRestGlobal + '/categories/' + cat.id + '/subcategories')
            .then(function (response) {
                $scope.categories = response.data;

            });
    };


    $scope.backTree = function () {
        treeIdCategories.pop();
        treeNameCategories.pop();
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
