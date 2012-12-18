

var HelloPlugin = {
    
    callNativeFunction: function (success, fail, resultType) {
    	return cordova.exec(success, fail, "com.tricedesigns.HelloPlugin", "nativeAction", [resultType]);
    }
};