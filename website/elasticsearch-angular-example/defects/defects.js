angular.module('defects', [])

    .directive('defects', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'DefectsController',
            templateUrl: 'defects/defects.html'
        };
    })

    .controller('DefectsController', function($scope) {})