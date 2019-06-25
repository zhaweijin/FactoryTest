package com.hiveview.factorytest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
 




import android.amlogic.Tv;
import android.amlogic.Tv.EpgEvent;
import android.amlogic.Tv.EpgEventListener;
import android.amlogic.Tv.SIG_LINE_STATUS;
import android.amlogic.Tv.SigInfoChangeListener;
import android.amlogic.Tv.SigLineChangeListener;
import android.amlogic.Tv.SourceInput;
import android.amlogic.Tv.SourceSwitchListener;
import android.amlogic.Tv.StatusSourceConnectListener;
import android.amlogic.Tv.VFrameEvent;
import android.amlogic.Tv.VframBMPEventListener;
import android.amlogic.Tv.WhiteBalanceParams;
import android.amlogic.Tv.tv_source_connect_status_t;
import android.amlogic.Tv.tvin_info_t;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.util.Log;

public class TVplay extends Activity implements SourceSwitchListener,
		SigInfoChangeListener, SigLineChangeListener, StatusSourceConnectListener, VframBMPEventListener{

	private String TAG = "TVplay";
	public Tv tv = PublicUtil.getTvInstance();

    private SurfaceView v;
    private MediaPlayer mediaPlayer;
	
    private SharedPreferences preferences;
    
	SourceInput[] SourceList = { Tv.SourceInput.HDMI1, Tv.SourceInput.HDMI2,
			Tv.SourceInput.HDMI3, Tv.SourceInput.AV1 };
	int SourceIdx;
	
	private int screenWidth;
	private int screenHeight;
 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_main);
 
		tv.SetSourceSwitchListener(this);
		tv.SetSigInfoChangeListener(this);
		tv.SetSigLineChangeListener(this);
		tv.setGetVframBMPListener(this);
		
	
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SourceIdx = preferences.getInt("hdmiIndex", 0);
		Log.d(TAG, "onCreate--INDEX=="+SourceIdx);
		SourceIdx = 0;
         
		initInputSrouceView(); //必须放在onCreate里面，否则截屏是背景后面的，不是当前的
		//如果只要显示，而不截图，那么可以随意初始化

	}


	@Override
	public void onSigChange(tvin_info_t arg0) {
		// TODO Auto-generated method stub
	}
 

	public static final int VIDEO_HOLE_REAL  = 0x102;
	private void initInputSrouceView() {
		
//		int hdmi1 = tv.GetSourceConnectStatus(SourceInput.HDMI1);
//		int hdmi2 = tv.GetSourceConnectStatus(SourceInput.HDMI2);
//		int hdmi3 = tv.GetSourceConnectStatus(SourceInput.HDMI3);
//		
//		Log.v(TAG,"-------HDMI1 plugin = " + hdmi1 + " , HDMI2 plugin = " + hdmi2 + 
//				" , HDMI3 plugin = " + hdmi3);
		
		if(SourceIdx<3){
			Toast.makeText(this, "正在测试HDMI"+(SourceIdx+1), 3000).show();
		}else if (SourceIdx==3){
			Toast.makeText(this, "正在测试MINIAV", 3000).show();
		}else if(SourceIdx==4){
			Toast.makeText(this, "正在测试AUX", 3000).show();
		}
 
		
		if(SourceIdx==0){
			int hdmi1 = tv.GetSourceConnectStatus(SourceInput.HDMI1);
			if(hdmi1!=1){
				failedGoTVplay();
				return;
			}
		}else if(SourceIdx==1){
			int hdmi2 = tv.GetSourceConnectStatus(SourceInput.HDMI2);
			if(hdmi2!=1){
				failedGoTVplay();
				return;
			}
		}else if(SourceIdx==2){
			int hdmi3 = tv.GetSourceConnectStatus(SourceInput.HDMI3);
			if(hdmi3!=1){
				failedGoTVplay();
				return;
			}
		}else if(SourceIdx==3){
			int av = tv.GetSourceConnectStatus(SourceInput.AV1);
			Utils.print(TAG, "av interface status=="+av);
			if(av!=1){
				preferences.edit().putString("av", "未通过").commit();
//				preferences.edit().putString("avsound", "未通过").commit();
//				preferences.edit().putInt("hdmiIndex", (SourceIdx+1)).commit();
//				Intent intent = new Intent(this,TVplay.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(intent);
				Intent intent = new Intent(this,MainActivity.class);
				intent.putExtra("testhdmi", true);
				startActivity(intent);
				finish();
				return;
			}
		}
		
		
		Log.v(TAG, "Source index=="+SourceIdx);
		WindowManager wm = this.getWindowManager();
		 
	    screenWidth = wm.getDefaultDisplay().getWidth();
	    screenHeight = wm.getDefaultDisplay().getHeight();
		String outputmode = SystemProperties.get("ubootenv.var.outputmode");
	    if(outputmode.startsWith("1080")){
	    	screenWidth = 1920;
	    	screenHeight = 1080;
	    }else if(outputmode.startsWith("4k2k")){
	    	screenWidth = 1920*2;
	    	screenHeight = 1080*2;
	    }
		
		if(SourceIdx<4){
			tv.SetSourceInput(SourceList[SourceIdx]);
			tv.SetPreviewWindow(0, 0, screenWidth, screenHeight);
			ViewGroup root = (ViewGroup) getWindow().getDecorView().findViewById(
					android.R.id.content);
			root.setFocusable(true);
			v = new SurfaceView(this);
			 
			
			root.addView(v, 0);
			if (v != null) {
				v.getHolder().addCallback(mSHCallback);
				v.getHolder().setFormat(VIDEO_HOLE_REAL);
			}
			
			
			/*new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						Thread.sleep(5000);
						Log.v(TAG, "get bitmap");
						tv.CreateVideoFrameBitmap(SourceList[SourceIdx].toInt());
						
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}).start();*/
		}else if(SourceIdx==4){
			//confirme aux sound is ok
			playMP3();
			confirmHDMIINSound(getResources().getString(R.string.confirm_title)+"AUX");
		}
		
 
 
	}

	public void failedGoTVplay(){
		/*new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(2000);
					preferences.edit().putString("hdmi"+(SourceIdx+1), "未通过").commit();
//					preferences.edit().putString("hdmi"+(SourceIdx+1)+"sound", "未通过").commit();
					preferences.edit().putInt("hdmiIndex", (SourceIdx+1)).commit();
					Intent intent = new Intent(TVplay.this,TVplay.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();*/

	}
	
	
	private SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			Log.d(TAG, "surfaceChanged");
			initSurface(holder);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "surfaceCreated");
			initSurface(holder);
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "surfaceDestroyed");
		}

		private void initSurface(SurfaceHolder h) {
			Canvas c = null;
			try {
				Log.d(TAG, "initSurface>>>>>");
				c = h.lockCanvas();
				
			} finally {
				if (c != null)
					h.unlockCanvasAndPost(c);
			}
		}
	};

	static int tmp = 0;


	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//tv.SetSourceInput(SourceInput.MPEG);
		try {
			if(SourceIdx==3){
				tv.StopTv();
			}
			if(mediaPlayer!=null && mediaPlayer.isPlaying()){
				mediaPlayer.stop();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	protected void onstop() {
		// TODO Auto-generated method stub
		try {
			if(SourceIdx<3){
				tv.release();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void onSourceSwitchStatusChange(SourceInput source, int state) {
		// TODO Auto-generated method stub
		/*if (state == 1) {
			Log.v(TAG, "------------ switching ------------");
		} else if (state == 0) {
			Log.v(TAG, "------------ switch OK ------------- " + source);
		}*/
	}

	@Override
	public void onSigLineStatusChange(SourceInput source, SIG_LINE_STATUS status) {
		// TODO Auto-generated method stub
        
	}

	@Override
	public void onSourceConnectChange(SourceInput arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(VFrameEvent arg0) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onEvent");
		 
		Bitmap showBitmap = tv.getVideoFrameBitmap();
		try {
			saveMyBitmap(showBitmap, "/mnt/sdcard/"+SourceIdx+".jpg");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		Log.v(TAG,"############getVideoFrameBitmap"+SourceIdx);
		String title="";
		String message="";
		if(SourceIdx<3){
			title = getResources().getString(R.string.confirm_title) + "HDMI"+(SourceIdx+1);
		}else if(SourceIdx==3){
			title = getResources().getString(R.string.confirm_title) + "MINIAV";
		}
//		confirmHDMIINSound(title);
//		next(true);
	}
 

	public void confirmHDMIINSound(String title){
		new AlertDialog.Builder(this)
		.setTitle(title)
		.setCancelable(false)
		.setMessage(getResources().getString(R.string.sound_confirm_message))
		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				next(true);
			}
		})
		.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				next(false);
			}
		}).show();
	}
 
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		if(keyCode == KeyEvent.KEYCODE_BACK)
//			return false;
//		return super.onKeyDown(keyCode, event);
//		
//	}
	
	private void next(boolean pass){
		if(SourceIdx < 3){  //HDMI1,HDMI2,HDMI3
			preferences.edit().putString("hdmi"+(SourceIdx+1), "通过").commit();
//			preferences.edit().putString("hdmi"+(SourceIdx+1)+"sound", pass?"通过":"未通过").commit();
			preferences.edit().putInt("hdmiIndex", (SourceIdx+1)).commit();
			Intent intent = new Intent(this,TVplay.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}else if(SourceIdx==3){//AV
			preferences.edit().putString("av", "通过").commit();
//			preferences.edit().putString("avsound", pass?"通过":"未通过").commit();
//			preferences.edit().putInt("hdmiIndex", (SourceIdx+1)).commit();
//			Intent intent = new Intent(this,TVplay.class);
//			startActivity(intent);
			Intent intent = new Intent(this,MainActivity.class);
			intent.putExtra("testhdmi", true);
			startActivity(intent);
			finish();
		}else if(SourceIdx==4){//AUX
//			preferences.edit().putString("auxsound", pass?"通过":"未通过").commit();
			Intent intent = new Intent(this,MainActivity.class);
			intent.putExtra("testhdmi", true);
			startActivity(intent);
			finish();
		}
	}
	
 
	public void saveMyBitmap(Bitmap bitmap, String bitName) {
		try {
			if(bitmap==null)
				return;
			File f = new File(bitName);
			Log.v(TAG, f.getAbsolutePath());
			if(!f.exists())
			   f.createNewFile();
			FileOutputStream fOut = new FileOutputStream(f);

			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	
	
 
	public void playMP3(){
 
        AssetManager am = getAssets();//获得该应用的AssetManager  
        try{  
            AssetFileDescriptor afd = am.openFd("testaux.mp3");  
            mediaPlayer = new MediaPlayer();  
            mediaPlayer.setDataSource(afd.getFileDescriptor());  
            mediaPlayer.prepare(); //准备  
            mediaPlayer.start();
            
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer arg0) {
					// TODO Auto-generated method stub
					Utils.print(TAG, "reset play mp3");
					mediaPlayer.start();
				}
			});
        }  
        catch(IOException e){  
            e.printStackTrace();  
        }  
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
            finish();
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

        Log.v(TAG, "use>>FactoryWhiteBalanceGetAllParams COLOR_TEMP_COLD");
        WhiteBalanceParams whiteBalanceParams = tv
                .FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_COLD.toInt());
        Log.v(TAG, "r=" + whiteBalanceParams.r_gain + "   ,r_offset="
                + whiteBalanceParams.r_offset + "  g="
                + whiteBalanceParams.g_gain + "   ,g_offset="
                + whiteBalanceParams.g_offset + "  b="
                + whiteBalanceParams.b_gain + ",   b_offset="
                + whiteBalanceParams.b_offset);
        
        
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 1);
        // 保存参数
        int savereturn = tv.FactoryWhiteBalanceSaveParameters(
                Tv.SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_COLD.toInt(), 1024, 1024, 1024, 0, 0,
                0);
        Log.v(TAG, "save>>" + savereturn);
        
        
        Log.v(TAG, "use>>FactoryWhiteBalanceGetAllParams COLOR_TEMP_STANDARD");
        whiteBalanceParams = tv
                .FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_STANDARD.toInt());
        Log.v(TAG, "r=" + whiteBalanceParams.r_gain + "   ,r_offset="
                + whiteBalanceParams.r_offset + "  g="
                + whiteBalanceParams.g_gain + "   ,g_offset="
                + whiteBalanceParams.g_offset + "  b="
                + whiteBalanceParams.b_gain + ",   b_offset="
                + whiteBalanceParams.b_offset);
        
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1);
        // 保存参数
        savereturn = tv.FactoryWhiteBalanceSaveParameters(
                Tv.SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1024, 1024, 1024, 0, 0,
                0);
        Log.v(TAG, "save>>" + savereturn);
        
        Log.v(TAG, "use>>FactoryWhiteBalanceGetAllParams COLOR_TEMP_WARM");
        whiteBalanceParams = tv
                .FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_WARM.toInt());
        Log.v(TAG, "r=" + whiteBalanceParams.r_gain + "   ,r_offset="
                + whiteBalanceParams.r_offset + "  g="
                + whiteBalanceParams.g_gain + "   ,g_offset="
                + whiteBalanceParams.g_offset + "  b="
                + whiteBalanceParams.b_gain + ",   b_offset="
                + whiteBalanceParams.b_offset);
        
        tv.FactoryWhiteBalanceSetColorTemperature(Tv.SourceInput.HDMI1.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1);
       // 保存参数
       savereturn = tv.FactoryWhiteBalanceSaveParameters(
                 Tv.SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1024, 1024, 1024, 0, 0,
                 0);
       Log.v(TAG, "save>>" + savereturn);
    }
}
