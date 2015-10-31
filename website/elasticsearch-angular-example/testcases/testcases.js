angular.module('testcases', ['treeGrid', 'restangular'])

    .config(function (RestangularProvider) {
        RestangularProvider.setBaseUrl('http://localhost:8047');
    })

    .directive('testcases', function () {
        return {
            restrict: 'E',
            transclude: true,
            scope: {
                testcase: '='
            },
            replace: true,
            controller: 'TestcasesController',
            templateUrl: 'testcases/testcases.html'
        };
    })

    .controller('TestcasesController', function ($scope, Restangular) {
        var GROUP_BYS = ["packaze", "clazz", "name"];

        $scope.expanding_property = {
            field: "Test",
            displayName: "Tests",
            sortable: true,
            filterable: true
        };

        $scope.col_defs = [
            {
                field: "Passed",
                cellTemplate: "<span class='label label-success'>{{row.branch[col.field]}}</span>",
            },
            {
                field: "Failed",
                cellTemplate: "<span class='label label-danger'>{{row.branch[col.field]}}</span>",
            },
            {
                field: "Broken",
                cellTemplate: "<span class='label label-warning'>{{row.branch[col.field]}}</span>",
            },
            {
                field: "Canceled",
                cellTemplate: "<span class='label label-default'>{{row.branch[col.field]}}</span>",
            },
            {
                field: "Pending",
                cellTemplate: "<span class='label label-status-pending'>{{row.branch[col.field]}}</span>",
            },
        ];

        $scope.tree_data = [];

        function sql(group_by, where) {
            var test = group_by;
            var full_name = group_by == 'name' ? "full_name, " : "";
            if (full_name) {
                group_by = group_by + ", full_name"
            }

            var sql = "SELECT " + full_name +
                " " + test + " AS Test, " +
                " SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) as Passed, " +
                " SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) as Failed, " +
                " SUM(CASE WHEN status = 'broken' THEN 1 ELSE 0 END) as Broken, " +
                " SUM(CASE WHEN status = 'canceled' THEN 1 ELSE 0 END) as Canceled, " +
                " SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as Pending, " +
                " COUNT(*) as Total " +
                " FROM mongo.testaspect.`TestCase` " +
                " WHERE started IN (SELECT MAX(started) FROM mongo.testaspect.`TestCase` GROUP BY full_name) AND " +
                "" + where + " " +
                " GROUP BY " + group_by;

            $scope.log = sql;

            return sql;
        };

        function render(data, to_array, level) {
            to_array.splice(0, to_array.length)

            function f(d) {
                return d == '0' ? "" : d;
            }

            for (i in data) {
                var t = data[i].Test

                var kids = level < GROUP_BYS.length - 1 ? [{"Test": "Loading..."}] : null;
                var name = (t == null ? "Without " + GROUP_BYS[level] : t) + (kids ? " (" + data[i].Total + ")" : "");

                to_array.push({
                    "Test": name,
                    "Passed": f(data[i].Passed),
                    "Failed": f(data[i].Failed),
                    "Broken": f(data[i].Broken),
                    "Canceled": f(data[i].Canceled),
                    "Pending": f(data[i].Pending),
                    "full_name": data[i].full_name,
                    "group_by": t,
                    "children": kids
                })
            }
        }

        function load(level, where, to_array) {
            Restangular.all('query.json').post({
                query: sql(GROUP_BYS[level], where),
                queryType: "SQL"
            }).then(function (res) {
                $scope.drill = sql(GROUP_BYS[level], where);
                render(res.rows, to_array, level);
            }, function (response) {
                $scope.drill = response;
            });
        }

        load(0, "2 > 1", $scope.tree_data);

        $scope.log = "fuck you";

        $scope.my_select = function (branch) {
            $scope.log = 'you select on ' + JSON.stringify(branch)
            $scope.tc = branch

            if (branch.level < GROUP_BYS.length) {
                var field = GROUP_BYS[branch.level - 1];
                var value = branch.group_by

                var where = value ? field + " = '" + value + "'" : field + " IS null";

                load(branch.level, where, branch.children);
            }
        }

        $scope.my_click = function (branch) {
            $scope.log = 'you clicked on ' + JSON.stringify(branch)

            if (branch.full_name) {
                Restangular.all('query.json').post({
                    query: "SELECT * FROM mongo.testaspect.`TestCase` WHERE full_name = '" + branch.full_name + "'",
                    queryType: "SQL"
                }).then(function (response) {
                    $scope.drill = response;

                    $scope.testcase = response.rows[0];
                }, function (response) {
                    $scope.drill = response;
                });
            }
        }

    })


