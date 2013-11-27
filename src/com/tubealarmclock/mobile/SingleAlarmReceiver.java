package com.tubealarmclock.mobile;

import com.tubealarmclock.code.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.WindowManager;

public class SingleAlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		long alarmId = extras.getLong(Constants.ExtraTag.AlarmId);
		
		//Log.d(Constants.LOG_TAG, String.format("Single alarm %d triggered", alarmId));
		
		//AlarmWakeLock.acquireWakeLock(context);
		//Create intent for AlertActivity
		Intent alertIntent = new Intent(context, AlertActivity.class);
		alertIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		alertIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		alertIntent.putExtra(Constants.ExtraTag.AlarmId, alarmId);
		context.startActivity(alertIntent);
	}

}
