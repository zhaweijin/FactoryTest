package com.hiveview.factorytest;

 

import android.amlogic.Tv;
import android.amlogic.Tv.SourceInput;
import android.amlogic.Tv.SourceInput_Type;
import android.amlogic.Tv.WhiteBalanceParams;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	private Tv tv = PublicUtil.getTvInstance();
	private int faildCount=0;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
 
			switch (msg.what) {
			case 0:
//				textview.setText(textview.getText()+"NG\r");
				WhiteBalanceParams params = tv.FactoryWhiteBalanceGetAllParams(1);
				Log.v("test", "NG\r"+"WhiteBalanceParams rgain="+params.r_gain+",bgain="+params.b_gain+",ggain="+params.g_gain
									+",roffset="+params.r_offset+",goffset="+params.g_offset+",boffset="+params.b_offset+"\r");
				break;
			case 1:
				Log.v("test", "pass\t");
				break;
			case 2:
				WhiteBalanceParams params2 = tv.FactoryWhiteBalanceGetAllParams(1);
				Log.v("test", "WhiteBalanceParams rgain="+params2.r_gain+",bgain="+params2.b_gain+",ggain="+params2.g_gain
									+",roffset="+params2.r_offset+",goffset="+params2.g_offset+",boffset="+params2.b_offset+"\r");
			}
			super.handleMessage(msg);
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		tv.StartTv();
		tv.SetSourceInput(SourceInput.HDMI1);
		
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub	
				mHandler.sendEmptyMessage(2);
				int preRGain = 1024;
				int preGGain = 1024;
				int preBGain = 1024;
				int preROffset = 0;
				int preGOffset = 0;
				int preBOffset = 0;
				
				for(int i = 1; i < 50; i++)
				{
					Log.d("test","FactoryWhiteBalanceSetRedGain="+tv.FactoryWhiteBalanceSetRedGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 1, 1024+i*8));
					Log.d("test","FactoryWhiteBalanceSetGreenGain="+tv.FactoryWhiteBalanceSetGreenGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 1, 1024));
					Log.d("test","FactoryWhiteBalanceSetBlueGain="+tv.FactoryWhiteBalanceSetBlueGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 1, 1024-i*8));
					Log.d("test","FactoryWhiteBalanceSaveParameters="+tv.FactoryWhiteBalanceSaveParameters(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 1, 1024+i*8, 1024, 1024-i*8, 0-i*8, 0, 0+i*8));
					Log.d("test","FactoryWhiteBalanceSetColorTemperature index0="+tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 0, 1));
					Log.d("test","FactoryWhiteBalanceSetColorTemperature index1="+tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 1, 1));
					WhiteBalanceParams params = tv.FactoryWhiteBalanceGetAllParams(1);
					Log.d("test","WhiteBalanceParams rgain="+params.r_gain+",bgain="+params.b_gain+",ggain="+params.g_gain
							+",roffset="+params.r_offset+",goffset="+params.g_offset+",boffset="+params.b_offset);
					
					if (params.r_gain - preRGain == 8 && params.g_gain == preGGain &&  params.b_gain - preBGain == -8
							&& params.r_offset - preROffset == -8 && params.g_offset == preGOffset &&  params.b_offset - preBOffset == 8)
					{
						mHandler.sendEmptyMessage(1);
					}
					else
					{
						mHandler.sendEmptyMessage(0);
						break;
					}
					preRGain = params.r_gain;
					preGGain = params.g_gain;
					preBGain = params.b_gain;
					preROffset = params.r_offset;
					preGOffset = params.g_offset;
					preBOffset = params.b_offset;
				}
			}
		}).start();*/
	}

 
 
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		faildCount=0;
		if(keyCode == KeyEvent.KEYCODE_0){//no delay and add switch other color mode
			new Thread(new Runnable() {
				@Override
				public void run() {
					int preRGain = 1024;
					int preGGain = 1024;
					int preBGain = 1024;
					int preROffset = 0;
					int preGOffset = 0;
					int preBOffset = 0;
					
					for(int i = 1; i < 50; i++)
					{
						Log.v("test", "i====="+i);
						//set rgb
						int r = 1024+i*8;
						int g = 1024;
						int b = 1024-i*8;
						int roffset = 0-i*8;
						int goffset = 0;
						int boffset = 0+i*8;
						Log.v("test", "r=="+r+",g=="+g+",b==="+b+",roffset="+roffset+",goffset=="+goffset+",boffset=="+boffset);
						
						tv.FactoryWhiteBalanceSetRedGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), r);
						tv.FactoryWhiteBalanceSetGreenGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), g);
						tv.FactoryWhiteBalanceSetBlueGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), b);
						
						
