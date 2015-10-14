var App = angular.module('App', ['elasticsearch', 'nvd3', 'ui.bootstrap']);

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

function d3bar($scope, data) {
    $scope.options = {
        chart: {
            type: 'multiBarHorizontalChart',
            height: 40,
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

    while($scope.bar_data.length > 0) {
        $scope.bar_data.pop();
    }

    for(i in data) {
        $scope.bar_data.push({
            "key": data[i].key,
            "color": colcors[data[i].key],
            "values": [
                {
                    "value": data[i].doc_count
                }]
        });
    }

    $scope.api.update();
}

function query($scope) {
    var f = [];
    for (i in $scope.statusResults) {
        f.push(
            {
                "query": {
                    "match": {
                        "result.status": {
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
                    "field": "result.status"
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

App.controller('ExampleController', function ($scope, client, esFactory) {
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
});
