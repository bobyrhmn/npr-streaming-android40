var onSlideValue;
var source;
var currStreamedContents = new Array(15);
var i = 0;

/*var minSlider =  0;
var maxSlider = minSlider;*/ 

function stopTextStreaming(){
	source.close();
}


//https://github.com/webinista/Server-Sent-Events-Demos/blob/master/lasteventid/index.html
function startTextStreaming(){
	var id_param = getURLParameter('id');


	//$("#slider").attr("min", 0).slider("refresh");
	//$("#slider").attr("max", currStreamedContents.length).slider("refresh");

	if(id_param == null|| id_param == undefined){
		source = new EventSource('http://skappsrv.towson.edu/npr/stream.php');
	}
	else{
		source = new EventSource('http://skappsrv.towson.edu/npr/stream.php?id='+id_param);
	}
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


		if(i<currStreamedContents.length){ 
			currStreamedContents[i] = e.data;
			//set the slider value
			/*$('#slider').val(i);
			$('#slider').slider("refresh");*/


			//$( "#slider" ).slider( "value", i );

			i++;
		}else{
			i = 0;
		}
	}
}

function streamFromSliderValue(value){
	alert('slided value: ' + value);
	//source.close();
	document.getElementById("elemData").innerHTML = "";
}

//http://jsfiddle.net/timdown/VxTfu/
function getSelectedWordIndex() {
	var div = document.getElementById("content");

	if (sel.rangeCount) {
		// Get the selected range
		var range = sel.getRangeAt(0);

		// Check that the selection is wholly contained within the div text
		if (range.commonAncestorContainer == div.firstChild) {
			var precedingRange = document.createRange();
			precedingRange.setStartBefore(div.firstChild);
			precedingRange.setEnd(range.startContainer, range.startOffset);
			var textPrecedingSelection = precedingRange.toString();
			var wordIndex = textPrecedingSelection.split(/\s+/).length;
			alert("Word index: " + wordIndex);
		}
	}

}

function getURLParameter( name )
{
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec( window.location.href );
	if( results == null )
		return null;
	else
		return results[1];
}

