var BufferAudioPlugin = {
    
	bufferRadioFunc: function (success, fail, resultType) {	 
		       
	   	return cordova.exec(success, fail, "com.streaming.StreamAudio", "bufferRadio", [resultType]);
	},
	 
	stopRadioFunc: function (success, fail, resultType) {	 
	   
		return cordova.exec(success, fail, "com.streaming.StreamAudio", "stopRadio", [resultType]);
	}
    
};