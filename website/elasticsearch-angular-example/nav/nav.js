angular.module('navi', [])

    .directive('navi', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'NavController',
            templateUrl: 'nav/nav.html'
        };
    })

    .controller('NavController', function($scope) {})