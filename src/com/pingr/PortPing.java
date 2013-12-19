package com.pingr;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.os.AsyncTask;

/*
 * PortPing
 * Try to open a socket on the given host:port
 *
 */
public class PortPing {
	
	private static final int TIMEOUT = 2000;
	
	Socket mSocket;
	String mTarget;
	int mPort;

	int result = -1;
	
	public PortPing(String target, int port){
		this.mTarget = target;
		this.mPort = port;
	}
	
	public int ping(){		
		
		new portPingTask().execute((Void)null);
		return result;		
	}
	
	class portPingTask extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			result = -1;
			try {
				mSocket = new Socket(mTarget, mPort);
//				SocketAddress targetAddress = new InetSocketAddress(mTarget, mPort);
//				mSocket.connect(targetAddress, TIMEOUT);
				mSocket.close();
			} catch (UnknownHostException e) {
				result = result | 1 ;
				e.printStackTrace();
			} catch (IOException e) {
				result = result | 2;
				e.printStackTrace();
			} catch (Exception e) {
				result = result | 4;
				e.printStackTrace();
			}
			result = 0;
			return null;
		}		
	}
}