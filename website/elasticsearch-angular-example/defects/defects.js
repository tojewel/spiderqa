angular.module('defects', ['smart-table'])

    .directive('defects', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'DefectsController',
            templateUrl: 'defects/defects.html'
        };
    })

    .controller('DefectsController', function ($scope, Drill) {
        $scope.col_defs = [
            {
                field: "Defect",
            },
        ];

        $scope.tree_data = [];

        var SQL = 'SELECT count(*) AS Count_, t.failure.message AS Defect ' +
            'FROM mongo.testaspect.`TestCase` t ' +
            'WHERE t.failure.message IS NOT NULL ' +
            'GROUP BY t.failure.message'

        Drill.all('query.json').post({
            query: SQL,
            queryType: "SQL"
        }).then(function (res) {
            for (var i = 0; i < res.rows.length; i++) {
                $scope.tree_data.push(res.rows[i])
            }
        }, function (response) {
            $scope.res = response;
        });

        $scope.my_click = function (branch) {
            console.log(branch);
            $scope.selected.defect = branch.Defect;
        }
    })