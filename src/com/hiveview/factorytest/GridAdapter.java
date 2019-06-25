package com.hiveview.factorytest;

import java.util.ArrayList;

 
 
 

import com.hiveview.factorytest.MainActivity.GridTestItem;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

 

public class GridAdapter extends BaseAdapter {

 
	private Context mContext;
	
	public ArrayList<GridTestItem> gridTestItems;
	
	
	public GridAdapter(Context context,ArrayList<GridTestItem> items) {
		this.mContext = context;
		gridTestItems = items;
	}

	public int getCount() {
		return gridTestItems.size();
	}

	public Object getItem(int position) {
		return gridTestItems.get(position);
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
                       R.layout.grid_result_list_item, null);

				viewHolder.name = (TextView) view.findViewById(R.id.name);
				viewHolder.result = (TextView)view.findViewById(R.id.result);
				viewHolder.image = (ImageView)view.findViewById(R.id.image);
				viewHolder.soundResult = (TextView)view.findViewById(R.id.sound_result);

				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
            
			
			if(gridTestItems.get(position).getResult().equals("通过")){
				viewHolder.result.setTextColor(mContext.getResources().getColor(R.color.settings_blue_007eff));
			}else {
				viewHolder.result.setTextColor(mContext.getResources().getColor(R.color.red));
			}
			
			viewHolder.name.setText(gridTestItems.get(position).getItemname());
			viewHolder.result.setText(gridTestItems.get(position).getResult());
			if(gridTestItems.get(position).getBitmap()!=null){
				viewHolder.image.setImageBitmap(gridTestItems.get(position).getBitmap());
			}
		/*	if(gridTestItems.get(position).getSoundResult().equals("通过")){
				viewHolder.soundResult.setBackgroundResource(R.drawable.gougou);
			}else {
				viewHolder.soundResult.setBackgroundResource(R.drawable.chacha);
			}*/
			
			if(gridTestItems.get(position).getItemname().equals("AUX")){
				viewHolder.image.setVisibility(View.INVISIBLE);
				viewHolder.result.setVisibility(View.INVISIBLE);
			}else {
				viewHolder.image.setVisibility(View.VISIBLE);
				viewHolder.result.setVisibility(View.VISIBLE);
			}
			
            
		} catch (Exception e) {
			e.printStackTrace();
		}

		return view;

	}

	public class ViewHolder {
	
		TextView name;
		ImageView image;
		TextView result;
		TextView soundResult;
	}
 

}