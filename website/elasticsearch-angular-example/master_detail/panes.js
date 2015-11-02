/*global angular*/
angular.module('master_detail', [])

    .directive('panes', function () {
        "use strict";
        return {
            restrict: 'E',
            transclude: true,
            scope: {},
            replace: true,
            controller: 'PanesController',
            templateUrl: 'master_detail/panes.html'
        };
    })

    .controller('PanesController', function ($scope, screenTest) {
        "use strict";
        var panes = [];
        this.addPane = function (pane) {
            panes.push(pane);
            this.updatePositions();
        };
        this.removePane = function (pane) {
            panes.splice(panes.indexOf(pane), 1);
            this.updatePositions();
        };
        this.updatePositions = function () {
            console.log("updating possition..." + panes)
            panes.forEach(setPanePosition);
        };

        function paneNeedOverlay(index) {
            return index < panes.length - 2 || (index < panes.length - 1 && panes[panes.length - 1].isExpanded());
        }

        function paneShouldBeAtRight(index, expanded) {
            return index !== 0 && index === panes.length - 1 && !expanded;
        }

        function setPanePosition(pane) {
            var index = panes.indexOf(pane);
            var expanded = pane.isExpanded();
            var offset = 5 * index;
            var width = (expanded ? 100 : 50) - offset;

            console.log("setting possition..." + index)

            pane.elem[index === panes.length - 1 ? 'addClass' : 'removeClass']('pane_col_last');
            pane.elem[index === 0 ? 'addClass' : 'removeClass']('pane_col_first');
            pane.elem[paneNeedOverlay(index) ? 'addClass' : 'removeClass']('pane_col-overlay');

            if (paneShouldBeAtRight(index, expanded)) {
                if (screenTest.isNarrow()) {
                    offset += 25;
                    width = 100 - offset;
                    pane.setPosition(offset, width);
                } else {
                    pane.setPosition(50, 50);
                }

            } else {
                pane.setPosition(offset, width);
            }
        }

        $scope.$on('screenTestChange', this.updatePositions);
    })

    .directive('pane', function () {
        "use strict";

        // Javascript's OOP :)
        function Pane(elem) {
            this.elem = elem;
        }

        Pane.prototype.setPosition = function (left, width) {
            this.elem.css({left: left + '%', width: width + '%'});
        };
        Pane.prototype.isExpanded = function () {
            return false;
        };
        //--------------------

        return {
            require: '^panes',
            restrict: 'E',
            transclude: true,
            scope: {
                title: '@',
                onclose: '&'
            },
            replace: true,
            templateUrl: 'master_detail/pane.html',

            link: function (scope, elem, attrs, PanesCtrl) {
                var pane = new Pane(elem);

                scope.show = true;
                scope.panesClr = PanesCtrl;

                scope.expanded = false;
                pane.isExpanded = function () {
                    return scope.expanded;
                };

                PanesCtrl.addPane(pane);

                scope.$on('$destroy', function () {
                    PanesCtrl.removePane(pane);
                });
            },

            controller: function ($scope) {
                $scope.expand = function () {
                    $scope.expanded = true;
                    $scope.panesClr.updatePositions();
                }

                $scope.collapse = function () {
                    $scope.expanded = false;
                    $scope.panesClr.updatePositions();
                }

                $scope.close = function () {
                    $scope.onclose && $scope.onclose();
                }
            }
        };
    })

    .factory('screenTest', function ($rootScope, $window) {
        "use strict";
        var breakpoints = {
            xs: testVisibility('visible-xs'),
            sm: testVisibility('visible-sm'),
            md: testVisibility('visible-md'),
            lg: testVisibility('visible-lg')
        };
        angular.element($window).on('resize', onResize);

        return {
            size: breakpoints,
            isNarrow: function () {
                return breakpoints.xs || breakpoints.sm;
            }
        };

        function onResize() {
            angular.forEach(breakpoints, function (value, point) {
                breakpoints[point] = testVisibility('visible-' + point);
            });
            $rootScope.$broadcast('screenTestChange', breakpoints);
        }

        function testVisibility(cls) {
            //var el = angular.element('<div />').addClass(cls).appendTo('body');
            //console.log('el=' + el )
            //var visible = el.css('display') !== 'none';
            // el.remove();
            //return visible;

            return true;
        }
    });
