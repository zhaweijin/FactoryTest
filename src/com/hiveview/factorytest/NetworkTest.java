package com.hiveview.factorytest;

import static android.net.ethernet.EthernetManager.ETH_STATE_ENABLED;

import java.util.Timer;
import java.util.TimerTask;
 
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkTest extends Activity{

	private static final String TAG = "NetworkTest";
	public static final int TEST_WIFI = 1;
	public static final int TEST_ETHERNET = 2;
	
	private int currentNetworkID;
	
	private Timer wifiLevelTimer;
	private WifiManager mWifiManager;
	public static final long WIFI_TIMEOUT = 120*1000l;
	public static final long WIFI_RECONNECT_TIMEOUT = 90*1000l;
	public static final long FIVE_WIFI_RECONNECT_TIMEOUT = 90*1000l;
	private volatile boolean wifi_enable = false;
	private volatile boolean wifi_do_work = true;
	private DhcpInfo mEthernetDhcpInfo = null;
	private WifiInfo wifiInfo = null;
	private TextView result;
	private TextView wifi_level;
	private TextView tips;
	private LinearLayout layout_input;
	
	private EthernetManager mEthernetManager;
	public static final long ETHERNET_TIMEOUT = 15*1000l;
	private volatile boolean ethernet_enable = false;
	private volatile boolean ethernet_do_work = true;
	public static final int EHTERNET_TRY_NUM = 1;
	
	private EditText ssid;
	private EditText password;
	private Button save;
	
	private LinearLayout contain;
	
/*	public static final String WIFI_SSID = "test"; 
	public static final String WIFI_PASSWORD = "12345678";*/
	
	public static final String WIFI_SSID = "test"; 
	public static final String WIFI_PASSWORD = "12345678";
	
	private BroadcastReceiver mNetworkReceiver, mWifiStateReceiver, mEthernetStateReceiver;
	private SharedPreferences preferences;
	
	private int index;
	
	public static final int MSG_WIFI_SCAN_RESULT = 0xff00;
	public static final int MSG_UPDATE_TIME = 0xff02;
	public static final int MSG_UPDATE_WIFI_RSSI = 0xff03;
	public static final int MSG_WIFI_TIMEOUT = 0xff04;
	public static final int MSG_ETHERNET_TIMEOUT = 0xff05;
	public static final int MSG_TEST_WIFI = 0xff06;
	public static final int MSG_UPDATE_WIFI_LEVEL = 0xff07;
	public static final int MSG_UPDATE_HDMI = 0xff08;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ETHERNET_TIMEOUT:
				result.setText("Ethernet失败测试，请检查网络连接设备");
				result.setTextColor(getResources().getColor(R.color.red));
				break;
			case MSG_WIFI_TIMEOUT:
				result.setText("WiFi测试失败，请检查路由器是否配置正确,或者 更新路由器名字再测试，请填写以下信息");
				result.setTextColor(getResources().getColor(R.color.red));
//				layout_input.setVisibility(View.VISIBLE);
				break;
			case MSG_UPDATE_WIFI_LEVEL:
				wifi_level.setText("WiFi信号量:"+wifiInfo.getRssi()+"db");
				break;
			}			
			super.handleMessage(msg);
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.other_main);
		contain = (LinearLayout)findViewById(R.id.contain);
		
		
		//设置工厂测试模式
	    SystemProperties.set("persist.sys.factory.mode", "1");
				
		mEthernetManager = (EthernetManager) getSystemService("ethernet");
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);	
		preferences = getSharedPreferences("systemsave", 0);
		
		index = getIntent().getIntExtra("index", 1);
		if(index == TEST_WIFI){
			initTestWift();
			testWifi();
		}else if(index == TEST_ETHERNET){
			testEthernet();
		}
 
		
		registerNetworkReceiver();
		registerWifiStateReceiver();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterWifiStateReceiver();
		unregisterNetworkReceiver();
		SystemProperties.set("persist.sys.factory.mode", "0");
		closeWifi();
		closeEthernet();
		
