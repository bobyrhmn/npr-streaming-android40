var BufferAudioPlugin = {
    callNativeFunction: function (success, fail, resultType) {
    	 
    	return cordova.exec(success, fail, "com.streaming.StreamAudio", "playAudio", [resultType]);
    }
};