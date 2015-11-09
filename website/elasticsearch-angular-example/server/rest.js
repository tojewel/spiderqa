angular.module('rest', ['restangular'])

    .factory("Drill", ["Restangular", function (restangular) {
        return restangular.withConfig(function (RestangularConfigurer) {
            RestangularConfigurer.setBaseUrl("http://localhost:8047");
        });
    }])

    .factory("Restheart", ["Restangular", function (restangular) {
        return restangular.withConfig(function (RestangularConfigurer) {
            RestangularConfigurer.setBaseUrl("http://localhost:8080/testaspect");
        });
    }])

    .factory("ES", ["Restangular", function (restangular) {
        return restangular.withConfig(function (RestangularConfigurer) {
            RestangularConfigurer.setBaseUrl("http://localhost:9200");
        });
    }])

;