package com.tubealarmclock.code;

import java.util.Calendar;

import com.tubealarmclock.data.Alarm;

import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.content.Context;
import android.content.Intent;

public class Utility {
	public static Typeface getTypeface(Context context, String fontName){
		Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
		return typeface;
	}
	
	//Will convert seconds into hh:mm:ss format
	public static String getTimeStringFromSeconds(int seconds){
		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		int remainingSeconds = ((seconds % 3600) % 60);
		String result;
		if(hours > 0){
			result = String.format("%d:%02d:%02d", hours, minutes, remainingSeconds);
		}else{
			result = String.format("%d:%02d", minutes, remainingSeconds);
		}
		return result;
	}
	
	//Truncate a string down to the limit, and add '...'
	public static String truncateString(String input, int truncationLimit){
		if(input.length() <= truncationLimit)
			return input;
		
		return String.format(input.substring(0, truncationLimit-3) +  "...");
	}
	
	//Returns the action name for a given day index: 0 - Monday, 1 - Tuesday, ... 6 - Sunday
	public static String getIntentActionNameByDay(int dayIndex){
		switch(dayIndex){
			case 0:
				return Constants.IntentAction.INTENT_ACTION_REPEAT_MON;
			case 1:
				return Constants.IntentAction.INTENT_ACTION_REPEAT_TUE;
			case 2:
				return Constants.IntentAction.INTENT_ACTION_REPEAT_WED;
			case 3:
				return Constants.IntentAction.INTENT_ACTION_REPEAT_THU;
			case 4:
				return Constants.IntentAction.INTENT_ACTION_REPEAT_FRI;
			case 5:
				return Constants.IntentAction.INTENT_ACTION_REPEAT_SAT;
			case 6:
				return Constants.IntentAction.INTENT_ACTION_REPEAT_SUN;
			default:
				return "";
		}
	}
	
	public static Intent prepareAlarmIntent(Context context, Alarm alarm, Class<?> receiverClass){
		Intent alarmIntent = new Intent(context, receiverClass);
		alarmIntent.putExtra(Constants.ExtraTag.AlarmId, alarm.getId());
		return alarmIntent;
	}
	
	public static Calendar getNextAlarmTime(Alarm alarm){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, alarm.getHour());
		cal.set(Calendar.MINUTE, alarm.getMinute());
		cal.set(Calendar.SECOND, 0);
		
		boolean[] repeatDays = alarm.getRepeatDays();
		int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		int numDaysTilNextAlarm = 0;
		//JAVA Day constants: Sunday - 1, Monday - 2, ... , Sat - 7
		//Our day indexes: Monday = 0, Tuesday = 1, Sunday = 6
		//Conversion from JAVA Day to our index is: 1->6, 2->0, 3->1, 4->2, ...
		//((JAVA constant - 1) + 6) % 7
		int currentDayIndex = ((currentDay - 1)+6)%7; //Transform Java's Day_Of_Week constant to our dayIndex
		
		//Check whether there is an alarm today, if so, check whether we have passed the time yet
		boolean firstAlarmIsToday = repeatDays[currentDayIndex] && cal.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();  
		
		if(!firstAlarmIsToday){
			currentDayIndex = (currentDayIndex + 1)%7; //Skip today and start checking the next day
			numDaysTilNextAlarm++;
			//Keep looping until we find the first day that needs to be repeated
			while(!repeatDays[currentDayIndex]){
				currentDayIndex = (currentDayIndex + 1)%7;
				numDaysTilNextAlarm++; //Track how many days until the next alarm
				Log.d(Constants.LOG_TAG, "First day to repeat is: " + currentDayIndex);
			}
		}
		
		if(numDaysTilNextAlarm > 0)
			cal.add(Calendar.HOUR, 24*numDaysTilNextAlarm);
		return cal;
	}
	
	public static boolean isOnline(Context c){
		ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		//NetInfo will be null if the device is in airplane mode, or any other situations when there's no available network
		if(netInfo != null && netInfo.isConnected()){
			return true;
		}
		return false;
	}
}
