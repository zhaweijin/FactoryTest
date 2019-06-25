package com.hiveview.factorytest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.hiveview.manager.SystemInfoManager;
import com.hiveview.manager.UsbDeviceManager;
import com.hiveview.manager.Usbdevice;
import android.amlogic.Tv;
import android.amlogic.Tv.FBC_MAINCODE_INFO;
import android.R.bool;
import android.R.integer;
import android.amlogic.Tv.WhiteBalanceParams;
import android.amlogic.Tv.tv_source_connect_status_t;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OtherTest extends Activity{

	//key test
	private TextView test_result;
	private String result="";
	//usb test
	private TextView usb1;
	private TextView usb2;
	private TextView usb4;
	public static final int FILE_CONTENT_LEN = 256;
	
	private String TAG = "OtherTest";
	
	private LinearLayout contain;
	
	private String USB1 = "1";
	private String USB2 = "2";
	private String USB4 = "4";
	
	private int index;
	private int displayKeyCount = 0;
	public Tv tv = PublicUtil.getTvInstance();

	//tv info
	private String mYunSN;
	private TextView yunping_sn;
	
	private String ddr;
	
	public final static int TEST_KEY = 1;
	public final static int TEST_USB = 2;
	public final static int TEST_TVINFO = 3;
	public final static int TEST_MANU_MODE = 4;
	
	private final static int MSG_UPDATE_YPING_SN = 0x111;
    Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_UPDATE_YPING_SN:
			    if(index == TEST_TVINFO){
			    	yunping_sn.setText(mYunSN);
				}
				break;
			}
		}
    	
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.other_main);
		contain = (LinearLayout)findViewById(R.id.contain);
		index = getIntent().getIntExtra("index", 1);
		if(index==TEST_KEY){
			testKey();
		}else if(index == TEST_USB){
			testUSB();
		}else if(index == TEST_TVINFO){
			displayTVInfo();
		}else if(index == TEST_MANU_MODE){
			initManuMode();
		}
		
		
	}
	
	
	private void testKey(){
		View view = LayoutInflater.from(this).inflate(R.layout.key_test, null);
		test_result = (TextView)view.findViewById(R.id.test_result);
		contain.removeAllViews();
		contain.addView(view);
	}
	
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == 2073){
			result = result + "已经按下RESET键.........\n";
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
			result = result + "已经按下OK键.........\n";
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			result = result + "已经按下方向左键.........\n";
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			result = result + "已经按下方向右键.........\n";
		}else {
			result = result + "已经按下键值"+keyCode+".........\n";
		}
		if(test_result!=null){
			displayKeyCount++;
			test_result.setText(result);
			if(displayKeyCount>25){
				result="";
				displayKeyCount=0;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	*/
	private void testUSB(){
		View view = LayoutInflater.from(this).inflate(R.layout.usb_test, null);
		usb1 = (TextView)view.findViewById(R.id.usb1);
		usb2 = (TextView)view.findViewById(R.id.usb2);
		usb4 = (TextView)view.findViewById(R.id.usb4);
		contain.removeAllViews();
		contain.addView(view);
		
		testUsb(USB1);
		testUsb(USB2);
		testUsb(USB4);
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
						usb1.setText("USB1 接口读写不正确");
						usb1.setTextColor(getResources().getColor(R.color.red));
					}else if(port.equals(USB2)){
						usb2.setText("USB2 接口读写不正确");
						usb2.setTextColor(getResources().getColor(R.color.red));
					}else if(port.equals(USB4)){
						usb4.setText("USB4 接口读写不正确");
						usb4.setTextColor(getResources().getColor(R.color.red));
					}
					return;
				}
				
				if(writeData(testuUsbdevice.usburi, "writetest.txt", "writetest") 
							&& readData(testuUsbdevice.usburi, "writetest.txt", "writetest")){
					if(port.equals(USB1)){
						usb1.setText("USB1 接口读写正确");
					}else if(port.equals(USB2)){
						usb2.setText("USB2 接口读写正确");
					}else if(port.equals(USB4)){
						usb4.setText("USB4 接口读写正确");
					}
					Utils.print(TAG, port+"===read and write ok");
			    }else {
			    	if(port.equals(USB1)){
						usb1.setText("USB1 接口读写不正确");
						usb1.setTextColor(getResources().getColor(R.color.red));
					}else if(port.equals(USB2)){
						usb2.setText("USB2 接口读写不正确");
						usb2.setTextColor(getResources().getColor(R.color.red));
					}else if(port.equals(USB4)){
						usb4.setText("USB4 接口读写不正确");
						usb4.setTextColor(getResources().getColor(R.color.red));
					}
			    	Utils.print(TAG, port+"===read and write failed");
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
 
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
		    		Log.d(TAG, "write buffer.len." + len);
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
			        		Log.d(TAG, "read len." + len + " totalLen." + totalLen);
			        	}		    		
			    		inStream.close();
			    		inStream = null;
			    		if(totalLen <= FILE_CONTENT_LEN){
							String readContent = new String(buffer, 0, totalLen);
							Log.d(TAG, "content:" + content + " readContent:" + readContent);					
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
		
	 
		 /*
	     * 显示烧录MAC、云盒SN、云TV SN、等信息
	     */
		private void displayTVInfo(){
			View view = LayoutInflater.from(this).inflate(R.layout.tv_info, null);
			TextView version = (TextView)view.findViewById(R.id.version);
			TextView mac = (TextView)view.findViewById(R.id.mac);
			TextView yun_boxsn = (TextView)view.findViewById(R.id.yun_boxsn);
			yunping_sn = (TextView)view.findViewById(R.id.yun_tvsn);
			TextView yun_ethernet_type = (TextView)view.findViewById(R.id.yun_ethernet_type);
            TextView yun_rtc_status = (TextView)view.findViewById(R.id.yun_rtc_status);
			RelativeLayout layout_tv_sn = (RelativeLayout)view.findViewById(R.id.layout_tv_sn);
			
			  
			 
			//ddr
			Button ddrSet = (Button)view.findViewById(R.id.ddr_set);
			final TextView ddrValue = (TextView)view.findViewById(R.id.ddr_value);
			String model="";
			
			try {
				Tv tv = Tv.open();
				tv.SetSourceInput(Tv.SourceInput.HDMI2);
				SystemInfoManager manager = SystemInfoManager.getSystemInfoManager();
//				Log.v(TAG, "version=="+manager.getFirmwareVersion());
				version.setText(manager.getFirmwareVersion());
				mac.setText(manager.getMacInfo());
				model = manager.getProductModel();
				if(model.startsWith("D32")){
					yun_boxsn.setText("");
					layout_tv_sn.setVisibility(View.INVISIBLE);
					yun_ethernet_type.setText(getResources().getString(R.string.ethernet_100));
				}else { //d55s、d43
					yun_boxsn.setText(manager.getSnInfo());
					String ethernetType = readFile("/sys/devices/platform/c9410000.ethernet/net/eth0/speed");
					if(ethernetType.equals("100")){
						yun_ethernet_type.setText(getResources().getString(R.string.ethernet_100));
					}else if(ethernetType.equals("1000")){
						yun_ethernet_type.setText(getResources().getString(R.string.ethernet_1000));
					}
				}
				
				if(rtcIsWork()){
					yun_rtc_status.setText(getResources().getString(R.string.rtc_ok));
				}else {
					yun_rtc_status.setText(getResources().getString(R.string.rtc_failed));
				}
				
				ddr = SystemProperties.get("ubootenv.var.ddr_spread");
				Log.v(TAG, "ddr==="+ddr);
				ddrValue.setText(ddr);
				
				ddrSet.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						new AlertDialog.Builder(OtherTest.this)
						.setTitle(getResources().getString(R.string.ddr_dialog_title))
						.setItems(getResources().getStringArray(R.array.ddr_item_value), new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								Log.v(TAG, "pos=="+arg1);
								String valueString  = getResources().getStringArray(R.array.ddr_item_value)[arg1];
								ddrValue.setText(valueString);
								SystemProperties.set("ubootenv.var.ddr_spread", valueString);
							}
						}).show();
						
					}
				});
				
				
				new Thread(new getVersion()).start();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			contain.removeAllViews();
			contain.addView(view);
			Utils.print(TAG, "displayTVinfo finished");
		}
		
		
		public  String readFile(String strFilePath) {
			String path = strFilePath;
			String content = "";
			File file = new File(path);
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					while ((line = buffreader.readLine()) != null) {
						content += line;
					}
					instream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return content;
		}
		
		
		public boolean rtcIsWork(){
			boolean result = false;
			if(!readFile("/sys/class/rtc/rtc0/time").endsWith("")){
				result = true;
			}
			return result;
		}
		
		
		private class getVersion implements Runnable {
			Tv tv = Tv.open();

			@Override
			public void run() {
				// TODO Auto-generated method stub
				FBC_MAINCODE_INFO a = tv.FactoryGet_FBC_Get_MainCode_Version();
	            String k = tv.FactoryGet_FBC_SN_Info().STR_SN_INFO;
	            if(null !=k&&!"".equals(k)){
	            	if(k.length()>=17){
	            		mYunSN=k.substring(0, 17);
	            	}else{
	            		mYunSN =k;
	            	}
	            }
	            mHandler.sendEmptyMessage(MSG_UPDATE_YPING_SN);
			}

		}
		
		
	private void initManuMode(){
		View view = LayoutInflater.from(this).inflate(R.layout.manu_mode, null);
		Button open_manumode = (Button)view.findViewById(R.id.open_manumode);
		open_manumode.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v(TAG, "open");
				SystemProperties.set("ubootenv.var.manumode", "1");
                                SystemProperties.set("ubootenv.var.powermode", "on");
			}
		});
		
		Button close_manumode = (Button)view.findViewById(R.id.close_manumode);
		close_manumode.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v(TAG, "close");
				SystemProperties.set("ubootenv.var.manumode", "0");
				SystemProperties.set("ubootenv.var.apkfile_exist", "0");
				SystemProperties.set("ubootenv.var.powermode", "standby");
			}
		});
		
		contain.removeAllViews();
		contain.addView(view);
		
	}
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

