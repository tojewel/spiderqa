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


    .controller('TestcaseController', function ($scope, Restheart, attachmentType) {
        function load_testcase() {
            Restheart.all('TestCase').customGET('', {filter: {full_name: $scope.selected.full_name}})
                .then(function (result) {
                    $scope.testcase = result._embedded['rh:doc'][0];
                })
        }

        load_testcase();

        $scope.$watch('selected.full_name', function () {
            load_testcase();
        });

        // AttachmentCtrl

        $scope.getIconClass = function (type) { //NOSONAR
            switch (attachmentType(type)) {
                case 'text':
                    return 'fa fa-file-text-o';
                case 'image':
                case 'svg':
                    return 'fa fa-file-image-o';
                case 'code':
                    return 'fa fa-file-code-o';
                case 'csv':
                    return 'fa fa-table';
                default:
                    return 'fa fa-file-o';
            }
        };

    })

    .controller('StepCtrl', function ($scope, Restheart) {

    })


    .factory('attachmentType', function () {
        "use strict";
        return function (type) { //NOSONAR
            switch (type) {
                case 'image/bmp':
                case 'image/gif':
                case 'image/tiff':
                case 'image/jpeg':
                case 'image/jpg':
                case 'image/png':
                case 'image/*':
                    return "image";
                case 'text/xml':
                case 'application/xml':
                case 'application/json':
                case 'text/json':
                case 'text/yaml':
                case 'application/yaml':
                case 'application/x-yaml':
                case 'text/x-yaml':
                    return "code";
                case 'text/plain':
                case 'text/*':
                    return "text";
                case 'text/html':
                    return "html";
                case 'text/csv':
                    return "csv";
                case 'image/svg+xml':
                    return "svg";
                default:
            }
        };
    })
