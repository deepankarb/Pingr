/**
 * 
 */
package com.pingr;

import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/**
 * @author bharddee
 * 
 */
public class TargetListAdapter implements ListAdapter {

	private static final int MAX_TARGETS = 10;

	private List<PingTarget> targetList;

	public TargetListAdapter(List<PingTarget> inList) {
		if (inList != null && inList.size() > 0) {
			this.targetList = inList;
		}

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
	public Object getItem(int arg0) {
		if (arg0 > 0 && arg0 < targetList.size()) {
			return targetList.get(arg0);
		} else {
			return 0;
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		if (getCount() == 0)
			return true;
		else
			return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
