package com.hiveview.factorytest;

import java.util.ArrayList;
import java.util.List;

import com.hiveview.factorytest.ResultAdapter.ViewHolder;
import com.hiveview.manager.RootManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageParser.NewPermissionInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MenuMain extends Activity{

	private ListView listView;
	private MenuAdapter menuAdapter;
	private ArrayList<TestItem> items = new ArrayList<TestItem>();
	
	private WifiManager mWifiManager;
	private EthernetManager mEthernetManager;
	
	private LinearLayout layout_main_menu;
	private Button autoTest;
	private Button stepTest;
	private Button recovery;
	private Button set_manumode;
	
	private String TAG = "MenuMain";
	private boolean isMainMenu = true;
	
	private SharedPreferences preferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu_main);
   
		
		String[] name = getResources().getStringArray(R.array.menu_name);
		listView = (ListView)findViewById(R.id.list);
		layout_main_menu = (LinearLayout)findViewById(R.id.layout_main_menu);
		autoTest = (Button)findViewById(R.id.auto_test);
		stepTest = (Button)findViewById(R.id.step_test);
		recovery = (Button)findViewById(R.id.recovery);
		set_manumode = (Button)findViewById(R.id.set_manumode);
		 
		autoTest.setOnClickListener(onClickListener);
		stepTest.setOnClickListener(onClickListener);
		recovery.setOnClickListener(onClickListener);
		set_manumode.setOnClickListener(onClickListener);
		
		preferences = getSharedPreferences("systemsave", 0);
		mEthernetManager = (EthernetManager) getSystemService("ethernet");
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		checkNetwork();
		
		for(int i=0;i<name.length;i++){
			TestItem item = new TestItem();
			item.setItemname(name[i]);
			items.add(item);
		}

		menuAdapter = new MenuAdapter(this, items);
		listView.setAdapter(menuAdapter);
		menuAdapter.notifyDataSetChanged();

		listView.setOnItemClickListener(onItemClickListener);
		
		Intent intent = new Intent(MenuMain.this, OtherTest.class);
		intent.putExtra("index", OtherTest.TEST_TVINFO);

		if (intent != null)
			startActivity(intent);
	}
	
	
	AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = null;
			if(arg2==0) {
				intent = new Intent(MenuMain.this,WifiSetting.class);
				intent.putExtra("type", WifiSetting.MANUAL);
			}else if(arg2==1){
				intent = new Intent(MenuMain.this,NetworkTest.class);
				intent.putExtra("index", NetworkTest.TEST_ETHERNET);
			}else if(arg2==2){
				intent = new Intent(MenuMain.this,OtherTest.class);
				intent.putExtra("index", OtherTest.TEST_USB);
			}else if(arg2==3){
				intent = new Intent(MenuMain.this,HDMITest.class);
				intent.putExtra("index", HDMITest.HDMI1);
			}else if(arg2==4){
				intent = new Intent(MenuMain.this,HDMITest.class);
				intent.putExtra("index", HDMITest.HDMI2);
			}else if(arg2==5){
				intent = new Intent(MenuMain.this,HDMITest.class);
				intent.putExtra("index", HDMITest.HDMI3);
			}else if(arg2==6){
				intent = new Intent(MenuMain.this,HDMITest.class);
				intent.putExtra("index", HDMITest.AV);
			}else if(arg2==7){
				intent = new Intent(MenuMain.this,HDMITest.class);
				intent.putExtra("index", HDMITest.AUX);
			}else if(arg2==8){
				intent = new Intent(MenuMain.this,OtherTest.class);
				intent.putExtra("index", OtherTest.TEST_KEY);
			}else if(arg2==9){
				intent = new Intent(MenuMain.this,OtherTest.class);
				intent.putExtra("index", OtherTest.TEST_TVINFO);
			}
			if(intent!=null)
			   startActivity(intent);
		}
		
	};
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
	};
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	
	View.OnClickListener onClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.auto_test:
				Intent intent = new Intent(MenuMain.this,WifiSetting.class);
				intent.putExtra("type", WifiSetting.AUTO);
				startActivity(intent);
				break;
			case R.id.step_test:
				isMainMenu = false;
				layout_main_menu.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				break;
			case R.id.recovery:
				recovery();
				break;
			case R.id.set_manumode:
				Intent intent2 = new Intent(MenuMain.this,OtherTest.class);
				intent2.putExtra("index", OtherTest.TEST_MANU_MODE);
				startActivity(intent2);
				break;
			}
		}
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		recoveryNetworkStatus();
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(!isMainMenu){
				isMainMenu = true;
				layout_main_menu.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	class MenuAdapter extends BaseAdapter {

		 
		private Context mContext;
		public ArrayList<TestItem> testItems;
		
		
		public MenuAdapter(Context context,ArrayList<TestItem> items) {
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
	                       R.layout.menu_list_item, null);

					viewHolder.name = (TextView) view.findViewById(R.id.name);
					view.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) view.getTag();
				}

				viewHolder.name.setText(testItems.get(position).getItemname());
 
			} catch (Exception e) {
				e.printStackTrace();
			}

			return view;

		}

		public class ViewHolder {
		
			TextView name;
		}
	}
	
	
	private void recovery() {

		new AlertDialog.Builder(MenuMain.this)
				.setMessage(MenuMain.this.getResources().getString(R.string.confirm_recovery))
				.setPositiveButton(getResources().getString(R.string.confirm_ok),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
							}
						})
				.setNegativeButton(
						getResources().getString(R.string.confirm_cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stu
							}
				}).show();

	}
	
	
	
	private void checkNetwork(){
		
		if(mWifiManager.isWifiEnabled()){
			preferences.edit().putBoolean("wifi_status", true).commit();
		}
		
		if(mEthernetManager.getEthState() == EthernetManager.ETH_STATE_ENABLED){
			preferences.edit().putBoolean("ethernet_status", true).commit();
		}
	}
	
	
	
	private void recoveryNetworkStatus(){
		if(preferences.getBoolean("wifi_status", false)){
			preferences.edit().putBoolean("wifi_status", false).commit();  //记录恢复默认
			mWifiManager.setWifiEnabled(true);
		}
		
		if(preferences.getBoolean("ethernet_status", false)){
			preferences.edit().putBoolean("ethernet_status", false).commit();  //记录恢复默认
			mEthernetManager.setEthEnabled(true);
		}
		
		 
	}
	 
}
