package com.hiveview.factorytest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.SystemProperties;
import junit.framework.Assert;
 
import com.hiveview.manager.SystemInfoManager;
import com.hiveview.manager.UsbDeviceManager;
import com.hiveview.manager.Usbdevice;

import android.R.integer;
import android.amlogic.Tv;
import android.amlogic.Tv.SIG_LINE_STATUS;
import android.amlogic.Tv.SigInfoChangeListener;
import android.amlogic.Tv.SigLineChangeListener;
import android.amlogic.Tv.SourceInput;
import android.amlogic.Tv.SourceSwitchListener;
import android.amlogic.Tv.StatusSourceConnectListener;
import android.amlogic.Tv.VFrameEvent;
import android.amlogic.Tv.VframBMPEventListener;
import android.amlogic.Tv.tvin_info_t;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetDevInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
//import com.android.internal.os.storage.ExternalStorageFormatter;
//import android.os.SystemProperties;
import static android.net.ethernet.EthernetManager.ETH_STATE_DISABLED;
import static android.net.ethernet.EthernetManager.ETH_STATE_ENABLED;
import static android.net.ethernet.EthernetManager.ETH_STATE_UNKNOWN;

public class MainActivity extends Activity{
	public static final String TAG = "MainActivity";
	public static final boolean DEBUG = true;
	private static HandlerThread sHandlerThread = new HandlerThread("factorytest");
	static {
		sHandlerThread.start();
	}
	private static Handler sHandler = new Handler(sHandlerThread.getLooper());
	private ArrayList<TestItem> mTestItems = new ArrayList<TestItem>();
	private ArrayList<GridTestItem> mGridTestItems = new ArrayList<MainActivity.GridTestItem>();
	
	public String[] itemName = {"1.WIFI*******************",
			                     "2.ETHERNET*******************************",
			                     "3.USB0***********************************",
			                     "4.USB1***********************************",
			                     "5.USB2************************************"};
	/*"HDMI1**************************************",
    "HDMI2**************************************",
    "HDMI3**************************************",
    "AUX****************************************",
    "MINIAV*************************************"*/
	
	private ResultAdapter resultAdapter = new ResultAdapter(this,mTestItems);
	private GridAdapter gridAdapter = new GridAdapter(this, mGridTestItems);
	//
	private DhcpInfo mEthernetDhcpInfo = null;
	private WifiInfo wifiInfo = null;		//获得的Wifi信息
	private Timer wifiLevelTimer;
	private String USB1 = "1";
	private String USB2 = "2";
	private String USB4 = "4";
	private ListView listView;
	
	private GridView gridView;
	public String[] itemName2 = {
			"HDMI1",
            "HDMI2",
            "HDMI3",
            "MINIAV"};
	public static final int VIDEO_HOLE_REAL  = 0x102;
	
//	public Tv tv = PublicUtil.getTvInstance();
//	
//	private SourceInput[] SourceList = { Tv.SourceInput.HDMI1, Tv.SourceInput.HDMI2,
//			Tv.SourceInput.HDMI3, Tv.SourceInput.AV1 };
	private int SourceIdx;
	
	private String HDMI1 = "1";
	private String HDMI2 = "2";
	private String HDMI3 = "3";
	private String AV = "4";
	
	private SharedPreferences preferences;
	private SharedPreferences wifiPreferences;
	private int currentNetworkID=0;
	
	public static final int FILE_CONTENT_LEN = 256;
	public static final String USB_FILE_UDISK0 = "udisk0";
	
	private Context mContext;
	private BroadcastReceiver mNetworkReceiver, mWifiStateReceiver, mEthernetStateReceiver;
	public static final int HTTP_TIMEOUT = 5*1000;
	public static final int HTTP_TRY_SLEEP = 1;
	private EthernetManager mEthernetManager;
	public static final long ETHERNET_TIMEOUT = 15*1000l;
	private volatile boolean ethernet_enable = false;
	private volatile boolean ethernet_do_work = true;
	public static final int EHTERNET_TRY_NUM = 1;
	
	public static final String WIFI_SSID_DEFAULT = "GTtest";
	public static final String WIFI_SSID_PASSWORD_DEFAULT = "";
 
	
	public static final String WIFI_SSID = "test";//SystemProperties.get("ro.imt.wifi.ssid", WIFI_SSID_DEFAULT);
	public static final String WIFI_PASSWORD = "12345678";//SystemProperties.get("ro.imt.wifi.password", WIFI_SSID_PASSWORD_DEFAULT);
 
