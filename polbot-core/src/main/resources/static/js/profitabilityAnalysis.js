//since 2010
//sticks 4h
poloneixChartApi = "https://poloniex.com/public?command=returnChartData&currencyPair=%CURR_HOLDER%&start=1262304000&end=9999999999&period=14400"

function postProcess(data, options) {
    for (index = 0; index < data.length; ++index) {
        data[index].start = new Date(data[index].start * 1000);
    }

    return data;
}



function loadJson(file) {
    var request;
    if (window.XMLHttpRequest) {
        // IE7+, Firefox, Chrome, Opera, Safari
        request = new XMLHttpRequest();
    } else {
        // code for IE6, IE5
        request = new ActiveXObject('Microsoft.XMLHTTP');
    }
    // load
    request.open('GET', file, false);
    request.send();

    return JSON.parse(request.responseText);
}


function postProcessCandle(data, options) {
    for (index = 0; index < data.length; ++index) {
        data[index].date = new Date(data[index].date * 1000);
    }

    return data;
}

function parseDates(data) {
    for (index = 0; index < data.length; ++index) {
        data[index].date = new Date(data[index].date * 1000);
    }

    return data;
}

function postProcessTxData(buys, sells, options) {
    if (data === null)
        return [];

    var data = [];
    for (index = 0; index < buys.length; ++index) {
        var obj = {};
        obj.value = buys[index].value;
        obj.date = new Date(buys[index].date);
        obj.type = 'sign';
        obj.backgroundColor = '#85CDE6';
        obj.graph = 'g1';
        obj.text = 'B';
        obj.description = 'SThis is description of an event';

        data.push(obj);
    }

    for (index = 0; index < sells.length; ++index) {
        var obj = {};
        obj.value = sells[index].value;
        obj.date = new Date(sells[index].date);
        obj.type = 'sign';
        obj.backgroundColor = '#85FFE6';
        obj.graph = 'g1';
        obj.text = 'S';
        obj.description = 'SThis is description of an event';

        data.push(obj);
    }

    return data;
}


// MOVING AVERAGE PLUGIN FOR JAVASCRIPT STOCK CHARTS FROM AMCHARTS //
AmCharts.averageGraphs = 0;
AmCharts.addMovingAverage = function (dataSet, field) {
    // update dataset
    var avgField = "avg" + AmCharts.averageGraphs;

    // calculate moving average
    var fc = 0;
    var sum = 0;
    for (var i = 0; i < dataSet.length; i++) {
        var dp = dataSet[i];
        if (dp[field] !== undefined) {
            sum += dp[field];
            fc++;
            dp[avgField] = chartData.slice(0,i+1).slice(-10).map((a) => a.close).reduce((a,b)=>a+b)/10;
        }
    }

    // create a graph
    //var graph = new AmCharts.StockGraph();
    //graph.valueField = avgField;
    //panel.addStockGraph(graph);

    // increment average graph count
    AmCharts.averageGraphs++;

    // return newly created StockGraph object
    // return graph;
}



