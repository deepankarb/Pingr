/**
 * 
 */
package com.pingr;

import android.app.Application;

/**
 * @author bharddee Singleton application class
 * 
 */
public class PingrApplication extends Application {
	
	private static PingrApplication instance;	

	public static PingrApplication getInstance() {
		return instance;
	}

	public static int greenThreshold;
	public static int orangeThreshold;
	public static int redThreshold;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}
}
