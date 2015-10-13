Template.barchart.rendered = function () {
    var data = [{v: 4, c: 'red'}, {v: 8, c: 'blue'}, {v: 20, c: 'green'}];

    var sum = data.map(function (num) {
        return num.v;
    }).reduce(function (a, b) {
        return a + b;
    });

    d3.select(".chart")
        .selectAll("div")
        .data(data)
        .enter().append("div")
        .style("display", "inline-block")
        .style("width", function (d) {
            return ((d.v * 100) / sum) + "%";
        })
        .style("background-color", function (d) {
            return d.c;
        })
}