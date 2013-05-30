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

	private static enum STATUS {
		GREEN, YELLOW, ORANGE, RED
	};
	private InetAddress mAddress;
	private String mHostname;
	private int mStatus;
	private int mRtt; // or latency

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mAddress == null) ? 0 : mAddress.hashCode());
		result = prime * result
				+ ((mHostname == null) ? 0 : mHostname.hashCode());
		result = prime * result + mRtt;
		result = prime * result + mStatus;
		return result;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PingTarget other = (PingTarget) obj;
		if (mAddress == null) {
			if (other.mAddress != null)
				return false;
		} else if (!mAddress.equals(other.mAddress))
			return false;
		if (mHostname == null) {
			if (other.mHostname != null)
				return false;
		} else if (!mHostname.equals(other.mHostname))
			return false;
		if (mRtt != other.mRtt)
			return false;
		if (mStatus != other.mStatus)
			return false;
		return true;
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
	 * @return the mStatus
	 */
	public int getStatus() {
		return mStatus;
	}
	/**
	 * @param mStatus
	 *            the mStatus to set
	 */
	public void setStatus(int mStatus) {
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

}
