angular.module('nav', [])

    .directive('nav', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'NavController',
            templateUrl: 'nav/nav.html'
        };
    })

    .controller('NavController', function($scope) {})