/*
 *	Copyright 2014 Deepankar Bhardwaj 
 * 
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License. 
 *	You may obtain a copy of the License at 
 * 	
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
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
