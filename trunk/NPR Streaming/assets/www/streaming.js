function startTextStreaming(){
	var id_param = getURLParameter('id');
	var source;
	if(id_param == null|| id_param == undefined){
		source = new EventSource('http://skappsrv.towson.edu/npr/stream.php');
	}
	else{
		source = new EventSource('http://skappsrv.towson.edu/npr/stream.php?id='+id_param);
	}
	source.onmessage = function(e) {
	document.getElementById("elemData").innerHTML += e.data;
};
	/*setTimeout(function() {
		source.onmessage = function (event) {
			document.getElementById("elemData").innerHTML += event.data;
		};
	}, 10000);*/
}

/*if (window.EventSource) {
	window.onload = function() {
		window.scrollTo(0,1);
		alert("Eventsource is working");
		setTimeout(function() {
			source.onmessage = function (event) {
				document.getElementById("elemData").innerHTML += event.data + "<br>";
			};
		}, 1000);
	};
} else {
	document.write("Please visit this page in a browser that supports EventSource to see the test");
}*/






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