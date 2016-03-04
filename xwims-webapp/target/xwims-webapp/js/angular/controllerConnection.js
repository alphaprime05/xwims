

myApp.controller('ConnectionCtrl', function ($scope, $http, $cookies) {
    $scope.connected = $cookies.get('connected');
    $scope.user = {};
    $scope.user.email = {};
    $scope.checkboxRememberMe = false;

    $scope.connection = function () {
        $http.defaults.headers.common['Authorization'] = 'Basic ' + btoa($scope.user.email.text + ':' + $scope.user.password);

        $http.get(urlRestGlobal + '/login')
            .then(function (response) {
                $scope.user.status = response.data;
                if (response.data === "ROOT" || response.data === "CERTIFIED" || response.data === "NORMAL_USER") {
                    $http.get(urlRestGlobal + '/getuserid')
                        .then(function (response) {
                            $scope.user.id = response.data;


                        });


                    $cookies.put('btoaUser', btoa($scope.user.email.text + ':' + $scope.user.password));
                    //$cookies.put('passUser', $scope.user.password);
                    $cookies.put('statusUser', $scope.user.status);
                    $cookies.put('idUser', $scope.user.id);
                    $cookies.put('connected', true);
                    $scope.connected = $cookies.get('connected');
                    $("#connexion").modal("hide");
                }


            });
    };
});
