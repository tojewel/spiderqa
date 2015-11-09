angular.module('statuses', [])

    .directive('statuses', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'StatusesController',
            templateUrl: 'view/statuses.html'
        };
    })

    .controller('StatusesController', function ($scope) {

        $scope.all_status_model = ['passed', 'failed', 'broken', 'canceled', 'pending'].map(function (s) {
            return {name: s, value: false}
        })

    })