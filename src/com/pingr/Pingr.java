/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.pingr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author bharddee
 * 
 */
public class Pingr {

	public static final String TAG = "Pingr";
	private static InetAddress targetAddress;
	private static int pingTimeout = 1000;
	private static boolean result;

	public static boolean pingIsReachable(final String host) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					targetAddress = InetAddress.getByName(host);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result = false;
					return;
				}

				try {
					if (targetAddress.isReachable(pingTimeout)) {
						result = true;
					} else {
						result = false;
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result = false;
					return;
				}
			}
		}).start();

		return result;
	}

	public static boolean pingIsReachable(String host, int timeout) {
		boolean result;

		try {
			targetAddress = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			if (targetAddress.isReachable(timeout)) {
				result = true;
			} else {
				result = false;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return result;

	}

	public static PingTarget pingAsyncTask(String host, int timeout) {
		PingTarget result = new PingTarget(host);
		new PingTask().execute(result);
		return result;
	}

	private static class PingTask extends AsyncTask<PingTarget, Void, Void> {
		PipedOutputStream mPOut;
		PipedInputStream mPIn;
		LineNumberReader mReader;
		Process mProcess;
		String pingCmd = "/system/bin/ping";
		PingTarget target;
		// Do not remove the -c (count) option
		String[] pingCmdOpts = {"-c10", "-i0.1"};

		List<String> commandLine = new ArrayList<String>();

		private int exitValue;
		private boolean statsAvailable = false;

		@Override
		protected void onPreExecute() {
			
			//disable ping button
			PingActivity.pingButton.setActivated(false);

			mPOut = new PipedOutputStream();
			try {
				mPIn = new PipedInputStream(mPOut);
				mReader = new LineNumberReader(new InputStreamReader(mPIn));
			} catch (IOException e) {
				if (BuildConfig.DEBUG) {
					e.printStackTrace();
				}

				cancel(true);
			}

		}
		public void stop() {
			Process p = mProcess;
			if (p != null) {
				p.destroy();
			}
			cancel(true);
		}

		@Override
		protected Void doInBackground(PingTarget... params) {

			target = params[0];

			// construct the command line
			commandLine.add(pingCmd);
			commandLine.addAll(Arrays.asList(pingCmdOpts));
			commandLine.add(target.getHostname());

			try {
				mProcess = new ProcessBuilder().command(commandLine)
						.redirectErrorStream(true).start();

				try {
					InputStream in = mProcess.getInputStream();
					OutputStream out = mProcess.getOutputStream();
					byte[] buffer = new byte[1024];
					int count;

					// in -> buffer -> mPOut -> mReader -> 1 line of ping
					// information to parse
					while ((count = in.read(buffer)) != -1) {
						mPOut.write(buffer, 0, count);
						publishProgress();
					}
					out.close();
					in.close();
					mPOut.close();
					mPIn.close();
					exitValue = mProcess.exitValue();
				} catch (IllegalThreadStateException e) {
					if (BuildConfig.DEBUG) {
						e.printStackTrace();
					}
					mProcess.destroy();
					mProcess = null;
				} finally {
					if (BuildConfig.DEBUG) {
						Log.v(TAG,
								"Ping terminated with : "
										+ String.valueOf(exitValue));
					}
				}
			} catch (IOException e) {
				if (BuildConfig.DEBUG) {
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			try {
				// Is a line ready to read from the "ping" commandLine?
				while (mReader.ready()) {
					String readLine = mReader.readLine();
					String minRtt;
					String maxRtt;
					String avgRtt;

					if (BuildConfig.DEBUG) {
						Log.v(TAG, readLine);
					}

					// Read result stats line 1
					if (readLine.contains("packets transmitted")) {
						// TODO
					}

					// Read result stats line 2
					else if (readLine.startsWith("rtt")) {
						
						statsAvailable  = true;

						// cut from '=' onwards for min/avg/max+
						minRtt = readLine.substring(readLine.indexOf('='));
						// cut from first '/' from above for avg/max+
						avgRtt = minRtt.substring(minRtt.indexOf('/'));
						// repeat above step for max+
						maxRtt = avgRtt.substring(avgRtt.indexOf('/', 1));
						// extract avg from avg/max+
						avgRtt = avgRtt.substring(1, avgRtt.indexOf('/', 1));
						// extract max from max+
						maxRtt = maxRtt.substring(1, maxRtt.indexOf('/', 1));
						// extract min from min/avg/max+
						minRtt = minRtt.substring(2, minRtt.indexOf('/'));

						if (BuildConfig.DEBUG) {
							Log.v(TAG, "min= " + minRtt + " avg= " + avgRtt
									+ " max= " + maxRtt);
						}

						target.setRttAvg(Float.valueOf(avgRtt));
						target.setRttMin(Float.valueOf(minRtt));
						target.setRttMax(Float.valueOf(maxRtt));
						PingActivity.adapter.notifyDataSetChanged();
					}
				}
			} catch (IOException t) {
				if (BuildConfig.DEBUG) {
					t.printStackTrace();
				}
			}
		} // end onProgressUpdate
		
		@Override
		protected void onPostExecute(Void result) {	
			super.onPostExecute(result);
			
			// if ping succeeded but stats weren't printed
			if (!statsAvailable && exitValue ==0) target.setStatusUnkown();
			
			//enable button
			PingActivity.pingButton.setActivated(true);
		}
	} // End async task
}
