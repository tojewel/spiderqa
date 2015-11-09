angular.module('testcase', [])

    .directive('testcase', function () {
        return {
            restrict: 'E',
            transclude: true,
            replace: true,
            controller: 'TestcaseController',
            templateUrl: 'testcase/testcase.html'
        };
    })

    .controller('TestcaseController', function($scope, Restheart) {
        function load_testcase() {
            Restheart.all('TestCase').customGET('', {filter: {full_name: $scope.selected.full_name}})
                .then(function (result) {
                    $scope.testcase = result._embedded['rh:doc'][0];
                })
        }

        load_testcase();

        $scope.$watch('selected.full_name', function() {
            load_testcase();
        });
    })

    .controller('StepCtrl', function($scope, Restheart) {

    })