//						Log.d("test","FactoryWhiteBalanceSetColorTemperature index0="+tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 0, 1));//stand
//						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1); //warm
						//save color
						tv.FactoryWhiteBalanceSaveParameters(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), r, g, b, roffset, goffset, boffset);
						//set color display
						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1);
						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1);
						
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							// TODO: handle exception
						}
						//get color
						WhiteBalanceParams params = tv.FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_WARM.toInt());
						Log.v("test","WhiteBalanceParams rgain="+params.r_gain+
								",ggain="+params.g_gain+
								",bgain="+params.b_gain+
								",roffset="+params.r_offset+
								",goffset="+params.g_offset+
								",boffset="+params.b_offset);
						
						if (params.r_gain - preRGain == 8 
							&& params.g_gain == preGGain 
							&& params.b_gain - preBGain == -8
							&& params.r_offset - preROffset == -8 
							&& params.g_offset == preGOffset 
							&& params.b_offset - preBOffset == 8)
						{
							Log.v("test", "ok");
						}
						else
						{
							Log.v("test", "failed");
							faildCount++;
						}
						preRGain = params.r_gain;
						preGGain = params.g_gain;
						preBGain = params.b_gain;
						preROffset = params.r_offset;
						preGOffset = params.g_offset;
						preBOffset = params.b_offset;
					}
					Log.v("test", "failcount>>>>>>>>>>>>>>>>>>>>>>"+faildCount);
					
				}
			}).start();
		}else if(keyCode == KeyEvent.KEYCODE_1){//no delay and no switch other color mode
			new Thread(new Runnable() {
				@Override
				public void run() {
					int preRGain = 1024;
					int preGGain = 1024;
					int preBGain = 1024;
					int preROffset = 0;
					int preGOffset = 0;
					int preBOffset = 0;
					
					for(int i = 1; i < 50; i++)
					{
						Log.v("test", "i====="+i);
						//set rgb
						int r = 1024+i*8;
						int g = 1024;
						int b = 1024-i*8;
						int roffset = 0-i*8;
						int goffset = 0;
						int boffset = 0+i*8;
						Log.v("test", "r=="+r+",g=="+g+",b==="+b+",roffset="+roffset+",goffset=="+goffset+",boffset=="+boffset);
						
						tv.FactoryWhiteBalanceSetRedGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), r);
						tv.FactoryWhiteBalanceSetGreenGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), g);
						tv.FactoryWhiteBalanceSetBlueGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), b);
						
						
//						Log.d("test","FactoryWhiteBalanceSetColorTemperature index0="+tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 0, 1));//stand
//						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1); //warm
						//save color
						tv.FactoryWhiteBalanceSaveParameters(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), r, g, b, roffset, goffset, boffset);
						//set color display
//						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1);
						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1);
						
						
						//get color
						WhiteBalanceParams params = tv.FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_WARM.toInt());
						Log.v("test","WhiteBalanceParams rgain="+params.r_gain+
								",ggain="+params.g_gain+
								",bgain="+params.b_gain+
								",roffset="+params.r_offset+
								",goffset="+params.g_offset+
								",boffset="+params.b_offset);
						
						if (params.r_gain - preRGain == 8 
							&& params.g_gain == preGGain 
							&& params.b_gain - preBGain == -8
							&& params.r_offset - preROffset == -8 
							&& params.g_offset == preGOffset 
							&& params.b_offset - preBOffset == 8)
						{
							Log.v("test", "ok");
						}
						else
						{
							Log.v("test", "failed");
							faildCount++;
						}
						preRGain = params.r_gain;
						preGGain = params.g_gain;
						preBGain = params.b_gain;
						preROffset = params.r_offset;
						preGOffset = params.g_offset;
						preBOffset = params.b_offset;
					}
					Log.v("test", "failcount>>>>>>>>>>>>>>>>>>>>>>"+faildCount);
					
				}
			}).start();
		}else if(keyCode == KeyEvent.KEYCODE_2){//add delay no switch color mode
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					int preRGain = 1024;
					int preGGain = 1024;
					int preBGain = 1024;
					int preROffset = 0;
					int preGOffset = 0;
					int preBOffset = 0;
					
					for(int i = 1; i < 50; i++)
					{
						Log.v("test", "i====="+i);
						//set rgb
						int r = 1024+i*8;
						int g = 1024;
						int b = 1024-i*8;
						int roffset = 0-i*8;
						int goffset = 0;
						int boffset = 0+i*8;
						Log.v("test", "r=="+r+",g=="+g+",b==="+b+",roffset="+roffset+",goffset=="+goffset+",boffset=="+boffset);
						
						tv.FactoryWhiteBalanceSetRedGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), r);
						tv.FactoryWhiteBalanceSetGreenGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), g);
						tv.FactoryWhiteBalanceSetBlueGain(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), b);
						
						
//						Log.d("test","FactoryWhiteBalanceSetColorTemperature index0="+tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), 0, 1));//stand
//						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1); //warm
						//save color
						tv.FactoryWhiteBalanceSaveParameters(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), r, g, b, roffset, goffset, boffset);
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							// TODO: handle exception
						}
						
						//set color display
						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_STANDARD.toInt(), 1);
						tv.FactoryWhiteBalanceSetColorTemperature(SourceInput_Type.SOURCE_TYPE_HDMI.toInt(), Tv.color_temperature.COLOR_TEMP_WARM.toInt(), 1);
						
						
						//get color
						WhiteBalanceParams params = tv.FactoryWhiteBalanceGetAllParams(Tv.color_temperature.COLOR_TEMP_WARM.toInt());
						Log.v("test","WhiteBalanceParams rgain="+params.r_gain+
								",ggain="+params.g_gain+
								",bgain="+params.b_gain+
								",roffset="+params.r_offset+
								",goffset="+params.g_offset+
								",boffset="+params.b_offset);
						
						if (params.r_gain - preRGain == 8 
							&& params.g_gain == preGGain 
							&& params.b_gain - preBGain == -8
							&& params.r_offset - preROffset == -8 
							&& params.g_offset == preGOffset 
							&& params.b_offset - preBOffset == 8)
						{
							Log.v("test", "ok");
						}
						else
						{
							Log.v("test", "failed");
							faildCount++;
						}
						preRGain = params.r_gain;
						preGGain = params.g_gain;
						preBGain = params.b_gain;
						preROffset = params.r_offset;
						preGOffset = params.g_offset;
						preBOffset = params.b_offset;
					}
					Log.v("test", "failcount>>>>>>>>>>>>>>>>>>>>>>"+faildCount);
					
				}
			}).start();
		}
		return super.onKeyDown(keyCode, event);
	}
 

}
