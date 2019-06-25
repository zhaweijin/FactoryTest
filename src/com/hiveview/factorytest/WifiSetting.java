package com.hiveview.factorytest;

import java.net.PasswordAuthentication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WifiSetting extends Activity{

	public static final String WIFI_SSID = "test"; 
	public static final String WIFI_PASSWORD = "12345678";
	
	private SharedPreferences preferences;
	
	private EditText ssid;
	private EditText password;
	private Button igone;
	private Button next;
	
	private TextView tips;
    public static final int AUTO = 1;
    public static final int MANUAL = 2;
    public int TYPE;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.wifisetting_main);
		
		
		TYPE = getIntent().getIntExtra("type", AUTO);
		
		
		preferences = getSharedPreferences("systemsave", 0);
		ssid = (EditText)findViewById(R.id.ssid);
		password = (EditText)findViewById(R.id.password);
		igone = (Button)findViewById(R.id.igone);
		next = (Button)findViewById(R.id.next);
		
		tips = (TextView)findViewById(R.id.tips);
		updateWifiTips();
		
		igone.setOnClickListener(onClickListener);
		next.setOnClickListener(onClickListener);
		
		
		ssid.setText(preferences.getString("wifi_ssid", WIFI_SSID));
		ssid.setSelection(ssid.getText().toString().length());
		password.setText(preferences.getString("wifi_password", WIFI_PASSWORD));
		password.setSelection(password.getText().toString().length());
	}
	
	View.OnClickListener onClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.next:
				preferences.edit().putString("wifi_ssid", ssid.getText().toString().trim()).commit();
				preferences.edit().putString("wifi_password", password.getText().toString().trim()).commit();
				Toast.makeText(WifiSetting.this, "保存wifi信息成功,请重新测试", 2000).show();
			case R.id.igone:
				updateWifiTips();
				next();
				break;
			}
		}
	};
	
	
	private void updateWifiTips(){
    	tips.setText("当前使用的默认WiFi\n"+
                     "路由器:    "+preferences.getString("wifi_ssid", WIFI_SSID) + "\n"+
                     "密码:     "+preferences.getString("wifi_password", WIFI_PASSWORD)+"\n"+
                     "加密方式:    "+"WPA/PSK"+"\n");
    }
	
	
	private void next(){
		Intent intent =null;
		if(TYPE==AUTO){
			intent = new Intent(WifiSetting.this,MainActivity.class);
		}else if(TYPE==MANUAL){
			intent = new Intent(WifiSetting.this,NetworkTest.class);
			intent.putExtra("index", NetworkTest.TEST_WIFI);
		}
		startActivity(intent);
		finish();
	}
}
