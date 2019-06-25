package com.hiveview.factorytest;

import java.util.ArrayList;

 

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

 

public class ResultAdapter extends BaseAdapter {

 
	private Context mContext;
//	public String[] itemName = {"WIFI","ETHERNET","USB0","USB1","USB2","HDMI1","HDMI2","HDMI3","AUX","MINIAV"};
	
	public ArrayList<TestItem> testItems;
	
	
	public ResultAdapter(Context context,ArrayList<TestItem> items) {
		this.mContext = context;
        testItems = items;
	}

	public int getCount() {
		return testItems.size();
	}

	public Object getItem(int position) {
		return testItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup parent) {
		try {
			final ViewHolder viewHolder;
			if (view == null) {
				viewHolder = new ViewHolder();
				LayoutInflater layoutInflater = LayoutInflater.from(mContext);
				view = layoutInflater.inflate(
                       R.layout.result_list_item, null);

				viewHolder.name = (TextView) view.findViewById(R.id.itemname);
				viewHolder.status_icon = (TextView) view.findViewById(R.id.status);
				viewHolder.result = (TextView)view.findViewById(R.id.result);
				viewHolder.status = (TextView)view.findViewById(R.id.status2);

				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			viewHolder.name.setText(testItems.get(position).getItemname());
			if(testItems.get(position).getResult()!=null){
				viewHolder.result.setText(testItems.get(position).getResult());
				if(testItems.get(position).getResult().equals("通过")){
					viewHolder.result.setTextColor(mContext.getResources().getColor(R.color.settings_blue_007eff));
				}else {
					viewHolder.result.setTextColor(mContext.getResources().getColor(R.color.red));
				}
			}
			
			if(testItems.get(position).getStatus()!=null){
				viewHolder.status.setText(testItems.get(position).getStatus());
			}
            
		} catch (Exception e) {
			e.printStackTrace();
		}

		return view;

	}

	public class ViewHolder {
	
		TextView name;
		TextView status_icon;
		TextView result;
		TextView status;
	}
 

}