//            test1();  //旧接口测试
        	testNewInterfaceWrite();  //新接口测试

        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//             testGetColor();   //旧接口测试
        	testNewInterfaceRead();  //新接口测试

        } else if (keyCode == KeyEvent.KEYCODE_0) {
             //获取当前色温
        	int value = tv.FactoryWhiteBalanceGetColorTemperature(Tv.SourceInput.HDMI1.toInt());
            Log.v(TAG, "current color value>>" + value);
            if(value==Tv.color_temperature.COLOR_TEMP_COLD.toInt()){
            	Log.v(TAG, "current color mode>>");
            }else if(value==Tv.color_temperature.COLOR_TEMP_STANDARD.toInt()){
            	Log.v(TAG, "current stand mode>>");
            }else if(value==Tv.color_temperature.COLOR_TEMP_WARM.toInt()){
            	Log.v(TAG, "current warm mode>>");
            }
        }else if(keyCode == KeyEvent.KEYCODE_1){
        	Log.v(TAG, "switch cold mode");
        	tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 1);
        }else if(keyCode == KeyEvent.KEYCODE_2){
        	Log.v(TAG, "switch stand mode");
        	tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1);
        }else if(keyCode == KeyEvent.KEYCODE_3){
        	Log.v(TAG, "switch warm mode");
        	tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1);
        }
        return super.onKeyDown(keyCode, event);

    }

    private void test1(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                	testCOLO_TEMP_WARM();
                	testCOLOR_TEMP_STANDARD();
                    testCOLOR_TEMP_COLD();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }).start();
    } 
    
    
    public void testCOLO_TEMP_WARM(){
    	 //获取色温 COLOR_TEMP_WARM
        int value = tv.FactoryWhiteBalanceGetColorTemperature(Tv.SourceInput.HDMI1.toInt());
        Log.v(TAG, "current color value>>" + value);
        Log.v(TAG, "test COLOR_TEMP_WARM value>>");
        //get rgb
        int red = tv.FactoryWhiteBalanceGetRedGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        int blue = tv.FactoryWhiteBalanceGetBlueGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        int green = tv.FactoryWhiteBalanceGetGreenGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        int redoffset = tv.FactoryWhiteBalanceGetRedOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        int blueoffset = tv.FactoryWhiteBalanceGetBlueOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        int greenoffset = tv.FactoryWhiteBalanceGetGreenOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        Log.v(TAG, "red>>" + red+",redoffset>>"+redoffset+",blue>>"+blue+",blueoffset>>"+blueoffset+",green>>"+green+",greenoffset>>"+greenoffset);

        //设置色温
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1);
        //保存参数
   /*     int savereturn = tv.FactoryWhiteBalanceSaveParameters(Tv.SourceInput.HDMI1.toInt(),
                1, red, green, blue, redoffset, greenoffset, blueoffset);*/
        
        int savereturn = tv.FactoryWhiteBalanceSaveParameters(Tv.SourceInput.HDMI1.toInt(),
        		Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1024,1024,1024, 1, 1, 1);
        Log.v(TAG, "COLOR_TEMP_WARM save>>" + savereturn);
        //再次获取已经保存的值
