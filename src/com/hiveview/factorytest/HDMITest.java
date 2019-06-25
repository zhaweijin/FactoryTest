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
import android.amlogic.Tv.SourceInput_Type;
import android.amlogic.Tv.SourceSwitchListener;
import android.amlogic.Tv.StatusSourceConnectListener;
import android.amlogic.Tv.VFrameEvent;
import android.amlogic.Tv.VframBMPEventListener;
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
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.util.Log;

public class HDMITest extends Activity implements SourceSwitchListener,
		SigInfoChangeListener, SigLineChangeListener, StatusSourceConnectListener, VframBMPEventListener{

	private String TAG = "HDMITest";
	public Tv tv = PublicUtil.getTvInstance();

    private SurfaceView v;
    private MediaPlayer mediaPlayer;
	
    private SharedPreferences preferences;
    
    private ViewGroup root;
    
	SourceInput[] SourceList = { Tv.SourceInput.HDMI1, Tv.SourceInput.HDMI2,
			Tv.SourceInput.HDMI3, Tv.SourceInput.AV1 };
	int index;
 
	public static final int HDMI1 = 1;
	public static final int HDMI2 = 2;
	public static final int HDMI3 = 3;
	public static final int AV = 4;
	public static final int AUX = 5;

	private int screenWidth;
	private int screenHeight;
 
	private TextView textView;
	private LinearLayout layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hdmitest_main);
		
		tv.SetSourceSwitchListener(this);
		tv.SetSigInfoChangeListener(this);
		tv.SetSigLineChangeListener(this);
		tv.setGetVframBMPListener(this);
		
		
		textView = (TextView)findViewById(R.id.text);
		layout = (LinearLayout)findViewById(R.id.layout);
		
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
		 
		index = getIntent().getIntExtra("index", 0);
		Log.d(TAG, "onCreate--INDEX=="+index);
         
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
//		
//		if(index<HDMI1){
//			Toast.makeText(this, "正在测试HDMI"+(SourceIdx+1), 3000).show();
//		}else if (index==3){
//			Toast.makeText(this, "正在测试MINIAV", 3000).show();
//		}else if(SourceIdx==4){
//			Toast.makeText(this, "正在测试AUX", 3000).show();
//		}
// 
		
		if(index==HDMI1){
			int hdmi1 = tv.GetSourceConnectStatus(SourceInput.HDMI1);
			if(hdmi1!=1){
				Toast.makeText(this, "测试HDMI1显示不通过", 3000).show();
				finish();
				return;
			}
		}else if(index==HDMI2){
			int hdmi2 = tv.GetSourceConnectStatus(SourceInput.HDMI2);
			if(hdmi2!=1){
				Toast.makeText(this, "测试HDMI2显示不通过", 3000).show();
				finish();
				return;
			}
		}else if(index==HDMI3){
			int hdmi3 = tv.GetSourceConnectStatus(SourceInput.HDMI3);
			if(hdmi3!=1){
				Toast.makeText(this, "测试HDMI3显示不通过", 3000).show();
				finish();
				return;
			}
		}else if(index==AV){
			int av = tv.GetSourceConnectStatus(SourceInput.AV1);
			Utils.print(TAG, "av interface status=="+av);
			if(av!=1){
				Toast.makeText(this, "测试AV显示不通过", 3000).show();
				finish();
				return;
			}
		}
		
		
		if(index!=AUX){
			initDisplay();
		}else {
			playMP3();
		}
		Log.v(TAG, "Source index=="+index);
	}

	
	private void initDisplay(){
		tv.SetSourceInput(SourceList[index-1]);
		tv.SetPreviewWindow(0, 0, screenWidth, screenHeight);
		root = (ViewGroup) getWindow().getDecorView().findViewById(
				android.R.id.content);
		root.setFocusable(true);
		v = new SurfaceView(this);
		 
		testAPI();
		/*root.addView(v, 0);*/
		layout.addView(v,0);
		if (v != null) {
			v.getHolder().addCallback(mSHCallback);
			v.getHolder().setFormat(VIDEO_HOLE_REAL);
		}
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
		tv.StopTv();
		
		try {
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
			tv.release();
//			if(SourceIdx<4){
//				tv.release();
//			}
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
 
	private void testAPI() {
		try {
			Log.v(TAG,"testAPI");
			Tv tv = Tv.open();
			int[] result = new int[2];
			int a = tv.GetHdmiHdcpKeyKsvInfo(result);
			
			 
			textView.setText("HDCPKsv>>>>"+Integer.toHexString(result[0]) + Integer.toHexString(result[1]));
			 
			Log.v(TAG,
					">>>>" + result.length + ",1="
							+ Integer.toHexString(result[0]) + ",2="
							+ Integer.toHexString(result[1]) + "-------a=" + a);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
/*	private SourceInput_Type souceValue[]={SourceInput_Type.SOURCE_TYPE_AV,SourceInput_Type.SOURCE_TYPE_TV,SourceInput_Type.SOURCE_TYPE_DTV,SourceInput_Type.SOURCE_TYPE_HDMI,SourceInput_Type.SOURCE_TYPE_MPEG};
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_0){
			for(SourceInput_Type source :souceValue){
				setUserMode(source);
				tv.SetColorTemperature(Tv.color_temperature.COLOR_TEMP_STANDARD, source, 1);
				tv.SetBrightness(100, source, 1);
				tv.SetContrast(100, source, 1);
			}
		}else if(keyCode == KeyEvent.KEYCODE_1){
			Log.v(TAG, "getBrightness =="+tv.GetBrightness(SourceInput_Type.SOURCE_TYPE_HDMI)+
					" GetContrast== "+tv.GetContrast(SourceInput_Type.SOURCE_TYPE_HDMI));
		}
		return super.onKeyDown(keyCode, event);
	}*/
}
