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

package org.zerogravity.pingr;

import java.io.IOException;
import java.net.Socket;
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