//		try {
//			if(index == TEST_WIFI){
//				mWifiManager.forget(currentNetworkID, mForgetListener);
//			}else if(index == TEST_ETHERNET){
//				 
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
		
	}
	
	WifiManager.ActionListener mForgetListener = new WifiManager.ActionListener() {
		public void onSuccess() {
		}

		public void onFailure(int reason) {
			 
		}
	};

	/*
	 * 测试以太网，先关闭以wifi,然后开启以太网动态获取IP，设定超时时间
	 * 根据连接网络的系统广播 ，检测以太网连接结果
	 */
	public void testEthernet(){
		View view = LayoutInflater.from(this).inflate(R.layout.ethernet_test, null);
		result = (TextView)view.findViewById(R.id.result);
		contain.removeAllViews();
		contain.addView(view);
		result.setText("正在测试Ethernet中......");
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(null != mEthernetManager){
					ethernet_do_work = false;
					closeWifi();
		 

					checkEthernetIsFaceState();
					openEthernet();

					Utils.print(TAG, "teset ethernet");
				    try {
						Thread.sleep(ETHERNET_TIMEOUT);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(!ethernet_do_work){
						// close ethernet
						closeEthernet();
						//UI display ethernet failed
						Utils.print(TAG, "test ethernet failed");
						mHandler.sendEmptyMessage(MSG_ETHERNET_TIMEOUT);
					}			
				}
			}
		}).start();

	}
	
	/*
	 * 测试wifi，先关闭以太网,然后开启wifi固定连接的热点，设定超时时间
	 * 根据连接网络的系统广播 ，检测wifi连接结果
	 */
	public void testWifi(){

		updateWifiTips();
		Log.v(TAG, "test wifi");
		layout_input.setVisibility(View.INVISIBLE);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					wifi_do_work = false;
					// close ethernet
//					closeWifi();
					closeEthernet();
		            //set wifi enable
		            openWifi();
		            Thread.sleep(WIFI_TIMEOUT);
		            Utils.print(TAG, "test wifi");
		            if(!wifi_do_work){
		            	//close wifi
		            	closeWifi();     
		            	mHandler.sendEmptyMessage(MSG_WIFI_TIMEOUT);
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
			}
		}).start();

	}

	
	
	
	
	private void initTestWift(){
		View view = LayoutInflater.from(this).inflate(R.layout.wifi_test, null);
		result = (TextView)view.findViewById(R.id.result);
		ssid = (EditText)view.findViewById(R.id.ssid);
		password = (EditText)view.findViewById(R.id.password);
		save = (Button)view.findViewById(R.id.save);
		tips = (TextView)view.findViewById(R.id.tips);
		wifi_level = (TextView)view.findViewById(R.id.wifi_level);
		layout_input = (LinearLayout)view.findViewById(R.id.layout_input);
		contain.removeAllViews();
		contain.addView(view);
		result.setText("正在测试wifi中......");
		
        save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 
					Log.v(TAG, "save ssid=="+ssid.getText().toString().trim()+",pwd="+password.getText().toString().trim());
					preferences.edit().putString("wifi_ssid", ssid.getText().toString().trim()).commit();
					preferences.edit().putString("wifi_password", password.getText().toString().trim()).commit();
					Toast.makeText(NetworkTest.this, "保存wifi信息成功,请重新测试", 2000).show();
				 
					result.setText("正在测试wifi中......");
					
					testWifi();
				
			}
		});
	}
	/*
	 * 连接WIFI，固定SSID、PASSWORD，加密方式WPA_PSK
	 */
	public void connectWifi(){

		try {
            Log.d(TAG, "connet wifi");
            startScanWifi();
            String temp_WIFI_SSID = preferences.getString("wifi_ssid", WIFI_SSID);
            String temp_WIFI_PASSWORD = preferences.getString("wifi_password", WIFI_PASSWORD);
//            Log.v(TAG, "connect ssid="+temp_WIFI_SSID+",password=="+temp_WIFI_PASSWORD);
            WifiConfiguration config1 = getConfig(temp_WIFI_SSID, temp_WIFI_PASSWORD);
            int networkId = mWifiManager.addNetwork(config1);
            // Set to the highest priority and save the configuration.
            int mLastPriority = 0;
            WifiConfiguration config = new WifiConfiguration();
            config.networkId = networkId;
            currentNetworkID = networkId;
            config.priority = ++mLastPriority;
            mWifiManager.updateNetwork(config);
            //save network
            
            mWifiManager.saveConfiguration();
            Utils.print(TAG, "enable wifi network id=="+networkId);
            // Connect to network by disabling others.
            mWifiManager.enableNetwork(networkId, true);
            mWifiManager.reconnect();

            /*Thread.sleep(WIFI_RECONNECT_TIMEOUT);
            
            if(DEBUG) Log.d(TAG, "wifi reconnect threadName." + Thread.currentThread().getName() +
					" sleep." + WIFI_RECONNECT_TIMEOUT);
            if(!wifi_do_work){
            	//close wifi
            	closeWifi();            	
            }  */          
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
	
	/*
	 * 获取WIFI信号量，检测WIFI是否达标，需要明确路由器与盒子的距离
	 * （其中0db到-50db表示信号最好，-50db到-70db表示信号偏差，
	 * 小于-70db表示最差，有可能连接不上或者掉线，一般Wifi已断则值为-200db）
	 */
	public void getWifiLevel(){
		wifiLevelTimer = new Timer();
		
		wifiLevelTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				wifiInfo = mWifiManager.getConnectionInfo();
				Utils.print(TAG, "connected ssid:"+wifiInfo.getSSID());
				//获得信号强度值
//				wifiInfo.getRssi();
				Utils.print(TAG, "level:"+wifiInfo.getRssi());
//				Toast.makeText(mContext, "level:"+wifiInfo.getRssi(), 1000).show(); 
				mHandler.sendEmptyMessage(MSG_UPDATE_WIFI_LEVEL);
			}

		}, 1000, 5000);
		
	}
	
	/*
	 * 确认wifi 连接是否成功，采用ping 网关的方式
	 */
	public void testEnableWifi(){
		
		
		DhcpInfo info  = mWifiManager.getDhcpInfo();
		if(info == null){
			Log.e(TAG, "mEthernetDhcpInfo is null");
			result.setText("WiFi测试失败，请检查路由器是否配置正确,或者 更新路由器名字再测试，请填写以下信息");
			result.setTextColor(getResources().getColor(R.color.red));
			return;
		}
		
		
		String requestSSID = "\"" + preferences.getString("wifi_ssid", WIFI_SSID) + "\"";
		if(!mWifiManager.getConnectionInfo().getSSID().equals(requestSSID)){
			result.setText("WiFi测试失败，请检查路由器是否配置正确,或者 更新路由器名字再测试，请填写以下信息");
			result.setTextColor(getResources().getColor(R.color.red));
			return;
		}
		
		getWifiLevel();
		String mGatewayAddress = Utils.getAddress(info.gateway);
		Utils.print(TAG, "wifi gateway=="+mGatewayAddress);
		if(Utils.pingHost(mGatewayAddress)){
			result.setText("WiFi测试通过");
		}else {
			result.setText("WiFi测试失败，请检查路由器是否配置正确,或者 更新路由器名字再测试，请填写以下信息");
			result.setTextColor(getResources().getColor(R.color.red));
		}
		
	}
	
	
	/*
	 * 确认以太网 连接是否成功，采用ping 网关的方式
	 */
	public void testEnableEthernet(){
		mEthernetManager.getDhcpInfo();
        Log.v(TAG, "testEnableEthernet");
		mEthernetDhcpInfo = mEthernetManager.getDhcpInfo();
		if(mEthernetDhcpInfo == null){
			Log.e(TAG, "mEthernetDhcpInfo is null");
			result.setText("Ethernet失败测试，请检查网络连接设备");
			result.setTextColor(getResources().getColor(R.color.red));
			return;
		}
		String mGatewayAddress = Utils.getAddress(mEthernetDhcpInfo.gateway);
		Utils.print(TAG, "ethernet gateway=="+mGatewayAddress);
        if(Utils.pingHost(mGatewayAddress)){
        	result.setText("Ethernet测试通过");
        }else {
        	result.setText("Ethernet失败测试，请检查网络连接设备");
        	result.setTextColor(getResources().getColor(R.color.red));
		}
	}
	
	
	private void registerNetworkReceiver(){
		// network ConnectivityManager
		mNetworkReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Log.d(TAG, "networkReceiver()");
				getNetworkConnState();
			}
		};
		IntentFilter networkFilter = new IntentFilter();
		networkFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(mNetworkReceiver, networkFilter);
		
	}
	
	private void unregisterNetworkReceiver(){
		// network ConnectivityManager
		if(null != mNetworkReceiver){
			unregisterReceiver(mNetworkReceiver);
			mNetworkReceiver = null;
		}
	}
	
	private void registerWifiStateReceiver(){ 
		mWifiStateReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				Log.d(TAG, "wifiStateReceiver() " + action);
	            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
	                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
	                		WifiManager.WIFI_STATE_UNKNOWN);	                
	                if(WifiManager.WIFI_STATE_ENABLED == wifistate){
	                	new Thread(new Runnable() {
							@Override
							public void run() {
								connectWifi();
							}
						}).start();
	                }
	            }
			}
		};
		IntentFilter wifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);		
        // The order matters! We really should not depend on this. :(
		wifiStateFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		wifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		wifiStateFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		wifiStateFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		registerReceiver(mWifiStateReceiver, wifiStateFilter);
	}
	
	private void unregisterWifiStateReceiver(){
		if(null != mWifiStateReceiver){
			unregisterReceiver(mWifiStateReceiver);
			mWifiStateReceiver = null;
		}
	}
	
 
	
	private void getNetworkConnState(){
		ConnectivityManager connec =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo info = connec.getActiveNetworkInfo();
		if(null != info){
			Log.d(TAG, "getActiveNetworkInfo() getTypeName: " + info.getTypeName() +
					" getState: " + info.getState() +
					" isAvailable: " + info.isAvailable() +
					" isConnected: " + info.isConnected() +
					" isConnectedOrConnecting: " + info.isConnectedOrConnecting() +
					" isFailover: " + info.isFailover() +
					" isRoaming: " + info.isRoaming());
			if(NetworkInfo.State.CONNECTED.equals(info.getState())){
				if("WIFI".equals(info.getTypeName().trim())){
					if(!wifi_do_work){
						wifi_do_work = true;
						testEnableWifi();
					}
				}
 
				if("ethernet".equals(info.getTypeName().trim())){
					Log.v(TAG, "aaa==ethernet_do_work=="+ethernet_do_work);
					if(!ethernet_do_work){
						ethernet_do_work = true;
						
						testEnableEthernet();
					}
				}
			}
		}else{
			Log.d(TAG, "getActiveNetworkInfo() info is null");
		}
 
	}    
    
    public static WifiConfiguration getConfig(String ssid, String password) {
        try {
            WifiConfiguration config = new WifiConfiguration();

            // config.SSID = AccessPoint.convertToQuotedString(
            // mAccessPoint.ssid);
            config.SSID = "\"" + ssid + "\"";
            Log.d(TAG,  "SSID." + config.SSID);
 
//            config.hiddenSSID = true;
            Log.d(TAG, "password." + password);
            if (password.length() != 0) {
            	config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }else{
            	config.allowedKeyManagement.set(KeyMgmt.NONE);
            }
            return config;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }
    
    private void openEthernet(){
    	Log.d(TAG, "openEthernet()");
        mEthernetManager.setEthEnabled(true);
    }
    
    private void closeEthernet(){
    	Log.d("aaa", "closeEthernet()");
        mEthernetManager.setEthEnabled(false);
    }
    
    private void closeWifi(){
    	if(mWifiManager.isWifiEnabled()){
    		Utils.print(TAG, "close wifi");
    		mWifiManager.setWifiEnabled(false);
    	}
    	if(wifiLevelTimer!=null)
    		wifiLevelTimer.cancel();
    }
    
    private void openWifi(){
    	if(!mWifiManager.isWifiEnabled()){
    		Utils.print(TAG, "open wifi");
    		mWifiManager.setWifiEnabled(true);
    	}
    }
    
    private void checkEthernetIsFaceState(){
    	
    	if (mEthernetManager.getEthState() == ETH_STATE_ENABLED) {
    		Utils.print(TAG, "close ethernet");
    		mEthernetManager.setEthEnabled(false);
    	}
    }
        
 
    
    private void startScanWifi(){
    	mWifiManager.startScan();
    }
    
    private void updateWifiTips(){
    	tips.setText("当前使用的默认WiFi\n"+
                     "路由器:    "+preferences.getString("wifi_ssid", WIFI_SSID) + "\n"+
                     "密码:     "+preferences.getString("wifi_password", WIFI_PASSWORD)+"\n"+
                     "加密方式:    "+"WPA/PSK"+"\n");
    }
    
    
    
}
