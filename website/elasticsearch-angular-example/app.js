var ExampleApp = angular.module('ExampleApp', ['elasticsearch', 'nvd3', 'ui.bootstrap']);

// Service
//
// esFactory() creates a configured client instance. Turn that instance
// into a service so that it can be required by other parts of the application
ExampleApp.service('client', function (esFactory) {
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

function hbar(data) {
    var sum = data
        .map(function (num) {
            return num.doc_count;
        })
        .reduce(function (a, b) {
            return a + b;
        });

    d3.select(".chart")
        .selectAll("div")
        .data(data)
        .enter().append("div")
        .style("display", "inline-block")
        .style("width", function (d) {
            return ((d.doc_count * 100) / sum) + "%";
        })
        .attr("class", function (d) {
            return d3.select(this).attr("class") + " " + 'progress-bar-' + d.key;
        })
        .attr("uib-tooltip", function (d) {
            return d.doc_count + " " + d.key
        })
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

            hbar($scope.tc)
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

ExampleApp.controller('ExampleController', function ($scope, client, esFactory) {
    $scope.statusModel = {
        broken: true,
        passed: false,
        canceled: true,
        pending: true,
        failed: true
    };

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
