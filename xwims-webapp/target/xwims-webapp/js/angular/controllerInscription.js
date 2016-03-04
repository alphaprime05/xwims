
var myApp = angular.module('myApp', ['ngCookies', 'ngStorage']).config(function ($httpProvider) {
    delete $httpProvider.defaults.headers.common['X-Requested-With']; // on active le cross domain
}).filter('trustAsResourceUrl', ['$sce', function ($sce) {
    return function (val) {
        return $sce.trustAsResourceUrl(val);
    };
}]);

myApp.controller('subscribtionCtrl', function ($scope, $http, $cookies, $window) {
    $http.defaults.headers.common['Authorization'] = 'Basic ' + $cookies.get('btoaUser');
    $scope.connected = $cookies.get('connected');
    
    
    $scope.subscribe = function(){
        if($scope.loginEx.password == $scope.loginEx.verify){
	var data = {
		email : $scope.inputEmail,
		password : $scope.loginEx.password,
		first_name : $scope.inputFirstname,
		last_name : $scope.inputName,
		lang : "fr"

	}
            $http.post(urlRestGlobal + '/register', data).then(function (response) {
                           console.log(response.data);
            });
        }
                $window.location.href = "inscriptionFinished.html"; 

        
    }
});

myApp.directive('nxEqualEx', function() {
    return {
        require: 'ngModel',
        link: function (scope, elem, attrs, model) {
            if (!attrs.nxEqualEx) {
                console.error('nxEqualEx expects a model as an argument!');
                return;
            }
            scope.$watch(attrs.nxEqualEx, function (value) {
                // Only compare values if the second ctrl has a value.
                if (model.$viewValue !== undefined && model.$viewValue !== '') {
                    model.$setValidity('nxEqualEx', value === model.$viewValue);
                }
            });
            model.$parsers.push(function (value) {
                // Mute the nxEqual error if the second ctrl is empty.
                if (value === undefined || value === '') {
                    model.$setValidity('nxEqualEx', true);
                    return value;
                }
                var isValid = value === scope.$eval(attrs.nxEqualEx);
                model.$setValidity('nxEqualEx', isValid);
                return isValid ? value : undefined;
            });
        }
    };
});
