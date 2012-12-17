package com.phonegap.streaming;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.*;

public class StartStreamingActivity extends DroidGap {
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("NPR","Activity is created");
        super.onCreate(savedInstanceState);
        
        super.loadUrl("file:///android_asset/www/index.html");
        super.setIntegerProperty("loadUrlTimeoutValue", 70000);
    }
}