//        value = tv.FactoryWhiteBalanceGetColorTemperature(Tv.SourceInput.HDMI1.toInt());
//        Log.v(TAG, "1value>>" + value);
    }
    
    public void testCOLOR_TEMP_STANDARD(){
   	   //获取色温 COLOR_TEMP_STANDARD
       Log.v(TAG, "test COLOR_TEMP_STANDARD value>>");

       //get rgb
       int red = tv.FactoryWhiteBalanceGetRedGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
       int blue = tv.FactoryWhiteBalanceGetBlueGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
       int green = tv.FactoryWhiteBalanceGetGreenGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
       int redoffset = tv.FactoryWhiteBalanceGetRedOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
       int blueoffset = tv.FactoryWhiteBalanceGetBlueOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
       int greenoffset = tv.FactoryWhiteBalanceGetGreenOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
       Log.v(TAG, "red>>" + red+",redoffset>>"+redoffset+",blue>>"+blue+",blueoffset>>"+blueoffset+",green>>"+green+",greenoffset>>"+greenoffset);

       //设置色温
       tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1);
       //保存参数
  /*     int savereturn = tv.FactoryWhiteBalanceSaveParameters(Tv.SourceInput.HDMI1.toInt(),
               1, red, green, blue, redoffset, greenoffset, blueoffset);*/
       
       int savereturn = tv.FactoryWhiteBalanceSaveParameters(Tv.SourceInput.HDMI1.toInt(),
       		Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1024,1024,1024, 1, 1, 1);
       Log.v(TAG, "COLOR_TEMP_STANDARD save>>" + savereturn);
       //再次获取已经保存的值