function addStockChart(currency) {
    if (!currency)
        return;


    poloneixChartApiForCurrency = poloneixChartApi.replace("%CURR_HOLDER%", currency);

    //createStockChart(poloneixChartApiForCurrency);

    chartData = loadJson(poloneixChartApiForCurrency);
    chartData = parseDates(chartData)

    // ADD AVERAGES //////////////////////////////////////
    var avgGraph = AmCharts.addMovingAverage(chartData, "close");

    var chart = AmCharts.makeChart("chartdiv", {
        "type": "stock",
        "theme": "light",
        "categoryAxesSettings": {
            "parseDates": true,
            "minPeriod": "4hh"
        },
        "dataSets": [{
            "fieldMappings": [{
                "fromField": "open",
                "toField": "open"
            }, {
                "fromField": "close",
                "toField": "close"
            }, {
                "fromField": "high",
                "toField": "high"
            }, {
                "fromField": "low",
                "toField": "low"
            }, {
                "fromField": "volume",
                "toField": "volume"
            }, {
                "fromField": "close",
                "toField": "value"
            },{
                "fromField": "avg0",
                "toField": "avg0"
            }],
            "color": "#7f8da9",
            "dataProvider": chartData,
            //   "dataLoader": {"url": poloneixChartApiForCurrency, "format": "json", "postProcess": postProcessCandle},
            "title": "Prices Data",
            "categoryField": "date",
            "stockEvents": postProcessTxData(buys, sells)
        }, {
            "title": "History",
            "fieldMappings": [{
                "fromField": "value",
                "toField": "open"
            }, {
                "fromField": "value",
                "toField": "close"
            }, {
                "fromField": "value",
                "toField": "high"
            }, {
                "fromField": "value",
                "toField": "low"
            }, {
                "fromField": "value",
                "toField": "value"
            }, {
                "fromField": "date",
                "toField": "date"
            }],
            "categoryField": "date",
            "compared": true,
            "dataProvider": postProcessTxData(buys, sells)
        }
        ],


        "panels": [{
            "title": "Value",
            "showCategoryAxis": true,
            "percentHeight": 70,
            "valueAxes": [{
                "id": "v1",
                "dashLength": 5
            }],


            "stockGraphs": [{
                "id": "g1",
                "proCandlesticks": true,
                "balloonText": "Open:<b>[[open]]</b><br>Low:<b>[[low]]</b><br>High:<b>[[high]]</b><br>Close:<b>[[close]]</b><br>",
                "closeField": "close",
                "fillColors": "#7f8da9",
                "highField": "high",
                "lineColor": "#7f8da9",
                "lineAlpha": 1,
                "lowField": "low",
                "fillAlphas": 0.9,
                "negativeFillColors": "#db4c3c",
                "negativeLineColor": "#db4c3c",
                "openField": "open",
                "title": "Price:",
                "type": "candlestick",
                "valueField": "value",
                "comparable": true,
                "compareField": "value"
            },{
                "id": "g2",
                "title": "Graph #2",
                "lineThickness": 2,
                "valueField": "avg0",
                "useDataSetColors": false
            }],

            "stockLegend": {
                "valueTextRegular": undefined,
                "periodValueTextComparing": "[[percents.value.close]]%",
                "periodValueTextRegular": "[[value.close]]"
            }
        }, {
            "title": "Volume",
            "percentHeight": 30,
            "marginTop": 1,
            "showCategoryAxis": true,
            "valueAxes": [{
                "dashLength": 5
            }],

            "categoryAxis": {
                "dashLength": 5
            },

            "stockGraphs": [{
                "valueField": "volume",
                "type": "column",
                "showBalloon": false,
                "fillAlphas": 1
            }],

            "stockLegend": {
                "markerType": "none",
                "markerSize": 0,
                "labelText": "",
                "periodValueTextRegular": "[[value.close]]"
            }
        }
        ],

        "panelsSettings": {
            "recalculateToPercents": "never"
        },

        "chartScrollbarSettings": {
            "graph": "g1",
            "graphType": "line"
        },
        "chartCursorSettings": {
            "valueBalloonsEnabled": true,
            "fullWidth": true,
            "cursorAlpha": 0.1,
            "valueLineBalloonEnabled": true,
            "valueLineEnabled": true,
            "valueLineAlpha": 0.5
        }, "dataSetSelector": {
            "position": "top"
        },
        "periodSelector": {
            "position": "bottom",
            "periods": [{
                "period": "DD",
                "count": 10,
                "selected": true,
                "label": "10D"
            }, {
                "period": "MM",
                "count": 1,
                "label": "1M"
            }, {
                "period": "MM",
                "count": 6,
                "label": "6M"
            }, {
                "period": "YYYY",
                "count": 1,
                "label": "1Y"
            }, {
                "period": "YYYY",
                "count": 2,

                "label": "2Y"
            },
                {
                    "period": "YTD",
                    "label": "YTD"
                },
                {
                    "period": "MAX",
                    "label": "MAX"
                }
            ]
        }
    });

}

function legendHandler(evt) {
    var state = evt.dataItem.hidden;
    if (evt.dataItem.id == "all") {
        for (var i1 in evt.chart.graphs) {
            if (evt.chart.graphs[i1].id != "all") {
                evt.chart[evt.dataItem.hidden ? "hideGraph" : "showGraph"](evt.chart.graphs[i1]);
            }
        }
    }
}