	private WifiManager mWifiManager;
	public static final long WIFI_TIMEOUT = 120*1000l;
	public static final long WIFI_RECONNECT_TIMEOUT = 90*1000l;
	public static final long FIVE_WIFI_RECONNECT_TIMEOUT = 90*1000l;
	private volatile boolean wifi_enable = false;
	private volatile boolean wifi_do_work = true;

	private Button mBtnRetest;
 

	private TextView mTvTimes;
	private long mTimes;
	private boolean isTested;
	
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
			case MSG_WIFI_SCAN_RESULT:
			case MSG_UPDATE_WIFI_RSSI:				
//				fillWifiScanList();
				break;
			case MSG_UPDATE_TIME:
				mTimes++;
//				mTvTimes.setText("" + mTimes);
				Log.v(TAG, "mTimes=="+mTimes);
				if(!isTested){
					Message message = mHandler.obtainMessage();
					message.what = MSG_UPDATE_TIME;
		            sendMessageDelayed(message, 1*1000l);
				}
				break;
			case MSG_ETHERNET_TIMEOUT:
				mTestItems.get(1).setResult("未通过");
				resultAdapter.notifyDataSetChanged();
				break;
			case MSG_WIFI_TIMEOUT:
				mTestItems.get(0).setResult("未通过");
				resultAdapter.notifyDataSetChanged();
				break;
			case MSG_TEST_WIFI:
				testWifi();
				break;
			case MSG_UPDATE_WIFI_LEVEL:
				mTestItems.get(0).setStatus("信号量:"+wifiInfo.getRssi()+"db");
				resultAdapter.notifyDataSetChanged();
				break;
			case MSG_UPDATE_HDMI:
				Bundle bundle = (Bundle)msg.obj;
				String port = bundle.getString("port");
				Boolean status = bundle.getBoolean("status");
				if(port.equals(HDMI1) && !status){
					mGridTestItems.get(0).setResult("未通过");
				}else if(port.equals(HDMI2) && !status){
					mGridTestItems.get(1).setResult("未通过");
				}else if(port.equals(HDMI3) && !status){
					mGridTestItems.get(2).setResult("未通过");
				}
				gridAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}			
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mContext = this;
		 
 
		SystemProperties.set("persist.sys.factory.mode", "1");
 
		mEthernetManager = (EthernetManager) mContext.getSystemService("ethernet");
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);	
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		wifiPreferences  = getSharedPreferences("systemsave", 0);
		
		mBtnRetest = (Button) findViewById(R.id.test);
		mBtnRetest.setVisibility(View.GONE);
		
		mBtnRetest.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//unableBtnRetest();
				testAll();