//       value = tv.FactoryWhiteBalanceGetColorTemperature(Tv.SourceInput.HDMI1.toInt());
//       Log.v(TAG, "1value>>" + value);
   }
    
    
    public void testCOLOR_TEMP_COLD(){
    	   //获取色温 COLOR_TEMP_STANDARD
        Log.v(TAG, "test COLOR_TEMP_COLD value>>");

        //get rgb
        int red = tv.FactoryWhiteBalanceGetRedGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int blue = tv.FactoryWhiteBalanceGetBlueGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int green = tv.FactoryWhiteBalanceGetGreenGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int redoffset = tv.FactoryWhiteBalanceGetRedOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int blueoffset = tv.FactoryWhiteBalanceGetBlueOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int greenoffset = tv.FactoryWhiteBalanceGetGreenOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        Log.v(TAG, "red>>" + red+",redoffset>>"+redoffset+",blue>>"+blue+",blueoffset>>"+blueoffset+",green>>"+green+",greenoffset>>"+greenoffset);

        //设置色温
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 1);
        //保存参数
   /*     int savereturn = tv.FactoryWhiteBalanceSaveParameters(Tv.SourceInput.HDMI1.toInt(),
                1, red, green, blue, redoffset, greenoffset, blueoffset);*/
        
        int savereturn = tv.FactoryWhiteBalanceSaveParameters(Tv.SourceInput.HDMI1.toInt(),
        		Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 1024,1024,1024, 1, 1, 1);
        Log.v(TAG, "COLOR_TEMP_COLD save>>" + savereturn);
        //再次获取已经保存的值
