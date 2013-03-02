var PageList = function() {
	
	var title = ''; //'<h3 class="title"><i class="icon-list"></i> &nbsp;&nbsp; PAGE LIST</h3>';
	var listSelector;
	var isStarted = false;
	var beforePageList = [];
	var siteUrl = "";
	var charsetQuery = "";
	
    return {
    	init: init,
        reload: reload,
        getPageTitle: getPageTitle
    };
    
    function init(selName, argSiteUrl, c) {
    	listSelector = $("#"+selName);
    	siteUrl = argSiteUrl; 

		if(c == "null"){
			charsetQuery = "";
		}else{
			charsetQuery = "and charset = '"+c+"'";
		}
    	
		
    }
    
    function drawInitTable(){
    	listSelector.html(title+'<table class="table table-hover"><thead><tr><th width="8%"></th><th width="30%">Path</th><th width="52%">Name</th><th width="10%">count</th></tr></thead><tbody></tbody></table>');
    }
    
    function drawRow(tableTbody, datas){
    	var data  = datas.split(",");
    	
    	if( $('#ST_'+jqSelector(data[0])).length > 0){ // existed!
    		
    		if( parseInt($('#CNT_'+jqSelector(data[0])).text(), 10) > parseInt(data[1].split("^")[1], 10)){
    			$('#ST_'+jqSelector(data[0])).html('<i class="icon-arrow-down"></i>');
    		}else if( parseInt($('#CNT_'+jqSelector(data[0])).text(), 10) < parseInt(data[1].split("^")[1], 10)){
    			$('#ST_'+jqSelector(data[0])).html('<i class="icon-arrow-up"></i>');
    		}else{
    			$('#ST_'+jqSelector(data[0])).html('<i class="icon-minus"></i>');
    		}
    		
    		$('#CNT_'+jqSelector(data[0])).text(data[1].split("^")[1]);
    		
    	}else{
    		tableTbody.prepend(
    			'<tr id="R_'+data[0]+'">'+
    			'<td id="ST_'+data[0]+'" class="st"><i class="icon-minus"></i></td>'+
    			'<td>'+data[0]+'</td>'+
    			'<td id="BTN_'+data[0]+'"><button onclick="PageList.getPageTitle(\''+data[0]+'\');" class="btn btn-small btn-success" type="button">Get page title</button></td>'+
    			'<td class="count"><span class="badge" id="CNT_'+data[0]+'" >'+data[1].split("^")[1]+'</span></td>'+
    			'</tr>'
    		);
    	}
    	
    	return parseInt(data[1].split("^")[1], 10);
    }
    
    
    var beforeDatas = {};
    function reload(cnt, dataArr) {
    	
    	if(!isStarted){ // the 1st action
    		
    		if(cnt == 0) {
    			listSelector.html('<h5><center>the number of connections is not existed</center></h5>');
    			return;
    		}
    		
    		drawInitTable();
    		isStarted = true;
    	}
    	
    	var tableTbody = $('#'+listSelector.attr("id")+' table tbody');
    	
    	
    	
    	var datas = {};
    	
    	var totalCnt = 0;
    	for (var i = 0; i < cnt; i++) {

    		var data = dataArr[i].split(",");
    		datas[data[0]] = data[1].split("^")[1]; 
    		
    		totalCnt = totalCnt + drawRow(tableTbody, dataArr[i]);
    	}
    	
    	for (var k in beforeDatas) {
    	    if (!datas.hasOwnProperty(k)) {
    	        //$('#CNT_'+jqSelector(k)).text('0');
    	    	$('#R_'+jqSelector(k)).remove();
    	    }
    	}
    	
    	beforeDatas = datas;
    	
    	return totalCnt;
    }
    
    function jqSelector(str)
    {
    	return str.replace(/([;&\/,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
    }

    function getPageTitle( p ) {
    	
    	var yql = 'http://query.yahooapis.com/v1/public/yql?q=' + encodeURIComponent("select content from html where url = '"+(siteUrl+p)+"' "+charsetQuery+" and xpath = '//title'") + '&format=json';
    	
    	$.ajax({
    		url: yql,
    		data: {},
    		dataType: 'jsonp',
    		crossDomain: 'true',
    		beforeSend: function ( xhr ) {
    			$('#BTN_'+jqSelector(p)).html('Loading . . . . ');
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
                $('#BTN_'+jqSelector(p)).html('<button onclick="PageList.getPageTitle(\''+p+'\');" class="btn btn-small btn-danger" type="button">Get page title</button> &nbsp;&nbsp; '+errorMessage);
            },
    		success: function (data) {
    			if(parseInt(data.query.count, 10) > 0){
    				$('#BTN_'+jqSelector(p)).html('<a href="'+(siteUrl+p)+'" target="_black">'+data.query.results.title+'</a>');
    			}else{
    				$('#BTN_'+jqSelector(p)).html('<button onclick="PageList.getPageTitle(\''+p+'\');" class="btn btn-small btn-danger" type="button">Get page title</button> &nbsp;&nbsp; Error is occured. Try again later.');
    			}
    		}
    	});
    	
    }


}();