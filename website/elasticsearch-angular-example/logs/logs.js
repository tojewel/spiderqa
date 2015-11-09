angular.module('logs', [])

    .directive('logs', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: 'LogsController',
            templateUrl: 'logs/logs.html'
        };
    })

    .controller('LogsController', function ($scope, ES) {
        $scope.col_defs = [
            {
                field: "Severity",
            },
            {
                field: "message",
            },
        ];

        $scope.tree_data = [];

        var fields = "Time, Severity, message"

        var SQL = "SELECT " + fields + " FROM logstash/logs" +
            " LIMIT 10"

        ES.all('').customGET('/_sql', {sql: SQL}).then(function (res) {
            $scope.res = res;

            var rows = res.hits.hits
            for (var i = 0; i < rows.length; i++) {
                $scope.tree_data.push(rows[i]._source)
            }
        }, function (response) {
            $scope.res = response;
        });

    })