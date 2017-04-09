//since 2010
//sticks 4h
poloneixChartApi = "https://poloniex.com/public?command=returnChartData&currencyPair=%CURR_HOLDER%&start=1262304000&end=9999999999&period=14400"


function postProcess(data, options) {
    for (index = 0; index < data.length; ++index) {
        data[index].date = new Date(data[index].date * 1000);
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
        responsive: {enabled: true},
        "theme": "light",
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
                "fromField": "weightedAverage",
                "toField": "value"
            }],
            "color": "#7f8da9",
            "dataLoader": {"url": poloneixChartApiForCurrency, "format": "json", "postProcess": postProcess},
            "title": currency,
            "categoryField": "date"
        }],
        "panels": [{
            "title": "Value",
            "showCategoryAxis": false,
            "percentHeight": 70,
            "valueAxes": [{
                "id": "v1",
                "dashLength": 5
            }],

            "categoryAxis": {
                "dashLength": 5
            },

            "stockGraphs": [{
                "type": "candlestick",
                "id": "g1",
                "openField": "open",
                "closeField": "close",
                "highField": "high",
                "lowField": "low",
                "valueField": "close",
                "lineColor": "#7f8da9",
                "fillColors": "#7f8da9",
                "negativeLineColor": "#db4c3c",
                "negativeFillColors": "#db4c3c",
                "fillAlphas": 1,
                "useDataSetColors": false,
                "comparable": true,
                "compareField": "value",
                "showBalloon": false,
                "proCandlesticks": true
            }],

            "stockLegend": {
                "valueTextRegular": undefined,
                "periodValueTextComparing": "[[percents.value.close]]%"
            }
        },
            {
                "title": "Volume",
                "percentHeight": 30,
                "marginTop": 1,
                "showCategoryAxis": true,
                "valueAxes": [{
                    "dashLength": 5
                }],

                "categoryAxis": {
                    "dashLength": 5,
                    "parseDates": "true",
                    "minPeriod": "hh",
                    "equalSpacing": "true"
                },

                "categoryAxesSettings": {
                    "minPeriod": "hh"
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
            "graphType": "line",
            "usePeriod": "WW"
        },

        "chartCursorSettings": {
            "valueLineBalloonEnabled": true,
            "valueLineEnabled": true
        },
        "export": {
            "enabled": true
        }
    });

    var periodSelector = new AmCharts.PeriodSelector();

    periodSelector.periods = [
        {period: "DD", count: 1, label: "1 day"},
        {period: "DD", selected: true, count: 5, label: "5 days"},
        {period: "MM", count: 1, label: "1 month"},
        {period: "YYYY", count: 1, label: "1 year"},
        {period: "YTD", label: "YTD"},
        {period: "MAX", label: "MAX"}
    ];
//periodSelector.addListener("changed", periodChanged);
    periodSelector.position = "bottom";
    chart.periodSelector = periodSelector;

}

