angular.module('testcase', [])

    .directive('testcase', function () {
        return {
            restrict: 'E',
            transclude: true,
            scope: {
                testcase: '='
            },
            replace: true,
            //      controller: 'PanesController',
            templateUrl: 'testcase/testcase.html'
        };
    })