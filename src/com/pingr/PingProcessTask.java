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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.pingr.PingTarget.STATUS;

/**
 * @author bharddee
 * 
 */
class PingProcessTask extends AsyncTask<Void, Void, Void> {

	private static final String TAG = PingTarget.class.getName();

	PipedOutputStream mPOut;
	PipedInputStream mPIn;
	LineNumberReader mReader;
	Process mProcess;
	String pingCmd = "/system/bin/ping";
	PingTarget pingTarget;

	// Do not remove the -c (count) option
	String[] pingCmdOpts = { "-c10", "-i0.1" };

	List<String> commandLine = new ArrayList<String>();

	private int exitValue = -1;
	private boolean statsAvailable = false;

	public PingProcessTask(PingTarget pt) {
		this.pingTarget = pt;
	}

	@Override
	protected void onPreExecute() {

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
		commandLine.add(pingTarget.getHostname());
		// commandLine.add("2>&1");

		try {
			mProcess = new ProcessBuilder().command(commandLine)
					.redirectErrorStream(false).start();

			try {
				InputStream in = mProcess.getInputStream();
				InputStream err = mProcess.getErrorStream();
				OutputStream out = mProcess.getOutputStream();
				byte[] inBuf = new byte[1024];
				byte[] errBuf = new byte[1024];
				int inCount, errCount;

				// in -> buffer -> mPOut -> mReader -> 1 line of ping
				// information to parse
				while ((inCount = in.read(inBuf)) != -1) {
					mPOut.write(inBuf, 0, inCount);
					publishProgress();
				}
				while ((errCount = err.read(errBuf)) != -1) {
					mPOut.write(errBuf, 0, errCount);
					publishProgress();
				}

				out.close();
				in.close();
				err.close();
				mPOut.close();
				mPIn.close();
				exitValue = mProcess.waitFor();
				
			} catch (IllegalThreadStateException e) {
				if (BuildConfig.DEBUG) {
					e.printStackTrace();
				}
				mProcess.destroy();
				mProcess = null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (BuildConfig.DEBUG) {
					Log.v(TAG,
							"PingTarget terminated with : "
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

				else if (readLine.contains("not permitted")) {
					int port = 80;
					PortPing pp = new PortPing(pingTarget.getHostname(), port);
					int portResult = pp.ping();
					if (BuildConfig.DEBUG) {
						Log.v(TAG, "port "
								+ port
								+ (portResult == 0 ? " is OPEN"
										: " might be CLOSED ;)"));
					}
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
						Log.v(TAG + ": " + this.toString(), "min= " + minRtt
								+ " avg= " + avgRtt + " max= " + maxRtt);
					}

					pingTarget.setRttAvg(Float.valueOf(avgRtt));
					// mRttAvg = Float.valueOf(avgRtt);
					pingTarget.setRttMin(Float.valueOf(minRtt));
					// mRttMin = Float.valueOf(minRtt);
					pingTarget.setRttMax(Float.valueOf(maxRtt));
					// mRttMax = Float.valueOf(maxRtt);

					if (pingTarget.getRttAvg() < (float) PingrApplication.greenThreshold) {
						pingTarget.setStatus(STATUS.GREEN);
						if (pingTarget.mStatusChangeListener != null)
							pingTarget.mStatusChangeListener
									.onTargetStatusChange();

					} else if (pingTarget.getRttAvg() < (float) PingrApplication.orangeThreshold) {
						pingTarget.setStatus(STATUS.ORANGE);
						if (pingTarget.mStatusChangeListener != null)
							pingTarget.mStatusChangeListener
									.onTargetStatusChange();

					} else {
						pingTarget.setStatus(STATUS.RED);
						if (pingTarget.mStatusChangeListener != null)
							pingTarget.mStatusChangeListener
									.onTargetStatusChange();
					}
				}

				else {

					pingTarget.setStatus(STATUS.PING_IN_PROGRESS);

					if (pingTarget.mStatusChangeListener != null)
						pingTarget.mStatusChangeListener.onTargetStatusChange();
				}
			}

		} catch (IOException t) {

			if (BuildConfig.DEBUG) {
				t.printStackTrace();
			}
		}

//		if (exitValue == 0) {
//			pingTarget.setStatus(STATUS.YELLOW);
//		} else if (exitValue > 0) {
//			pingTarget.setStatus(STATUS.UNREACHABLE);
//		}
//		if (pingTarget.mStatusChangeListener != null)
//			pingTarget.mStatusChangeListener.onTargetStatusChange();
//		
	} // end onProgressUpdate

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		// if ping succeeded but stats weren't printed
		if (!statsAvailable && exitValue == 0) {
			pingTarget.setStatus(STATUS.YELLOW);
			if (pingTarget.mStatusChangeListener != null)
				pingTarget.mStatusChangeListener.onTargetStatusChange();
		}

		if (exitValue > 0) {
			pingTarget.setStatus(STATUS.UNREACHABLE);
			if (pingTarget.mStatusChangeListener != null)
				pingTarget.mStatusChangeListener.onTargetStatusChange();

		}
	}

} // End async task
