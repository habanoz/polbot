//since 2010
//sticks 4h
poloneixChartApi = "https://poloniex.com/public?command=returnChartData&currencyPair=%CURR_HOLDER%&start=1262304000&end=9999999999&period=14400"

function postProcess(data, options) {
    for (index = 0; index < data.length; ++index) {
        data[index].start = new Date(data[index].start * 1000);
    }

    return data;
}


function postProcessCandles(data, options) {
    for (index = 0; index < data.length; ++index) {
        date = new Date(data[index].date * 1000);
        data[index].date = date.getUTCFullYear() +
            '-' + ('0' + date.getUTCMonth()).slice(-2) +
            '-' + ('0' + date.getUTCDate()).slice(-2) +
            ' ' + ('0' + date.getUTCHours()).slice(-2) +
            ':' + ('0' + date.getUTCMinutes()).slice(-2) +
            ':' + ('0' + date.getUTCSeconds()).slice(-2) +
            '.' + (date.getUTCMilliseconds() / 1000).toFixed(3).slice(2, 5)
    }

    return data;
}

function postProcessCandle(data, options) {
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
        var obj={};
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
        var obj={};
        obj.value = sells[index].value;
        obj.date = new Date(sells[index].date );
        obj.type = 'sign';
        obj.backgroundColor = '#85FFE6';
        obj.graph = 'g1';
        obj.text = 'S';
        obj.description = 'SThis is description of an event';

        data.push(obj);
    }

    return data;
}


function periodChanged(event) {
    alert("period changed:" + event)
}

function addStockChart(currency) {
    if (!currency)
        return;

    poloneixChartApiForCurrency = poloneixChartApi.replace("%CURR_HOLDER%", currency);

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
            }],
            "color": "#7f8da9",
            "dataLoader": {"url": poloneixChartApiForCurrency, "format": "json", "postProcess": postProcessCandle},
            "title": "Prices Data",
            "categoryField": "date",
            "stockEvents": postProcessTxData(buys, sells)
        },
            {
                "color": "#7ffdf9",
                "title": "Buys",
                "categoryField": "date",
                "dataProvider": buys
            }, {
                "color": "#755df9",
                "title": "Sells",
                "categoryField": "date",
                "dataProvider": sells
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
                "valueField": "close"
            }],

            "stockLegend": {
                "valueTextRegular": undefined,
                "periodValueTextComparing": "[[percents.value.close]]%"
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

        "chartScrollbarSettings": {
            "graph": "g1",
            "graphType": "line"
        },
        "chartCursorSettings": {
            "pan": true,
            "valueLineEnabled": true,
            "valueLineBalloonEnabled": true
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
