
function getURLParameter(name) {
	var u = decodeURI((RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]);
	if(u){
		//if(u.indexOf('://') >= 0){
		//	return u.substr(u.indexOf('://')+3);
		//}else{
			return u
		//}
	}else{
		return false;
	}
}
function reloadPage(u){
	
	var uu = u;
	if(u.indexOf('://') >= 0){
		uu = u.substr(u.indexOf('://')+3);
	}
	
	$.ajax({
		url: 'http://www.stalk.io:7777/list?site=' + uu , //encodeURIComponent(u),
		data: {},
		dataType: 'jsonp',
		crossDomain: 'true',
		beforeSend: function ( xhr ) {
			$('#urlTitle').html(' . . . . ');
		},
		error: function(jqXHR, exception) {
			var errorMessage = "";
            if (jqXHR.status === 0) {
            	errorMessage = 'Not connect.\n Verify Network.';
            } else if (jqXHR.status == 404) {
            	errorMessage = 'Requested page not found. [404]';
            } else if (jqXHR.status == 500) {
            	errorMessage = 'Internal Server Error [500].';
            } else if (exception === 'parsererror') {
            	errorMessage = 'Requested JSON parse failed.';
            } else if (exception === 'timeout') {
            	errorMessage = 'Time out error.';
            } else if (exception === 'abort') {
            	errorMessage = 'Ajax request aborted.';
            } else {
            	errorMessage = 'Uncaught Error.\n' + jqXHR.responseText;
            }
            errorMessage = errorMessage + ' Try again later.';
        },
		success: function (data) {
			
			var totalCnt = PageList.reload(data.count, data.data);
			LineChart.reload(totalCnt);
			
			setTimeout("reloadPage('"+u+"')",1000*3);
			
		}
	});
	
}

function getUrlTitle(u, c){

	if(c == "null"){
		c = "";
	}else{
		c = "and charset = '"+c+"'";
	}
	
	var yql = 'http://query.yahooapis.com/v1/public/yql?q=' + encodeURIComponent("select content from html where url = '"+u+"' "+c+" and xpath = '//title'") + '&format=json';
	
	$.ajax({
		url: yql,
		data: {},
		dataType: 'jsonp',
		crossDomain: 'true',
		beforeSend: function ( xhr ) {
			$('#urlTitle').html(' . . . . ');
		},
		error: function(jqXHR, exception) {
			var errorMessage = "";
            if (jqXHR.status === 0) {
            	errorMessage = 'Not connect.\n Verify Network.';
            } else if (jqXHR.status == 404) {
            	errorMessage = 'Requested page not found. [404]';
            } else if (jqXHR.status == 500) {
            	errorMessage = 'Internal Server Error [500].';
            } else if (exception === 'parsererror') {
            	errorMessage = 'Requested JSON parse failed.';
            } else if (exception === 'timeout') {
            	errorMessage = 'Time out error.';
            } else if (exception === 'abort') {
            	errorMessage = 'Ajax request aborted.';
            } else {
            	errorMessage = 'Uncaught Error.\n' + jqXHR.responseText;
            }
            errorMessage = errorMessage + ' Try again later.';
        },
		success: function (data) {
			if(parseInt(data.query.count, 10) > 0){
				$('#pageTitle').html(
						u+' <br><small class="text-info">'+data.query.results.title+' <a href="'+u+'" target="_black"><i class="icon-search"></i></a></small>');
			}
		}
	});
}

$(function() {

	var s = getURLParameter("site");
	var charset = getURLParameter("charset");
	$('#pageTitle').html(s);
	
   	window.prettyPrint && prettyPrint();
	LineChart.init('lineChart');
	PageList.init('pageList', s, charset );
	
	getUrlTitle(s, charset);
	
    reloadPage(s);
})