//        value = tv.FactoryWhiteBalanceGetColorTemperature(Tv.SourceInput.HDMI1.toInt());
//        Log.v(TAG, "1value>>" + value);
    }
    
    
    public void testGetColor(){
    	Log.v(TAG, "test get COLOR_TEMP_COLD value>>");

        //get rgb
        int red = tv.FactoryWhiteBalanceGetRedGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int blue = tv.FactoryWhiteBalanceGetBlueGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int green = tv.FactoryWhiteBalanceGetGreenGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int redoffset = tv.FactoryWhiteBalanceGetRedOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int blueoffset = tv.FactoryWhiteBalanceGetBlueOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        int greenoffset = tv.FactoryWhiteBalanceGetGreenOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        Log.v(TAG, "red>>" + red+",redoffset>>"+redoffset+",blue>>"+blue+",blueoffset>>"+blueoffset+",green>>"+green+",greenoffset>>"+greenoffset);
        
       

        //get rgb
        Log.v(TAG, "test get COLOR_TEMP_STANDARD value>>");
        red = tv.FactoryWhiteBalanceGetRedGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        blue = tv.FactoryWhiteBalanceGetBlueGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        green = tv.FactoryWhiteBalanceGetGreenGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        redoffset = tv.FactoryWhiteBalanceGetRedOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        blueoffset = tv.FactoryWhiteBalanceGetBlueOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        greenoffset = tv.FactoryWhiteBalanceGetGreenOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        Log.v(TAG, "red>>" + red+",redoffset>>"+redoffset+",blue>>"+blue+",blueoffset>>"+blueoffset+",green>>"+green+",greenoffset>>"+greenoffset);
        
        
      //get rgb
        Log.v(TAG, "test get COLOR_TEMP_WARM value>>");
        red = tv.FactoryWhiteBalanceGetRedGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        blue = tv.FactoryWhiteBalanceGetBlueGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        green = tv.FactoryWhiteBalanceGetGreenGain(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        redoffset = tv.FactoryWhiteBalanceGetRedOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        blueoffset = tv.FactoryWhiteBalanceGetBlueOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        greenoffset = tv.FactoryWhiteBalanceGetGreenOffset(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        Log.v(TAG, "red>>" + red+",redoffset>>"+redoffset+",blue>>"+blue+",blueoffset>>"+blueoffset+",green>>"+green+",greenoffset>>"+greenoffset);
    }
 
    
    
    
    private void testNewInterfaceRead() {

        Log.v(TAG, "use>>FactoryWhiteBalanceGetAllParams COLOR_TEMP_COLD");
        WhiteBalanceParams whiteBalanceParams = tv
                .FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        Log.v(TAG, "r=" + whiteBalanceParams.r_gain + "   ,r_offset="
                + whiteBalanceParams.r_offset + "  g="
                + whiteBalanceParams.g_gain + "   ,g_offset="
                + whiteBalanceParams.g_offset + "  b="
                + whiteBalanceParams.b_gain + ",   b_offset="
                + whiteBalanceParams.b_offset);

        // 保存参数
//        int savereturn = tv.FactoryWhiteBalanceSaveParameters(
//                Tv.SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 50, 50, 50, 0, 0,
//                0);
//        Log.v(TAG, "save>>" + savereturn);
        
        
        Log.v(TAG, "use>>FactoryWhiteBalanceGetAllParams COLOR_TEMP_STANDARD");
        whiteBalanceParams = tv
                .FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        Log.v(TAG, "r=" + whiteBalanceParams.r_gain + "   ,r_offset="
                + whiteBalanceParams.r_offset + "  g="
                + whiteBalanceParams.g_gain + "   ,g_offset="
                + whiteBalanceParams.g_offset + "  b="
                + whiteBalanceParams.b_gain + ",   b_offset="
                + whiteBalanceParams.b_offset);
        
        
        Log.v(TAG, "use>>FactoryWhiteBalanceGetAllParams COLOR_TEMP_WARM");
        whiteBalanceParams = tv
                .FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        Log.v(TAG, "r=" + whiteBalanceParams.r_gain + "   ,r_offset="
                + whiteBalanceParams.r_offset + "  g="
                + whiteBalanceParams.g_gain + "   ,g_offset="
                + whiteBalanceParams.g_offset + "  b="
                + whiteBalanceParams.b_gain + ",   b_offset="
                + whiteBalanceParams.b_offset);
        
    }
    
    
    private void testNewInterfaceWrite() {

        Log.v(TAG, "use>>FactoryWhiteBalanceSaveParameters COLOR_TEMP_COLD 1000 1000 1000");
 
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 1);
        // 保存参数
        int savereturn = tv.FactoryWhiteBalanceSaveParameters(
                Tv.SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 1000, 1000, 1000, 0, 0,
                0);
        Log.v(TAG, "save state>>" + savereturn);
        
        
        Log.v(TAG, "use>>FactoryWhiteBalanceSaveParameters COLOR_TEMP_STANDARD 900 900 900");
        
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1);
        // 保存参数
        savereturn = tv.FactoryWhiteBalanceSaveParameters(
                Tv.SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 900, 900, 900, 0, 0,
                0);
        Log.v(TAG, "save state>>" + savereturn);
        
        Log.v(TAG, "use>>FactoryWhiteBalanceSaveParameters COLOR_TEMP_WARM 600 600 600");
        
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1);
       // 保存参数
       savereturn = tv.FactoryWhiteBalanceSaveParameters(
                 Tv.SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 600, 600, 600, 0, 0,
                 0);
       Log.v(TAG, "save state>>" + savereturn);
    }
}
