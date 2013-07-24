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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Ping target info
 * 
 * @author bharddee
 * 
 */
public class PingTarget {

	public static enum STATUS {
		GREEN, YELLOW, ORANGE, RED, PING_IN_PROGRESS, UNKNOWN, UNREACHABLE;
	}

	public static final String TAG = "PingTarget";

	private InetAddress mAddress;
	private String mHostname;
	private STATUS mStatus;

	private float mRttAvg; // in ms
	private float mRttMin;
	private float mRttMax;
	private float mRttStdDev; // unused

	private ArrayAdapter<PingTarget> mAdapter;

	public void setStatus(STATUS s) {
		this.mStatus = s;
	}

	/**
	 * @return the mRttAvg
	 */
	public float getRttAvg() {
		return mRttAvg;
	}

	/**
	 * @param mRttAvg
	 *            the mRttAvg to set
	 */
	public void setRttAvg(float mRttAvg) {
		this.mRttAvg = mRttAvg;
		if (this.mRttAvg < (float) PingActivity.greenThreshold) {
			this.mStatus = STATUS.GREEN;
		} else if (this.mRttAvg < (float) PingActivity.orangeThreshold) {
			this.mStatus = STATUS.ORANGE;
		} else {
			this.mStatus = STATUS.RED;
		}
	}

	/**
	 * @return the mRttMin
	 */
	public float getRttMin() {
		return mRttMin;
	}

	/**
	 * @param mRttMin
	 *            the mRttMin to set
	 */
	public void setRttMin(float mRttMin) {
		this.mRttMin = mRttMin;
	}

	/**
	 * @return the mRttMax
	 */
	public float getRttMax() {
		return mRttMax;
	}

	/**
	 * @param mRttMax
	 *            the mRttMax to set
	 */
	public void setRttMax(float mRttMax) {
		this.mRttMax = mRttMax;
	}

	/**
	 * @return the mAddress
	 */
	public InetAddress getAddress() {
		return mAddress;
	}

	/**
	 * @param mAddress
	 *            the mAddress to set private
	 */
	public void setAddress(InetAddress mAddress) {
		this.mAddress = mAddress;
	}

	/**
	 * @return the mHostname
	 */
	public String getHostname() {
		return mHostname;
	}

	/**
	 * @param mHostname
	 *            the mHostname to set
	 */
	public void setHostname(String mHostname) {
		this.mHostname = mHostname;
	}

	/**
	 * @return the mStatus
	 */
	public STATUS getStatus() {
		return mStatus;
	}

	// public PingTarget(String mHostname) {
	// super();
	// this.mHostname = mHostname;
	// this.mStatus = STATUS.PING_IN_PROGRESS;
	// }

	public PingTarget(String mHostname, ArrayAdapter<PingTarget> adapter) {
		super();
		this.mHostname = mHostname;
		this.mStatus = STATUS.UNKNOWN;
		this.mAdapter = adapter;
	}

	@Override
	public boolean equals(Object o) {
		PingTarget in = (PingTarget) o;
		if (this.getHostname() == in.getHostname()) {
			return true;

		} else
			return false;
	}

	public boolean ping() {

		boolean result = false;

		new PingTask().execute((Void) null);

		return result;
	}

	private class PingTask extends AsyncTask<Void, Void, Void> {

		PipedOutputStream mPOut;
		PipedInputStream mPIn;
		LineNumberReader mReader;
		Process mProcess;
		String pingCmd = "/system/bin/ping";

		// Do not remove the -c (count) option
		String[] pingCmdOpts = { "-c10", "-i0.1" };

		List<String> commandLine = new ArrayList<String>();

		private int exitValue;
		private boolean statsAvailable = false;

		@Override
		protected void onPreExecute() {

			// disable ping button
			if (PingActivity.pingButton.isEnabled())
				PingActivity.pingButton.setEnabled(false);

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
		protected Void doInBackground(Void... params) {

			// publishProgress();
			// construct the command line

			commandLine.add(pingCmd);
			commandLine.addAll(Arrays.asList(pingCmdOpts));
			commandLine.add(mHostname);

			try {
				mProcess = new ProcessBuilder().command(commandLine)
						.redirectErrorStream(false).start();

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
						Log.v(TAG + ": " + this.toString(),
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
						Log.v(TAG + ": " + this.toString(), readLine);
					}

					// Read result stats line 1
					if (readLine.contains("packets transmitted")) {
						// TODO
					}

					// Read result stats line 2
					else if (readLine.startsWith("rtt")) {

						statsAvailable = true;

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
							Log.v(TAG + ": " + this.toString(), "min= "
									+ minRtt + " avg= " + avgRtt + " max= "
									+ maxRtt);
						}

						// setRttAvg(Float.valueOf(avgRtt));
						mRttAvg = Float.valueOf(avgRtt);
						// setRttMin(Float.valueOf(minRtt));
						mRttMin = Float.valueOf(minRtt);
						// setRttMax(Float.valueOf(maxRtt));
						mRttMax = Float.valueOf(maxRtt);
						// PingActivity.adapter.notifyDataSetChanged();

						if (mRttAvg < (float) PingActivity.greenThreshold) {
							mStatus = STATUS.GREEN;
						} else if (mRttAvg < (float) PingActivity.orangeThreshold) {
							mStatus = STATUS.ORANGE;
						} else {
							mStatus = STATUS.RED;
						}

						if (mAdapter != null)
							mAdapter.notifyDataSetChanged();
					}

					else {
						// setStatus(STATUS.PING_IN_PROGRESS);
						mStatus = STATUS.PING_IN_PROGRESS;
						// PingActivity.adapter.notifyDataSetChanged();
						if (mAdapter != null)
							mAdapter.notifyDataSetChanged();
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
			if (!statsAvailable && exitValue == 0) {
				// setStatus(STATUS.UNKNOWN);
				mStatus = STATUS.UNKNOWN;
				// PingActivity.adapter.notifyDataSetChanged();
				if (mAdapter != null)
					mAdapter.notifyDataSetChanged();
			}

			if (exitValue != 0) {
				// setStatus(STATUS.UNREACHABLE);
				mStatus = STATUS.UNREACHABLE;
				// PingActivity.adapter.notifyDataSetChanged();
				if (mAdapter != null)
					mAdapter.notifyDataSetChanged();
			}

			// enable button
			if (!PingActivity.pingButton.isEnabled())
				PingActivity.pingButton.setEnabled(true);
		}

	} // End async task
}
