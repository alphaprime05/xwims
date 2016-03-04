/*jslint plusplus: true */
var myApp = angular.module('myApp', ['ngTagsInput', 'ui.bootstrap', 'ngCookies', 'ngStorage']).config(function ($httpProvider) {
    delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
}).filter('trustAsResourceUrl', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsResourceUrl(val);
    };
}]);


myApp.controller('CategoryCtrl', function ($scope, $http, $cookies, $sessionStorage) {
    $scope.cookies = [];
    $scope.cookies.statusUser = $cookies.get('statusUser');
    $scope.cookies.idUser = $cookies.get('idUser');
    $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
    $http.defaults.headers.common['Content-Language'] = 'fr';

    $scope.selectedSort = "relevance";


    $scope.exercises = [];
    $scope.currentPage = 1;
    $scope.itemsPerPage = 15;
    $scope.maxSize = 5;
    $scope.Math = Math;



    if($.isEmptyObject($sessionStorage.worksheet))
        $sessionStorage.worksheet = [];
    
    //Lors du clique sur l'ajout à la feuille
    $scope.togglePaper = function (exercise, $event) {
        if ($event.currentTarget.alt == "Ajouter") {
            $event.currentTarget.src = "img/addedPaper.png";
            $event.currentTarget.alt = "Supprimer";

            $sessionStorage.worksheet.push(exercise);

        } else {

            $event.currentTarget.src = "img/addPaper.png";
            $event.currentTarget.alt = "Ajouter";

            var i = functiontofindIndexByKeyValue(cookieWorksheet, "id", exercise.id);
            if (i != -1) {
                $sessionStorage.worksheet.splice(i, 1);
            }
        }
    }


    //Lors de la sélection d'un tri
    $scope.changeSort = function () {

        if ($scope.tagsString != null && $scope.categoriesSearchedName != null) {
            $scope.reloadExercises($scope.selectedSort);
        }
    };

    //Charge la liste des exercises en fonction de la recherche et affiche les images du score
    $scope.loadExercises = function (url) {
        $http.get(url)
            .then(function (response) {
                var exercises = response.data;
                if (exercises) {

                    for (i = 0; i < exercises.length; i++) {
                        var ex = exercises[i];
                            ex.hats = [];
console.log("score"+ex.score);
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
                $scope.exercises = exercises;
            });
    }


    //lors d'un déplacement dans la pagination
    $scope.pageChanged = function (currentPage) {

        $scope.loadExercises(urlRestGlobal + '/search?keywords=' + $scope.tagsString + '&lang=fr&sortby=' + $scope.selectedSort + '&page=' + currentPage);

    };




    $scope.categoriesSearchedId = [];
    $scope.categoriesSearchedName = [];

    
    $scope.tags = [];

    $scope.loadTags = function (query) {
        var config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }

        return $http.get(urlRestGlobal + '/autocomplete?keyword=' + query + '&lang=fr');
    };

   $scope.addTag = function (cat) {
        
        $scope.categoriesSearchedId.push(cat.id);
        $scope.categoriesSearchedName.push(cat.xwims_translation);
       console.log($scope.categoriesSearchedName);
        $scope.reloadExercises();
    };

    $scope.addTagLevel = function (l) {
        $scope.tags.push({
            text:l.wimsName
        })
        $scope.reloadExercises();
    };



    $scope.categories = [];
    $scope.levels = [];
    $scope.categoryHeading = "Disciplines";

    $http.get(urlRestGlobal + '/categories', {
            cache: true
        })
        .then(function (response) {
            $scope.categories = response.data;
        });


    $scope.thirdTabCallback = function () {
        $http.get(urlRestGlobal + '/levels', {
                cache: true
            })
            .then(function (response) {
                $scope.levels = response.data;

            });
    };

    var treeIdCategories = [];

    $scope.changeToCategoryTab = function (cat) {
        $scope.addTag(cat);
        $scope.categoryHeading = "Themes";
        /*$('.keywords').each(function (index, value) { 
        $(this).remove();
        });*/
        treeIdCategories.push(cat.id);
        console.log(treeIdCategories);
        $http.get(urlRestGlobal + '/categories/' + cat.id + '/subcategories', {
            cache: true
        })
            .then(function (response) {
                console.log($scope.tags);
                $scope.categories = response.data;

            });
    };

    $scope.reloadCategory = function () {
        console.log(treeIdCategories);
        treeIdCategories.pop();
        $scope.categoriesSearchedName.pop();
        if(treeIdCategories.length > 0){
        var prevCatId = treeIdCategories[treeIdCategories.length - 1];
        $http.get(urlRestGlobal + '/categories/' + prevCatId + '/subcategories', {
            cache: true
        })
            .then(function (response) {
                console.log($scope.tags);
                $scope.categories = response.data;

            });
        }
        else{
            $http.get(urlRestGlobal + '/categories')
            .then(function (response) {
                console.log($scope.tags);
                $scope.categories = response.data;

            });
        }
        $scope.tagsString = $scope.tags.map(function (tag) {
            return tag.text;
        });
        $scope.loadExercises(urlRestGlobal + '/search?keywords=' + $scope.tagsString + '&lang=fr&sortby=relevance&page=' + $scope.currentPage, {
            cache: true
        });
    };

    $scope.reloadExercises = function (sortType) {
        $scope.tagsString = $scope.tags.map(function (tag) {
            return tag.text;
        });
        $scope.toSend = $scope.tagsString.concat($scope.categoriesSearchedName);
        $scope.loadExercises(urlRestGlobal + '/search?keywords=' + $scope.toSend + '&lang=fr&sortby='+sortType+'&page=' + $scope.currentPage, {
            cache: true
        });



        $http.get(urlRestGlobal + '/search?keywords=' + $scope.tagsString + '&lang=fr&total=true', {
                cache: true
            })
            .then(function (response) {
                console.log(response.data);
                $scope.bigTotalItems = response.data;
            });
    };

});
