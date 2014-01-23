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

package com.pingr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author bharddee
 * 
 */
public class TargetListAdapter extends ArrayAdapter<PingTarget> implements PingTargetStatusChangeListener {

	LayoutInflater mInflater;
	private Context context;
	private static final String TAG = "TargetListAdapter";
	private static final int MAX_TARGETS = 10;
	
	private class ViewHolder {
		TextView targetAddress;
		TextView targetRtt;
		ImageView rttLight;
	}
	
	public TargetListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = context;
		this.targetList = new ArrayList<PingTarget>();
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	
	public TargetListAdapter(Context context, int textViewResourceId,
			List<PingTarget> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.targetList = objects;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}



	private List<PingTarget> targetList;
	/**
	 * @return the targetList
	 */
	public List<PingTarget> getTargetList() {
		return targetList;
	}





	@Override
	public int getCount() {
		if (targetList != null) {
			return targetList.size();
		} else {
			return 0;
		}
	}

	@Override
	public PingTarget getItem(int arg0) {
		if (arg0 >= 0 && arg0 < targetList.size()) {
			return targetList.get(arg0);
		} else {
			return null;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		
		String targetRttText = new String();

		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.list_target, parent, false);
			holder = new ViewHolder();
			holder.targetAddress = (TextView) convertView
					.findViewById(R.id.text_target_address);
			holder.targetRtt = (TextView) convertView
					.findViewById(R.id.text_target_rtt);
			holder.rttLight = (ImageView) convertView
					.findViewById(R.id.image_target_rtt);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		switch (targetList.get(position).getStatus()) {		
		case UNKNOWN:
			targetRttText = "Status unknown";
			break;			
		case PING_IN_PROGRESS:
			targetRttText = "Pinging";
			break;
		case UNREACHABLE:
			targetRttText = "Unreachable";
			break;
		case YELLOW:
			targetRttText = "Host is up";
			break;
		default:
			targetRttText = targetList.get(position)
			.getRttAvg()==0.0f?"Not reachable":String.valueOf(targetList.get(position)
			.getRttAvg())+ " ms";
			break;
		}

		targetList.get(position).setStatusChangeListener(this);
		holder.targetAddress.setText(targetList.get(position).getHostname());
		holder.targetRtt.setText(targetRttText);
		holder.rttLight.setImageDrawable(getStatusImageDrawable((targetList
				.get(position))));

		return convertView;

	}
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return MAX_TARGETS;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public long getItemId(int position) {	
		return position;
	}
	
	

	@Override
	public boolean isEmpty() {
		if (getCount() == 0)
			return true;
		else
			return false;
	}

	private Drawable getStatusImageDrawable(PingTarget p) {
		Drawable result;
		if (p.getStatus() == null) return context.getResources().getDrawable(R.drawable.rtt_red);
		switch (p.getStatus()) {

			case GREEN :
				result = context.getResources().getDrawable(
						R.drawable.rtt_green);
				break;
			case ORANGE :
				result = context.getResources().getDrawable(
						R.drawable.rtt_orange);
				break;
			case RED :
				result = context.getResources().getDrawable(R.drawable.rtt_red);
				break;
			case YELLOW :
				result = context.getResources().getDrawable(
						R.drawable.rtt_yellow);
				break;
//			case UNKNOWN:
//				result = context.getResources().getDrawable();
//				break;
			default :
				result = context.getResources().getDrawable(R.drawable.rtt_red);
				break;
		}
		return result;
	}

	public void addTarget(PingTarget in) {

		// let's assume this is a new target
		boolean newItem = true;
		
		// now look for it in the list
		for (PingTarget p: targetList)
		{
			// and if already present in the list
			if (p.getHostname().trim().equals(in.getHostname().trim())){
				p = in; // replace it
				notifyDataSetChanged();
				// it's not new anymore
				newItem = false;
			}
		}	

		// if not found in the list
		if (newItem){			
			
			// add it
			this.targetList.add(in);
			notifyDataSetChanged();
		}			
		
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Total scanned tragets : " + targetList.size());
		}		
		
	}

	@Override
	public void onTargetStatusChange() {
		notifyDataSetChanged();
	}
}
