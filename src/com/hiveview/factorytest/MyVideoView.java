package com.hiveview.factorytest;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.VideoView;

public class MyVideoView extends VideoView {
	public static final String TAG = "MyVideoView";
	public static final boolean DEBUG = false;
	private Context mContext;
	private int mMaxWidth;
	private int mMaxHeight;
	
	public MyVideoView(Context context) {
        super(context);
        mContext = context;
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	// TODO Auto-generated method stub
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	getDisplayWidthAndHeight();
    	setMeasuredDimension(mMaxWidth, mMaxHeight);
    }
    
    private void getDisplayWidthAndHeight(){ 
    	DisplayMetrics dm = new DisplayMetrics();
    	((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);    	
    	if(DEBUG) Log.d(TAG, "w." + dm.widthPixels + " h." + dm.heightPixels);
    	// true: 全屏显示 即高度加上状态栏高度
    	// false: 留有状态栏
    	if(true){
    		// 如放在SDK源码中编译,可选择该方式
    		//dm.heightPixels += getResources().getDimension(com.android.internal.R.dimen.navigation_bar_height);
    		// 该方式要注意android版本. 2.3 读取的是."status_bar_height"
    		dm.heightPixels += getNavigationBarHeight();
		  	mMaxWidth = dm.widthPixels;
		  	mMaxHeight = dm.heightPixels;
	    }else{
	    	mMaxWidth = dm.widthPixels;
		  	mMaxHeight = dm.heightPixels;
	    }
    }
    
    private int getNavigationBarHeight(){
    	Class<?> c = null;
    	Object obj = null;
    	Field field = null;
    	int x = 0, sbar = 0;
    	try {
    	    c = Class.forName("com.android.internal.R$dimen");
    	    obj = c.newInstance();
    	    // 2.3 "status_bar_height"
    	    field = c.getField("navigation_bar_height");
    	    x = Integer.parseInt(field.get(obj).toString());
    	    sbar = getResources().getDimensionPixelSize(x);
    	    if(DEBUG) Log.d(TAG, "sbar." + sbar);
    	} catch (Exception e1) {
    		Log.w(TAG, "get status bar height fail");
    	    e1.printStackTrace();
    	}
    	return sbar;
    }
}
