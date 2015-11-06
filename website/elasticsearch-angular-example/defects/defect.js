angular.module('defect', [])

    .directive('defect', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'DefectController',
            templateUrl: 'defects/defect.html'
        };
    })

    .controller('DefectController', function ($scope, Drill) {
        $scope.col_defs = [
            {
                field: "Name",
            },
            {
                field: "Status",
            },
        ];

        console.log("$scope.selected.defect=" + $scope.selected.defect)

        $scope.tree_data = [];

        function callServer() {
            var SQL = 'SELECT name AS Name, status AS Status, full_name ' +
                'FROM mongo.testaspect.`TestCase` t ' +
                "WHERE t.failure.message =  '" + $scope.selected.defect + "'"
            console.log(SQL)
            Drill.all('query.json').post({
                query: SQL,
                queryType: "SQL"
            }).then(function (res) {
                while ($scope.tree_data.length > 0) {
                    $scope.tree_data.pop();
                }
                for (var i = 0; i < res.rows.length; i++) {
                    $scope.tree_data.push(res.rows[i])
                }
            }, function (response) {
                $scope.res = response;
            });
        }

        callServer();

        $scope.my_click = function (branch) {
            console.log(branch)
            $scope.selected.full_name = branch.full_name
        }

        $scope.$watch('selected.defect', function() {
            callServer();
        });
    })