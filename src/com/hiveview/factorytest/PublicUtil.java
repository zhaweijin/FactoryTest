package com.hiveview.factorytest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.hiveview.factorytest.TvService;

import android.amlogic.Tv;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;

public class PublicUtil {
	private static final String TAG = "PublicUtil";
	public TvService mService;
	static Runtime run = Runtime.getRuntime();
	static String cmd = "sync";
	private static volatile Tv instance = null;
	
    public static Tv getTvInstance(){
    	synchronized (PublicUtil.class) {
    		if (instance == null){
    			instance = Tv.open();
    		}
    	}    	
    	return instance;    	
    }
    
    public static void release(){
    	synchronized (PublicUtil.class) {
    		if (instance != null){
    			instance = null;
    		}
    	}
    }
	
	
    public  void setSleepTimer(int level){
   	 try {
          mService.tvapi_set_sleep_timer(level);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   }
    
	public int getSleepTimePos() {
		int sleep_time_index = -1;
		int time = SystemProperties.getInt("tv.sleep_timer", 0);
		switch (time) {
		case 0:
			sleep_time_index = 0;
			break;
		case 900000:
			sleep_time_index = 1;
			break;
		case 1800000:
			sleep_time_index = 2;
			break;
		case 2700000:
			sleep_time_index = 3;
			break;
		case 3600000:
			sleep_time_index = 4;
			break;
		case 5400000:
			sleep_time_index = 5;
			break;
		case 7200000:
			sleep_time_index = 6;
			break;
		default:
			sleep_time_index = 0;
			break;
		}
		if (sleep_time_index != -1)
			return sleep_time_index;
		return 0;
	}

	public void SetTimeout(int position) {
		switch (position) {
		case 0:
			SystemProperties.set("tv.sleep_timer", "0");
			Log.d(TAG, "tv.sleep_timer 0");
				break;
		case 1:
			SystemProperties.set("tv.sleep_timer", "900000");
			Log.d(TAG, "tv.sleep_timer 900000");
			break;
		case 2:
			SystemProperties.set("tv.sleep_timer", "1800000");
			break;
		case 3:
			SystemProperties.set("tv.sleep_timer", "2700000");
			break;
		case 4:
			SystemProperties.set("tv.sleep_timer", "3600000");
			break;
		case 5:
			SystemProperties.set("tv.sleep_timer", "5400000");
			break;
		case 6:
			SystemProperties.set("tv.sleep_timer", "7200000");
			break;
		default:
			SystemProperties.set("tv.sleep_timer", "0");
			break;
		}
		runSystemCmd();
		if (position >= 0 && position <= 6) {
			this.setSleepTimer(position);
		}

	}
	
	
    public void setAutoBackLight(int pos) 
    {
        switch (pos)
        {
        case 0: 
        	if(SystemProperties.getInt("persist.tv.auto_backlight", -1) == 1){
        		SystemProperties.set("persist.tv.auto_backlight", "0");
        	}
            break;
        case 1:
        	if(SystemProperties.getInt("persist.tv.auto_backlight", -1) == 0){
        		SystemProperties.set("persist.tv.auto_backlight", "1");

        	}
            break;
        }
        runSystemCmd();
    }
    
    public int getAutoBackLight()
    {
    	Log.d(TAG,"enter getAutoBackLight");
        int m = SystemProperties.getInt("persist.tv.auto_backlight", 1);
        return m;
    }
	public static void runSystemCmd() {
		try {
			Process p = run.exec(cmd);
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			while ((lineStr = inBr.readLine()) != null)
				System.out.println(lineStr);// ��ӡ�����Ϣ
			if (p.waitFor() != 0) {
				if (p.exitValue() == 1)// p.exitValue()==0��ʾ�����ߣ��������
					System.err.println("runSystemCmd error!");
			}
			inBr.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
