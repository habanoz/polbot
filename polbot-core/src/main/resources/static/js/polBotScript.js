

function isEmpty(str) {
    return (!str || 0 === str.length);
}

$(document).ready(function () {

bindUserCurrencyEvent();
function bindUserCurrencyEvent() {
    $('[data-user-currency]').each(function () {
        $(this).off("click");
        $(this).on("click", handleUserCurrencyEvent);
    });
}

function handleUserCurrencyEvent(e) {
    var caller = e.target;
    var userId = $(caller).attr('data-user-currency');
    var postData="";
    var ajaxUrl = "/GetAjaxUsers?botUserId="+parseInt(userId);
    ajaxMethodCall(postData,ajaxUrl, function (data) {

        var template = $('#userCurrenciesTemplate').html();
        var data = {currencies: JSON.parse(data.userCurrencies) };
        console.log(data);
        var result = Mustache.render(template, data);
        $("#userCurrencies").html(result);
    });
}


});

function ajaxMethodCall(postData, ajaxUrl, successFunction) {

    $.ajax({
        type: "GET",
        url: ajaxUrl,
     //   data: postData,
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        success: successFunction,
         beforeSend: function(xhr) {
                    xhr.setRequestHeader("Accept", "application/json");
                    xhr.setRequestHeader("Content-Type", "application/json");
                },
        error: function (jqXHR, exception) {
            console.error("parameters :" + postData);
            console.error("ajaxUrl :" + ajaxUrl);
            console.error("responseText :" + jqXHR.responseText);
            if (jqXHR.status === 0) {
                console.error('Not connect.\n Verify Network.');
            } else if (jqXHR.status == 404) {
                console.error('Requested page not found. [404]');
            } else if (jqXHR.status == 500) {
                console.error('Internal Server Error [500].');
            } else if (exception === 'parsererror') {
                console.error('Requested JSON parse failed.');
            } else if (exception === 'timeout') {
                console.error('Time out error.');
            } else if (exception === 'abort') {
                console.error('Ajax request aborted.');
            } else {
                console.error('Uncaught Error.\n' + jqXHR.responseText);
            }
        }
    });
}