//				Utils.writeMACorSN("DMA30104140700055", "sn");
 
			}
		});
 
 
		registerNetworkReceiver();
		registerWifiStateReceiver();
 
		initAll();
		testAll();
		
		if(getIntent().getBooleanExtra("testhdmi",false)){
			Utils.print(TAG, "test other");
			mBtnRetest.setVisibility(View.GONE);
			testOther();
		}
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterWifiStateReceiver();
		unregisterNetworkReceiver();
		
		closeWifi();
		closeEthernet();
 

		mHandler.removeMessages(MSG_UPDATE_TIME);
		preferences.edit().clear();
		deleteTempFile();
		SystemProperties.set("persist.sys.factory.mode", "0");
	
	}
 
	
	private void deleteTempFile(){
		
		for(int i=0;i<itemName2.length;i++){
			File file = new File("mnt/sdcard/"+i+".jpg");
			if(file.exists())
				file.delete();
		}
		
		
	}
   
	
 
	
	public void testFileOfUsbStorage(){		
		try {
			UsbDeviceManager usbDeviceManager = UsbDeviceManager.getUsbDeviceManager();
			Log.v(TAG, "usb devices size="+usbDeviceManager.getUsbDevicePool().size());
			for(int i=0;i<usbDeviceManager.getUsbDevicePool().size();i++){
				Usbdevice usbdevice = usbDeviceManager.getUsbDevicePool().get(i);
				Log.v(TAG, "usburi=="+usbdevice.usburi);
				Log.v(TAG, "usbport=="+usbdevice.usbport);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	

	//从电源口开始，USB4 USB2 USB1
	/*
	 * private String USB1 = "1";
	 * private String USB2 = "2";
	 * private String USB4 = "4";
	 * port=1 or 2 or 4 
	 */
	public void testUsb(String port){
		Utils.print(TAG, "bbbbb");
		try {
			UsbDeviceManager usbDeviceManager = UsbDeviceManager.getUsbDeviceManager();
			Log.v(TAG, "usb devices size="+usbDeviceManager.getUsbDevicePool().size());
			
			Usbdevice testuUsbdevice=null;
			
			for(int i=0;i<usbDeviceManager.getUsbDevicePool().size();i++){
				Usbdevice usbdevice = usbDeviceManager.getUsbDevicePool().get(i);
				Log.v(TAG, "usburi=="+usbdevice.usburi);
				Log.v(TAG, "usbport=="+usbdevice.usbport);
				
				if(usbdevice.usbport!=null && usbdevice.usbport.equals(port)){
					testuUsbdevice = usbdevice;
					break;
				}
			}
			
			
			if(testuUsbdevice==null){
				Utils.print(TAG, "usb devices null");
				if(port.equals(USB1)){
				   mTestItems.get(2).setResult("未通过");
				}else if(port.equals(USB2)){
				   mTestItems.get(3).setResult("未通过");
				}else if(port.equals(USB4)){
				   mTestItems.get(4).setResult("未通过");
				}
				return;
			}
			
			if(writeData(testuUsbdevice.usburi, "writetest.txt", "writetest") 
						&& readData(testuUsbdevice.usburi, "writetest.txt", "writetest")){
				if(port.equals(USB1)){
					mTestItems.get(2).setResult("通过");
				}else if(port.equals(USB2)){
					mTestItems.get(3).setResult("通过");
				}else if(port.equals(USB4)){
					mTestItems.get(4).setResult("通过");
				}
				Utils.print(TAG, port+"===read and write ok");
		    }else {
		    	if(port.equals(USB1)){
					 mTestItems.get(2).setResult("未通过");
				}else if(port.equals(USB2)){
				     mTestItems.get(3).setResult("未通过");
				}else if(port.equals(USB4)){
					 mTestItems.get(4).setResult("未通过");
				}
		    	Utils.print(TAG, port+"===read and write failed");
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		resultAdapter.notifyDataSetChanged();
	}
	
	
 
	/*
	 * 确认wifi 连接是否成功，采用ping 网关的方式
	 */
	public void testEnableWifi(){

		DhcpInfo info  = mWifiManager.getDhcpInfo();
		if(info == null){
			Log.e(TAG, "mEthernetDhcpInfo is null");
			mTestItems.get(0).setResult("未通过");
			return;
		}
		
		String requestSSID = "\"" + wifiPreferences.getString("wifi_ssid", WIFI_SSID) + "\"";
		if(!mWifiManager.getConnectionInfo().getSSID().equals(requestSSID)){
			mTestItems.get(0).setResult("未通过");
			return;
		}
		getWifiLevel();
		String mGatewayAddress = Utils.getAddress(info.gateway);
		Utils.print(TAG, "wifi gateway=="+mGatewayAddress);
		if(Utils.pingHost(mGatewayAddress)){
			mTestItems.get(0).setResult("通过");
		}else {
			mTestItems.get(0).setResult("未通过");
		}
		
		resultAdapter.notifyDataSetChanged();
		
	}
	
	
	/*
	 * 确认以太网 连接是否成功，采用ping 网关的方式
	 */
	public void testEnableEthernet(){
		mEthernetManager.getDhcpInfo();
 
		mEthernetDhcpInfo = mEthernetManager.getDhcpInfo();
		if(mEthernetDhcpInfo == null){
			Log.e(TAG, "mEthernetDhcpInfo is null");
			mTestItems.get(1).setResult("未通过");
			return;
		}
		String mGatewayAddress = Utils.getAddress(mEthernetDhcpInfo.gateway);
		Utils.print(TAG, "ethernet gateway=="+mGatewayAddress);
        if(Utils.pingHost(mGatewayAddress)){
        	mTestItems.get(1).setResult("通过");
        }else {
        	mTestItems.get(1).setResult("未通过");
		}
        resultAdapter.notifyDataSetChanged();
        
        testWifi();
	}
	
	/*
	 * 测试以太网，先关闭以wifi,然后开启以太网动态获取IP，设定超时时间
	 * 根据连接网络的系统广播 ，检测以太网连接结果
	 */
	public void testEthernet(){
		mTestItems.get(0).setResult("");
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
						mHandler.sendEmptyMessage(MSG_TEST_WIFI);
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
		mTestItems.get(0).setResult("测试中......");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					wifi_do_work = false;
					// close ethernet
					closeWifi();
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

	/*
	 * 连接WIFI，固定SSID、PASSWORD，加密方式WPA_PSK
	 */
	public void connectWifi(){

		try {
            if(DEBUG) Log.d(TAG, "connet wifi");
            startScanWifi();
            String temp_WIFI_SSID = wifiPreferences.getString("wifi_ssid", WIFI_SSID);
            String temp_WIFI_PASSWORD = wifiPreferences.getString("wifi_password", WIFI_PASSWORD);
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
            Utils.print(TAG, "enable wifi network");
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
	 * 测试USB 写数据
	 */
	private boolean writeData(final String filePath, final String fileName, final String content){
		boolean result = false;
		File file = new File(filePath+File.separator+fileName);
		Utils.print(TAG, "writefile=="+file.getAbsolutePath());
		if(null != file){
				    
			FileOutputStream outStream = null;
	    	try {
	    		if(!file.exists()){
					file.createNewFile();
				}

	    		outStream = new FileOutputStream(file);
	    		byte[] buffer= new byte[FILE_CONTENT_LEN];
	    		buffer = content.getBytes();
	    		final int len = buffer.length;
	    		if(DEBUG) Log.d(TAG, "write buffer.len." + len);
	    		if(len <= FILE_CONTENT_LEN){
	    			outStream.write(buffer, 0, len);
	    		}
				outStream.close();
				outStream = null;
				result = true;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(null != outStream){
					try {
						outStream.close();
						outStream = null;
					} catch (Exception e2) {
						e2.printStackTrace();
					}						
				}
			}
		}
		Utils.print(TAG, "write result=="+result);
		return result;
	}
	
	/*
	 * 测试USB 读数据
	 */
	private boolean readData(final String filePath, final String fileName, final String content){
		boolean result = false;
		File file = new File(filePath+File.separator+fileName);
		if(null != file){
			if(file.exists()){
				FileInputStream inStream = null;	    	
		    	try {
		    		byte[] buffer= new byte[FILE_CONTENT_LEN];		    	
		    		inStream = new FileInputStream(file);
		    		int len = 0, totalLen = 0;
		    		while(((len = inStream.read(buffer))!= -1)){
		    			totalLen += len;
		        		if(DEBUG) Log.d(TAG, "read len." + len + " totalLen." + totalLen);
		        	}		    		
		    		inStream.close();
		    		inStream = null;
		    		if(totalLen <= FILE_CONTENT_LEN){
						String readContent = new String(buffer, 0, totalLen);
						if(DEBUG) Log.d(TAG, "content:" + content + " readContent:" + readContent);					
						if(content.trim().equals(readContent.trim())){
							result = true;
						}
		    		}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if(null != inStream){
						try {
							inStream.close();
							inStream = null;
						} catch (Exception e2) {
							e2.printStackTrace();
						}						
					}
				}
			}			
		}
		Utils.print(TAG, "read result=="+result);
		return result;
	}
	
	private void initAll(){
		mTvTimes = (TextView)findViewById(R.id.testtime);
		listView = (ListView)findViewById(R.id.list);
		
		gridView = (GridView)findViewById(R.id.gridview);
		 
	}
	
	private void testAll() {

		//先循环HDMI 以及AV AUX设备，然后再进入自动化过程
		if(!getIntent().getBooleanExtra("testhdmi",false)){
			testHDMI();
		}else {
			Utils.print(TAG, "test other");
			mBtnRetest.setVisibility(View.GONE);
			testOther();
		}
	}
	
	
	private void testOther(){
		//top 
		mTestItems.clear();
		for (int i = 0; i <= itemName.length-1; i++) {
			Utils.print(TAG, "name=="+itemName[i]);
			TestItem item = new TestItem();
			item.setItemname(itemName[i]);
			item.setResult("测试中......");
			mTestItems.add(item);
		}
		listView.setAdapter(resultAdapter);

		//buttom
		mGridTestItems.clear();
		for (int i = 0; i <= itemName2.length-1; i++) {
			Utils.print(TAG, "name=="+itemName2[i]);
			GridTestItem item = new GridTestItem();
			item.setItemname(itemName2[i]);
			String index;
			if(i==itemName2.length-1){
				index = "av";
			}else {
				index = "hdmi"+(i+1);
			}
			String result = preferences.getString(index, "");
//			String soundresult = preferences.getString(index+"sound", "");
			
			Utils.print(TAG, "aaaa==="+result);
			if(result.equals("通过")){
				item.setBitmap(BitmapFactory.decodeFile("/mnt/sdcard/"+i+".jpg"));
			}
			item.setResult(result);
//			item.setSoundResult(soundresult);
			mGridTestItems.add(item);
		}
		gridView.setAdapter(gridAdapter);
		
		
		
		
		isTested = false;
//		mTimes = 0l;
//		Message msg = mHandler.obtainMessage();
//		msg.what = MSG_UPDATE_TIME;
//		mHandler.sendMessage(msg);
 
		
		testUsb(USB1);
		testUsb(USB2);
		testUsb(USB4);
		
		testEthernet();
	}
  

   
	
	
	private void registerNetworkReceiver(){
		// network ConnectivityManager
		mNetworkReceiver = new BroadcastReceiver() {			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if(DEBUG) Log.d(TAG, "networkReceiver()");
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
				if(DEBUG) Log.d(TAG, "wifiStateReceiver() " + action);
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
	            } else if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){	            	
	            	Message msg = mHandler.obtainMessage();
	            	msg.what = MSG_WIFI_SCAN_RESULT;
	            	mHandler.removeMessages(MSG_WIFI_SCAN_RESULT);
	            	mHandler.sendMessage(msg);
	            } else if(WifiManager.RSSI_CHANGED_ACTION.equals(action)){
	            	Message msg = mHandler.obtainMessage();
	            	msg.what = MSG_UPDATE_WIFI_RSSI;
	            	mHandler.removeMessages(MSG_UPDATE_WIFI_RSSI);
	            	mHandler.sendMessage(msg);
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
		ConnectivityManager connec =  (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo info = connec.getActiveNetworkInfo();
		if(null != info){
			if(DEBUG) Log.d(TAG, "getActiveNetworkInfo() getTypeName: " + info.getTypeName() +
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
					if(!ethernet_do_work){
						ethernet_do_work = true;
						
						testEnableEthernet();
					}
				}
			}
		}else{
			if(DEBUG) Log.d(TAG, "getActiveNetworkInfo() info is null");
		}
 
	}    
    
    public static WifiConfiguration getConfig(String ssid, String password) {
        try {
            WifiConfiguration config = new WifiConfiguration();

            // config.SSID = AccessPoint.convertToQuotedString(
            // mAccessPoint.ssid);
            config.SSID = "\"" + ssid + "\"";
            if(DEBUG) Log.d(TAG,  "SSID." + config.SSID);
 
//            config.hiddenSSID = true;
            if(DEBUG) Log.d(TAG, "password." + password);
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
    	if(DEBUG) Log.d(TAG, "openEthernet()");
        mEthernetManager.setEthEnabled(true);
    }
    
    private void closeEthernet(){
    	if(DEBUG) Log.d(TAG, "closeEthernet()");
        mEthernetManager.setEthEnabled(false);
    }
    
    private void enableBtnRetest(){
    	if(DEBUG) Log.d(TAG, "enableBtnRetest()");
    	mBtnRetest.post(new Runnable() {
			
			@Override
			public void run() {
				mBtnRetest.setEnabled(true);				
			}
		});  
    }
    
    private void unableBtnRetest(){
    	if(DEBUG) Log.d(TAG, "unableBtnRetest()");
    	mBtnRetest.post(new Runnable() {
			
			@Override
			public void run() {
				mBtnRetest.setEnabled(false);
			}
		});
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
    
 
    
    class GridTestItem {
    	String itemname;
        Bitmap bitmap;
    	String result;
    	String soundResult;
		public String getItemname() {
			return itemname;
		}
		public void setItemname(String itemname) {
			this.itemname = itemname;
		}
		
		public Bitmap getBitmap() {
			return bitmap;
		}
		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
		public String getSoundResult() {
			return soundResult;
		}
		public void setSoundResult(String soundResult) {
			this.soundResult = soundResult;
		}
    }
 
 
	
	private void testHDMI(){
		preferences.edit().putInt("hdmiIndex", 0).commit();
		Intent intent = new Intent(this,TVplay.class);
		startActivity(intent);
		finish();
 
	}
	
	public void sendHDMIMessage(String port,Boolean status){
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("port", port);
		bundle.putBoolean("status", status);
		message.what = MSG_UPDATE_HDMI;
		message.obj = bundle;
		mHandler.sendMessage(message);
	}

     
//	public void testAPI(){
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				Tv tv = Tv.open();
//				int red = tv.FactoryGet_FBC_Gain_Red();
//				Log.v(TAG, "red==="+red);
//				
//				
//				tv.FactorySet_FBC_Gain_Red(10);
//				int red1 = tv.FactoryGet_FBC_Gain_Red();
//				Log.v(TAG, "red2==="+red1);
// 
//			}
//		}).start();
//		
// 
//	
//	}
}
