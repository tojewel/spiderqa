angular.module('App', ['navi', 'dashboard', 'testcase', 'testcases', 'master_detail', 'defects', 'defect', 'logs', 'statuses'])

.run(function($rootScope, $templateCache) {
    $rootScope.$on('$viewContentLoaded', function() {
        $templateCache.removeAll();
    });
});
