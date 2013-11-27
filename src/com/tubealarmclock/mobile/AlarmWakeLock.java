package com.tubealarmclock.mobile;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.WindowManager;

public class AlarmWakeLock {
	private static PowerManager.WakeLock mWakeLock;
	
	static void acquireWakeLock(Context context){
		if(mWakeLock != null)
			return;
		
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "TubeAlarmClock");
		mWakeLock.acquire();
	}
	
	static void releaseWakeLock(){
		if(mWakeLock != null){
			mWakeLock.release();
			mWakeLock = null;
		}
	}
	
	static boolean isAcquired(){
		if(mWakeLock == null)
			return false;
		return mWakeLock.isHeld();
	}
	
	static boolean isScreenOn(Context context){
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
}
