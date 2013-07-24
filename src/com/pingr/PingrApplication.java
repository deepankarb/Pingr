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

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}
}
