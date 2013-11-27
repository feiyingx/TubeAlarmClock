package com.tubealarmclock.mobile;

import com.tubealarmclock.code.Constants;
import com.tubealarmclock.code.Utility;
import com.tubealarmclock.data.Alarm;
import com.tubealarmclock.data.AlarmsDataSource;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class CancelSnoozeActivity extends Activity {
	AlarmsDataSource mDatasource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDatasource = new AlarmsDataSource(this);
		mDatasource.open();
		
		Bundle extras = getIntent().getExtras();
		long alarmId = extras.getLong(Constants.ExtraTag.AlarmId);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		//Set a single snooze alarm
		Intent snoozeAlarmIntent = new Intent(getApplicationContext(), SingleAlarmReceiver.class);
		snoozeAlarmIntent.putExtra(Constants.ExtraTag.AlarmId, alarmId);
		snoozeAlarmIntent.setAction(Constants.IntentAction.INTENT_ACTION_SNOOZE_ALARM);
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), (int)alarmId, snoozeAlarmIntent, 0);
		
		//Use alarm manager to cancel the snooze alarm
		am.cancel(sender);
		Log.d(Constants.LOG_TAG, "SNOOZE Alarm cancelled for alarm: " + alarmId);
		
		//Dismiss the notification since the user triggered the notification action
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel((int)alarmId);
		
		//If this was a one-time alarm, then we will now delete it since the alarm has triggered and its snooze is now cancelled.
		Alarm alarm = mDatasource.getAlarm(alarmId);
		if(alarm != null && !alarm.isRepeat()){
			mDatasource.deleteAlarm(alarmId);
		}
		mDatasource.close();
		
		//After canceling, take user to AlarmsActivity
		Intent alarmsActivityIntent = new Intent(getApplicationContext(), AlarmsActivity.class);
		startActivity(alarmsActivityIntent);
	}

}
