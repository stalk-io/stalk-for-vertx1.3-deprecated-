var LineChart = function() {
  
	var linechart;
	var linechartDt = [];
	var linechartSel;
	
	var colors = ['#94BA65', '#2B4E72', '#2790B0', '#777', '#555', '#999', '#bbb', '#ccc', '#eee'];

    return {
    	init: init,
        reload: reload
    };
    
    function init(selName) {

        var dt = [[new Date().getTime(), 0]]
        
        var data = [{
            data: dt,
            label: 'connections',
            points: {
                show: false
            },
            lines: {
                lineWidth: 2,
                fill: false
            }
        }];

        var options = {
		    colors: colors,
		    series: {
		        lines: {
		            show: true,
		            fill: true,
		            lineWidth: 4,
		            steps: false,
		            fillColor: {
		                colors: [{
		                    opacity: 0.4
		                }, {
		                    opacity: 0
		                }]
		            }
		        },
		        points: {
		            show: true,
		            radius: 4,
		            fill: true
		        }
		    },
		    legend: {
		        position: 'ne'
		    },
		    tooltip: true,
		    tooltipOpts: {
		        content: '%y  (%x)'
		    },
		    xaxis: {
		        mode: "time"
		    },
		    grid: {
		        borderWidth: 2,
		        hoverable: true
		    }
		};
		
        linechartSel = $('#'+selName);
		
		if (linechartSel.length) {
			linechart = $.plot(linechartSel, data, options);
		}
    }
    
    function reload(cnt) {
    	
        if (linechartSel.length) {
        	
        	if(linechartDt.length > 20){
        		linechartDt.shift(); 
        	}
        	
        	linechartDt.push([new Date().getTime(), cnt]);
        	
        	linechart.setData([linechartDt]);
        	linechart.setupGrid();
    		linechart.draw();
    		
        }
    }

}();
