package com.hiveview.factorytest;

 

import com.hiveview.factorytest.R;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.MediaController;

public class VideoPlay extends Activity {
	private static final String TAG = "VideoPlay";
	private static final boolean DEBUG = false;
	private Context mContext;
	private Button mBtnPlay;
	private MyVideoView mVideoView;
	private MediaController mMediaController;
	private String mVideoPath;
	
	//
	private MediaPlayer mMediaPlayer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_SHOW_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_videoplay);
        
        mContext = this;
        
        Intent intent = getIntent();        
        if(null != intent){
        	mVideoPath = intent.getStringExtra("PATH");
        	if(DEBUG) Log.d(TAG, "mVideoPath." + mVideoPath);
        }        
        
        mVideoView = (MyVideoView) findViewById(R.id.vv_main);
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(null != mVideoPath){
					playVideo(mVideoPath);
				}
			}
		});
        mVideoView.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				finish();
				return true;
			}
		});
        mMediaController = new MediaController(this);        
        mMediaPlayer = new MediaPlayer();

         
        if(null != mVideoPath){
        	playVideo(mVideoPath);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	if(null != mMediaPlayer){
    		if(mMediaPlayer.isPlaying()){
    			mMediaPlayer.stop();
    		}
    		mMediaPlayer.release();
    	}    	
    	super.onDestroy();
    }
    
    private void playVideo(String path){
    	mVideoView.setVideoPath(path);
    	mVideoView.setMediaController(mMediaController);
    	mMediaController.setMediaPlayer(mVideoView);
    	mVideoView.requestFocus();    	
    	mVideoView.start();    	
    }     
}
