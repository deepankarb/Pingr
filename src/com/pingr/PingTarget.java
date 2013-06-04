/**
 * 
 */
package com.pingr;

import java.net.InetAddress;

/**
 * Ping target info
 * 
 * @author bharddee
 * 
 */
public class PingTarget {

	public static enum STATUS {
		GREEN, YELLOW, ORANGE, RED
	};

	private InetAddress mAddress;
	private String mHostname;
	private STATUS mStatus;

	private float mRttAvg; // in ms
	private float mRttMin;
	private float mRttMax;
	private float mRttStdDev; // unused

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
		this.mStatus = mRttAvg > 1000 ? STATUS.ORANGE : STATUS.GREEN;
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

	public PingTarget(String mHostname) {
		super();
		this.mHostname = mHostname;
		this.mStatus = STATUS.RED;
	}

	@Override
	public boolean equals(Object o) {
		PingTarget in = (PingTarget) o;
		if (this.getHostname() == in.getHostname()) {
			return true;

		} else
			return false;
	}
}
