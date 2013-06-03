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
	private int mRtt; // or latency

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

	/**
	 * @param mStatus
	 *            the mStatus to set
	 */
	public void setStatus(STATUS mStatus) {
		this.mStatus = mStatus;
	}

	/**
	 * @return the mRtt
	 */
	public int getRtt() {
		return mRtt;
	}

	/**
	 * @param mRtt
	 *            the mRtt to set
	 */
	public void setRtt(int mRtt) {
		this.mRtt = mRtt;
	}

	public PingTarget(String mHostname) {
		super();
		this.mHostname = mHostname;
	}

	

}
