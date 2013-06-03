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

	public static class PingTask extends AsyncTask<PingTarget, Void, Void> {
		PipedOutputStream mPOut;
		PipedInputStream mPIn;
		LineNumberReader mReader;
		Process mProcess;
		String pingCmd = "/system/bin/ping";
		String pingCmdOpts = "-c10";

		// TextView mText = (TextView) findViewById(R.id.text);
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
		protected Void doInBackground(PingTarget... params) {
			try {
				mProcess = new ProcessBuilder()
						.command(pingCmd, pingCmdOpts, params[0].getHostname())
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
				} finally {
					mProcess.destroy();
					mProcess = null;
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
				// Is a line ready to read from the "ping" command?
				while (mReader.ready()) {
					String readLine = mReader.readLine();
					String minRtt;
					String maxRtt;
					String avgRtt;
					// if (BuildConfig.DEBUG){
					// Log.v(TAG, readLine);
					// }
					if (readLine.startsWith("rtt")) {
						readLine = readLine
								.substring(readLine.indexOf('='));
						minRtt = readLine.substring(2,
								readLine.indexOf('/')).trim();
						avgRtt = readLine.substring(readLine.indexOf('/'));
						avgRtt = avgRtt.substring(0,avgRtt.indexOf('/'));
						if (BuildConfig.DEBUG) {							
							Log.v(TAG, readLine + ":" + minRtt+":"+avgRtt);
						}
					}
					// This just displays the output, you should typically parse
					// it I guess.
					// mText.setText(mReader.readLine());
				}
			} catch (IOException t) {
				if (BuildConfig.DEBUG) {
					t.printStackTrace();
				}

			}
		}
	}

}
