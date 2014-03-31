/**
 * 
 */
package org.zerogravity.pingr;

import java.util.Map;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.HttpSender;
import org.acra.sender.ReportSenderException;

import android.net.TrafficStats;
import android.util.Log;

/**
 * @author bharddee
 * 
 */
public class AcraTrafficMonitor extends HttpSender {

	private static final String TAG = AcraTrafficMonitor.class.getSimpleName();
	Integer appUid = -1;

	public Integer getAppUid() {
		return appUid;
	}

	public void setAppUid(Integer appUid) {
		this.appUid = appUid;
	}

	public AcraTrafficMonitor(Method method, Type type, String formUri,
			Map<ReportField, String> mapping) {
		super(method, type, formUri, mapping);
		// TODO Auto-generated constructor stub
	}

	// public AcraTrafficMonitor(Method method, Type type,
	// Map<ReportField, String> mapping) {
	// super(method, type, mapping);
	// }

	@Override
	public void send(CrashReportData arg0) throws ReportSenderException {
		// traffic log start
		long bctx = TrafficStats.getUidTxBytes(appUid);
		long bcrx = TrafficStats.getUidRxBytes(appUid);

		if (BuildConfig.DEBUG) {
			Log.v(TAG, "Before: tx: " + bctx + " rx: " + bcrx);
		}

		super.send(arg0);

		long adtx = TrafficStats.getUidTxBytes(appUid);
		long adrx = TrafficStats.getUidRxBytes(appUid);

		// traffic stop
		if (BuildConfig.DEBUG) {
			Log.v(TAG, "After: tx: " + adtx + " rx: " + adrx);
			Log.v(TAG, "Delta: tx: " + (adtx - bctx) + " rx: " + (adrx - bcrx));
		}

	}

}
