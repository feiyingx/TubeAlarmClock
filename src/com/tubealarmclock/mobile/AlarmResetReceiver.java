package com.tubealarmclock.mobile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.tubealarmclock.code.Constants;
import com.tubealarmclock.code.Utility;
import com.tubealarmclock.data.Alarm;
import com.tubealarmclock.data.AlarmsDataSource;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmResetReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		//This receiver gets triggered when:
		//Phone reboots, phone time got reset, locale changes, time zone changes
		
		//So schedule all the alarms
		AlarmsDataSource alarmDataSource = new AlarmsDataSource(context);
		alarmDataSource.open();
		List<Alarm> alarms = alarmDataSource.getAllAlarms();
		if(alarms != null){
			for(int i = 0; i < alarms.size(); i++){
				setAlarm(alarms.get(i), context);
			}
		}
	}

	private void setAlarm(Alarm alarm, Context context){
		//Determine the alarm time
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, alarm.getHour());
		cal.set(Calendar.MINUTE, alarm.getMinute());
		cal.set(Calendar.SECOND, 0);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long alarmTime = cal.getTimeInMillis();
		//Check whether alarm time is in the past, if so, then add 1 day to the alarm so that it triggers tomorrow
		if(alarmTime <= currentTime){
			cal.add(Calendar.HOUR, 24);
			alarmTime = cal.getTimeInMillis();
		}
		
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		if(!alarm.isRepeat()){
			//Single alarm
			Intent singleAlarmIntent = Utility.prepareAlarmIntent(context, alarm, SingleAlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(context, (int)alarm.getId(), singleAlarmIntent, 0);
			
			//Use alarm manager to set the alarm
			am.set(AlarmManager.RTC_WAKEUP, alarmTime, sender);
			
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Log.d(Constants.LOG_TAG, "Alarm set for: " + dateFormatter.format(cal.getTime()));
			
		}else{
			//If this is a repeat alarm, then
			//Calculate when the first alarm of the repeating series needs to be triggered based on the current day and time
			//Update AlertActivity to handle repeating alarms, since we can no longer use 'setRepeating', we will have to
			//Manually check for when the next alarm of the series needs to be and then set it.	
			Calendar nextAlarmTime = Utility.getNextAlarmTime(alarm);
			Intent repeatAlarmIntent = Utility.prepareAlarmIntent(context, alarm, RepeatAlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(context, (int)alarm.getId(), repeatAlarmIntent, 0);
	
			am.set(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), sender);
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Log.d(Constants.LOG_TAG, "Repeat Alarm set for: " + dateFormatter.format(nextAlarmTime.getTime()));
	
		}
	}

}
