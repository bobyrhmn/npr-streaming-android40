var onSlideValue;
var source;
var currStreamedContents = new Array(15);
var i = 0;

/*var minSlider =  0;
var maxSlider = minSlider;*/ 

function stopTextStreaming(){
	if(source!== undefined){
		source.close();
		}
}


//https://github.com/webinista/Server-Sent-Events-Demos/blob/master/lasteventid/index.html
function startTextStreaming(){
 
	//$("#slider").attr("min", 0).slider("refresh");
	//$("#slider").attr("max", currStreamedContents.length).slider("refresh");

	source = new EventSource('http://skappsrv.towson.edu/npr/stream.php');

	source.onmessage = function(e) {
		updateCaptionAndSlider(e);
	};
	/*setTimeout(function() {
		source.onmessage = function (event) {
			document.getElementById("elemData").innerHTML += event.data;
		};
	}, 10000);*/
}


function updateCaptionAndSlider(e){
	var element = document.querySelector('#elemData');

	//http://stackoverflow.com/questions/2059743/detect-elements-overflow-using-jquery
	if( element.offsetHeight < (element.scrollHeight) || element.offsetWidth < (element.scrollWidth)){ // your element have overflow
		//if($("#elemData").isOverflowHeight()){
		//http://stackoverflow.com/questions/2112106/use-jquery-to-detect-container-overflow	

		document.getElementById("hiddenElemData").innerHTML = "";
		document.getElementById("hiddenElemData").innerHTML = document.getElementById("elemData").innerHTML;
		document.getElementById("elemData").innerHTML = "";



		//alert(document.getElementById("hiddenElemData").innerHTML);
	}
	else{ //Shown captions div
		document.getElementById("elemData").innerHTML += e.data;


	}
}
 

 

