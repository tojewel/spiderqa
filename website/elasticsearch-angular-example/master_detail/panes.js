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

        this.show = function (pane) {
            var remove = [];
            for (var i = panes.indexOf(pane) + 2; i < panes.length; i++) {
                remove.push(panes[i]);
            }

            for (var i = 0; i < remove.length; i++) {
                remove[i].close();
            }

            panes[panes.length - 1].expanded(false);
            this.updatePositions();
        }

        this.addPane = function (pane) {
            panes.push(pane);
            this.updatePositions();
        };

        this.removePane = function (pane) {
            panes.splice(panes.indexOf(pane), 1);
            this.updatePositions();
        };

        this.updatePositions = function () {
            //var text = "updating possition...";
            //
            //for (var i = 0; i < panes.length; i++) {
            //    text += panes[i].title() + "." + panes[i].isExpanded() + ", "
            //}
            //
            //console.log(text)
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
            var expanded = panes.length == 1 ? true : (index < panes.length - 2 ? true : pane.isExpanded());
            var offset = 5 * index;
            var width = (expanded ? 100 : 50) - offset;

            pane.elem[index === panes.length - 1 ? 'addClass' : 'removeClass']('pane_col_last');
            pane.elem[index === 0 ? 'addClass' : 'removeClass']('pane_col_first');
            pane.elem[paneNeedOverlay(index) ? 'addClass' : 'removeClass']('pane_col-overlay');

            if (paneShouldBeAtRight(index, expanded)) {
                if (screenTest.isNarrow()) {
                    offset += 25;
                    width = 100 - offset;
                    pane.setPosition(index, offset, width);
                } else {
                    pane.setPosition(index, 50, 50);
                }

            } else {
                pane.setPosition(index, offset, width);
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

        Pane.prototype.setPosition = function (i, left, width) {
            this.elem.css({zIndex: i + 1, left: left + '%', width: width + '%'});
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

                scope.panesClr = PanesCtrl;

                pane.title = function () {
                    return scope.title;
                };

                scope.expanded = false;
                pane.isExpanded = function () {
                    return scope.expanded;
                };

                pane.expanded = function (expanded) {
                    return scope.expanded = expanded;
                };

                pane.close = function () {
                    scope.close();
                }

                PanesCtrl.addPane(pane);

                scope.show = function () {
                    PanesCtrl.show(pane);
                }

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

            return false;
        }
    });
