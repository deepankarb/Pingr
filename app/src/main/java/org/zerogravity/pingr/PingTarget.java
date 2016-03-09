package org.zerogravity.pingr;

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

import java.io.Serializable;
import java.net.InetAddress;

/**
 * @author bharddee
 * 
 */
public class PingTarget implements Serializable {

	public static enum STATUS {
		GREEN, YELLOW, ORANGE, RED, PING_IN_PROGRESS, UNKNOWN, UNREACHABLE;
	}

	public static final String TAG = PingTarget.class.getName();

	private static final long serialVersionUID = 0xdeadbeef;
	private InetAddress mAddress; // Target address
	private String mHostname;
	private int mPort; // Target port
	private STATUS mStatus;

	private float mRttAvg; // in ms
	private float mRttMin;
	private float mRttMax;
	private float mRttStdDev; // unused
	public PingTargetStatusChangeListener mStatusChangeListener;

	private PingTask pingTask;

	public PingTarget(String mHostname) {

		super();
		this.mHostname = sanitiseHostName(mHostname);
		this.mStatusChangeListener = null;
		this.mStatus = STATUS.UNKNOWN;
	}

	private String sanitiseHostName(String host) {
		String result = host;
		if (host.contains(":")) {
			host.split(":");
		}
		return result;
	}

	public PingTarget(String hostname, int port) {
		this(hostname);
		this.mPort = port;
	}

	/**
	 * @param mStatusChangeListener
	 *            the mStatusChangeListener to set
	 */
	public void setStatusChangeListener(
			PingTargetStatusChangeListener mStatusChangeListener) {
		this.mStatusChangeListener = mStatusChangeListener;
	}

	/**
	 * @return the mAddress
	 */
	public InetAddress getAddress() {
		return mAddress;
	}

	/**
	 * @param mAddress
	 *            the mAddress to set
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
	 * @return the mPort
	 */
	public int getPort() {
		return mPort;
	}

	/**
	 * @param mPort
	 *            the mPort to set
	 */
	public void setPort(int mPort) {
		this.mPort = mPort;
	}

	/**
	 * @return the mStatus
	 */
	public STATUS getStatus() {
		return mStatus;
	}

	/**
	 * @param mStatus
	 *            the mStatus to set
	 */
	public void setStatus(STATUS mStatus) {
		this.mStatus = mStatus;
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
		if (this.mRttAvg < (float) PingrApplication.greenThreshold) {
			this.mStatus = STATUS.GREEN;
			if (mStatusChangeListener != null)
				mStatusChangeListener.onTargetStatusChange();
		} else if (this.mRttAvg < (float) PingrApplication.orangeThreshold) {
			this.mStatus = STATUS.ORANGE;
			if (mStatusChangeListener != null)
				mStatusChangeListener.onTargetStatusChange();
		} else {
			this.mStatus = STATUS.RED;
			if (mStatusChangeListener != null)
				mStatusChangeListener.onTargetStatusChange();
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
	 * @return the mRttStdDev
	 */
	public float getRttStdDev() {
		return mRttStdDev;
	}

	/**
	 * @param mRttStdDev
	 *            the mRttStdDev to set
	 */
	public void setRttStdDev(float mRttStdDev) {
		this.mRttStdDev = mRttStdDev;
	}

	public boolean ping() {

		boolean result = false;

		if (this.pingTask == null) {
			pingTask = new PingTask(this);
			pingTask.execute((Void) null);
		}

		// PortPing pp = new PortPing(getHostname(), mPort);
		// int portResult = pp.ping();
		// if (BuildConfig.DEBUG) {
		// Log.v(TAG, "port " + mPort
		// + (portResult == 0 ? " is OPEN" : " might be CLOSED ;)"));
		// }

		return result;
	}

	public void requestPingAbort() {
		if (this.pingTask != null && !this.pingTask.isCancelled()) {
			this.pingTask.cancel(true);
		}
	}
}
