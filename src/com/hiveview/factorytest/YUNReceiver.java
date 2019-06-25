

package com.hiveview.factorytest;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class YUNReceiver extends BroadcastReceiver
{
    private final static String TAG = "YUNReceiver";
    private static final boolean DEBUG = true;
    private static final HandlerThread sWorkerThread = new HandlerThread("ImtReceiver");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());	

    /*
     * 检测U盘里面的视频文件，如果为TEST.mp4 ，那么直接进入循环播放的模式
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(DEBUG) Log.d(TAG, "recv action." + action);
        if( action.equals(Intent.ACTION_MEDIA_MOUNTED) ) {
        	if(DEBUG) Log.d(TAG,"recv ACTION_MEDIA_MOUNTED.");
            String path = intent.getData().getPath();
            if(DEBUG) Log.d(TAG, "media is mounted to '" + path + "'." );
//            sWorker.post(new VideoRunnable(context, path));
        }
    }
    
    class VideoRunnable implements Runnable{
    	private Context mContext;
    	private String mPath;
    	
    	public VideoRunnable(Context context, String path) {
			mContext = context;
			mPath = path;
		}
    	
		@Override
		public void run() {
			// media is mounted to '/mnt/usb_storage/USB_DISK0'.
			// /mnt/usb_storage/USB_DISK0/udisk0
			final String videoFile = "TEST.mp4";
			//
			if(DEBUG) Log.d(TAG, "run");
			if(mPath.contains("/storage/external_storage")){
				File file = new File(mPath);
				if(null != file){
					File files[] = file.listFiles();
					if(null != files){
						for(File file1 : files){
							if(file1.isFile()){
								Log.v(TAG, "filepath=="+file1.getName());
							 
								if(file1.getName().equals(videoFile)){
									Intent intent = new Intent(mContext, VideoPlay.class);
									intent.putExtra("PATH", file1.getPath());
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
									mContext.startActivity(intent);
									break;
								}
							}						
						}
					}
				}
			}
		}    	
    }
}


