var App = angular.module('App', ['elasticsearch', 'nvd3', 'ui.bootstrap', 'treeGrid', 'restangular']);

App.config(function (RestangularProvider) {
    RestangularProvider.setBaseUrl('http://localhost:8047');
});

// Service
//
// esFactory() creates a configured client instance. Turn that instance
// into a service so that it can be required by other parts of the application
App.service('client', function (esFactory) {
    return esFactory({
        host: 'localhost:9200',
        apiVersion: '1.2',
        log: 'trace'
    });
});

// Controller
//
// It also requires the esFactory to that it can check for a specific type of
// error which might come back from the client
var first = false;
function d3bar($scope, data) {
    $scope.options = {
        chart: {
            type: 'multiBarHorizontalChart',
            height: 20,
            x: function (d) {
                return d.label;
            },
            y: function (d) {
                return d.value;
            },
            showControls: false,
            showValues: true,
            transitionDuration: 1000,
            valuePadding: 10,

            margin: {
                top: 0,
                right: 0,
                bottom: 0,
                left: 0
            },

            stacked: true,
            showLegend: false,
            showXAxis: false,
            showYAxis: false,
        }
    };

    var colcors = {};
    colcors["broken"] = "#f0ad4e";
    colcors["passed"] = "#5cb85c";
    colcors["canceled"] = "#999";
    colcors["pending"] = "#9f94bf";
    colcors["failed"] = "#d9534f";

    while ($scope.bar_data.length > 0) {
        $scope.bar_data.pop();
    }

    for (i in data) {
        $scope.bar_data.push({
            "key": data[i].key,
            "color": colcors[data[i].key],
            "values": [
                {
                    "value": data[i].doc_count
                }]
        });
    }

    if (!first) {
        $scope.api.update();
    }
    first = false;
}

function query($scope) {
    var f = [];
    for (i in $scope.statusResults) {
        f.push(
            {
                "query": {
                    "match": {
                        "status": {
                            "query": $scope.statusResults[i],
                            "type": "phrase"
                        }
                    }
                }
            }
        )
    }

    return {
        "query": {
            "filtered": {
                "filter": {
                    "bool": {
                        "must": {
                            "or": {
                                "filters": f
                            }
                        }
                    }
                }
            }
        },
        "aggregations": {
            "status": {
                "terms": {
                    "field": "status"
                }
            }
        }
    }
}

function makeServerCall($scope, client, esFactory) {
    client.search({
        index: 'testaspect',
        type: 'TestCase',
        search_type: 'count',
        body: query($scope)
    })
        .then(function (resp) {
            $scope.tc = resp.aggregations.status.buckets;
            $scope.error = null;

            d3bar($scope, $scope.tc)
        })
        .catch(function (err) {
            $scope.clusterState = null;
            $scope.error = err;

            // if the err is a NoConnections error, then the client was not able to
            // connect to elasticsearch. In that case, create a more detailed error
            // message
            if (err instanceof esFactory.errors.NoConnections) {
                $scope.error = new Error('Unable to connect to elasticsearch. ' +
                    'Make sure that it is running and listening at http://localhost:9200');
            }
        });
}

function drill(Restangular, $scope) {
    $scope.col_defs = [
        {field: "Passed"},
        {field: "Failed"},
        {field: "Broken"},
        {field: "Canceled"},
        {field: "Pending"},
    ];

    $scope.tree_data = [];

    function render(parent, data) {
        function f(d) {
            return d == '0'? "": d;
        }

        for (i in data) {
            parent.push({
                "Test": data[i].Test + " (" + data[i].Total + ")",
                "Passed": f(data[i].Passed),
                "Failed": f(data[i].Failed),
                "Broken": f(data[i].Broken),
                "Canceled": f(data[i].Canceled),
                "Pending": f(data[i].Pending),
                "children": [{Test: "Loading...", Passeds: "class"}]
            })
        }
    }

    function sql(group_by) {
        sql = "SELECT " +
            " (CASE WHEN " + group_by + " IS NULL THEN 'No Package' ELSE " + group_by + " END) AS Test, " +
            " SUM(CASE WHEN status = 'passed' THEN 1 ELSE 0 END) as Passed, " +
            " SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) as Failed, " +
            " SUM(CASE WHEN status = 'broken' THEN 1 ELSE 0 END) as Broken, " +
            " SUM(CASE WHEN status = 'canceled' THEN 1 ELSE 0 END) as Canceled, " +
            " SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as Pending, " +
            " COUNT(*) as Total " +
            "FROM mongo.testaspect.`TestCase` " +
            "GROUP BY " + group_by;

        return sql;
    }

    Restangular.all('query.json').post({
        query: sql("packaze"),
        queryType: "SQL"
    }).then(function (res) {
        $scope.rae = res;
        render($scope.tree_data, res.rows);
    }, function (response) {
        $scope.rae = response;
    });

}

App.controller('ExampleController', function ($scope, client, esFactory, Restangular) {
    $scope.statusModel = {
        broken: true,
        passed: false,
        canceled: true,
        pending: false,
        failed: false
    };

    $scope.bar_data = [];

    function updateModel($scope) {
        $scope.statusResults = [];
        angular.forEach($scope.statusModel, function (value, key) {
            if (value) {
                $scope.statusResults.push(key);
            }
        });
    }

    updateModel($scope);

    $scope.$watchCollection('statusModel', function () {
        updateModel($scope);
        makeServerCall($scope, client, esFactory);
    });

    makeServerCall($scope, client, esFactory);
    drill(Restangular, $scope);
});
