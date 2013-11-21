package com.tubealarmclock.data;

import java.util.ArrayList;
import java.util.List;

import com.tubealarmclock.code.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AlarmsDataSource {
	//Database fields
	private SQLiteDatabase db;
	private DBHelper dbHelper;
	private String[] allColumns = {
		DBHelper.COLUMN_ID,
		DBHelper.COLUMN_HOUR,
		DBHelper.COLUMN_MINUTE,
		DBHelper.COLUMN_DISPLAY_FORMAT,
		DBHelper.COLUMN_IS_ON,
		DBHelper.COLUMN_MON,
		DBHelper.COLUMN_TUE,
		DBHelper.COLUMN_WED,
		DBHelper.COLUMN_THU,
		DBHelper.COLUMN_FRI,
		DBHelper.COLUMN_SAT,
		DBHelper.COLUMN_SUN,
		DBHelper.COLUMN_VIDEO_ID,
		DBHelper.COLUMN_VIDEO_TITLE,
		DBHelper.COLUMN_VIDEO_DURATION
	};
	
	public AlarmsDataSource(Context context){
		dbHelper = new DBHelper(context);
	}
	
	public void open() throws SQLException{
		db = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
	}
	
	//Insert a new alarm into the db, and retrieve it from the db and return it
	public Alarm createAlarm(Alarm alarm){
		//Put all the values in the Alarm object into ContentValues which will be used to insert into the db
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_HOUR, alarm.getHour());
		values.put(DBHelper.COLUMN_MINUTE, alarm.getMinute());
		values.put(DBHelper.COLUMN_DISPLAY_FORMAT, alarm.getFormat());
		values.put(DBHelper.COLUMN_IS_ON, alarm.isOn() ? 1 : 0);
		if(alarm.isRepeat()){
			boolean[] repeatDays = alarm.getRepeatDays();
			if(repeatDays[0]) values.put(DBHelper.COLUMN_MON, 1); 
			if(repeatDays[1]) values.put(DBHelper.COLUMN_TUE, 1);
			if(repeatDays[2]) values.put(DBHelper.COLUMN_WED, 1);
			if(repeatDays[3]) values.put(DBHelper.COLUMN_THU, 1);
			if(repeatDays[4]) values.put(DBHelper.COLUMN_FRI, 1);
			if(repeatDays[5]) values.put(DBHelper.COLUMN_SAT, 1);
			if(repeatDays[6]) values.put(DBHelper.COLUMN_SUN, 1);
		}
		values.put(DBHelper.COLUMN_VIDEO_ID, alarm.getVideoId());
		values.put(DBHelper.COLUMN_VIDEO_TITLE, alarm.getVideoTitle());
		values.put(DBHelper.COLUMN_VIDEO_DURATION, alarm.getVideoDuration());
		
		//Get the alarm id by inserting the values, then retrieve the newly created Alarm from db and return it
		long insertId = db.insert(DBHelper.TABLE_ALARMS, null, values);
		Cursor cursor = db.query(DBHelper.TABLE_ALARMS, allColumns, DBHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		Alarm insertedAlarm = cursorToAlarm(cursor);
		cursor.close();
		
		return insertedAlarm;
	}
	
	//Delete the alarm with the given id
	public void deleteAlarm(long id){
		Log.d(Constants.LOG_TAG, "Alarm deleted with id: " + id);
		db.delete(DBHelper.TABLE_ALARMS, DBHelper.COLUMN_ID + " = " + id, null);
	}
	
	//Retrieve all Alarm rows from db
	public List<Alarm> getAllAlarms(){
		List<Alarm> alarms = new ArrayList<Alarm>();
		
		Cursor cursor = db.query(DBHelper.TABLE_ALARMS, allColumns, null, null, null, null, DBHelper.COLUMN_HOUR + " ASC, " + DBHelper.COLUMN_MINUTE + " ASC");
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Alarm alarm = cursorToAlarm(cursor);
			alarms.add(alarm);
			cursor.moveToNext();
		}
		cursor.close();
		return alarms;
	}
	
	public Alarm updateAlarm(Alarm alarm){
		//Put all the values in the Alarm object into ContentValues which will be used to insert into the db
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_HOUR, alarm.getHour());
		values.put(DBHelper.COLUMN_MINUTE, alarm.getMinute());
		values.put(DBHelper.COLUMN_DISPLAY_FORMAT, alarm.getFormat());
		values.put(DBHelper.COLUMN_IS_ON, alarm.isOn() ? 1 : 0);
		
		boolean[] repeatDays = alarm.getRepeatDays();
		values.put(DBHelper.COLUMN_MON, repeatDays[0] ? 1 : 0); 
		values.put(DBHelper.COLUMN_TUE, repeatDays[1] ? 1 : 0);
		values.put(DBHelper.COLUMN_WED, repeatDays[2] ? 1 : 0);
		values.put(DBHelper.COLUMN_THU, repeatDays[3] ? 1 : 0);
		values.put(DBHelper.COLUMN_FRI, repeatDays[4] ? 1 : 0);
		values.put(DBHelper.COLUMN_SAT, repeatDays[5] ? 1 : 0);
		values.put(DBHelper.COLUMN_SUN, repeatDays[6] ? 1 : 0);
		
		values.put(DBHelper.COLUMN_VIDEO_ID, alarm.getVideoId());
		values.put(DBHelper.COLUMN_VIDEO_TITLE, alarm.getVideoTitle());
		values.put(DBHelper.COLUMN_VIDEO_DURATION, alarm.getVideoDuration());
		
		//Get the alarm id by inserting the values, then retrieve the newly created Alarm from db and return it
		db.update(DBHelper.TABLE_ALARMS, values, DBHelper.COLUMN_ID +  " = " + alarm.getId(), null);
		
		return alarm;
	}
	
	public Alarm getAlarm(long id){
		Cursor cursor = db.query(DBHelper.TABLE_ALARMS, allColumns, DBHelper.COLUMN_ID + " = " + id, null, null, null, null);
		
		Alarm alarm;
		if(!cursor.moveToFirst() || cursor.getCount() == 0){
			alarm = null;
		}else{
			alarm = cursorToAlarm(cursor);
		}
		cursor.close();
		return alarm;
	}
	
	//Translate db fields into Alarm
	private Alarm cursorToAlarm(Cursor cursor){
		Alarm alarm = new Alarm();
		alarm.setId(cursor.getLong(DBHelper.COLUMN_INDEX_ID));
		alarm.setHour(cursor.getInt(DBHelper.COLUMN_INDEX_HOUR));
		alarm.setMinute(cursor.getInt(DBHelper.COLUMN_INDEX_MINUTE));
		alarm.setFormat(cursor.getInt(DBHelper.COLUMN_INDEX_DISPLAY_FORMAT));
		boolean isOn = cursor.getInt(DBHelper.COLUMN_INDEX_IS_ON) == 1;
		alarm.setIsOn(isOn);
		int monVal = cursor.getInt(DBHelper.COLUMN_INDEX_MON);
		int tueVal = cursor.getInt(DBHelper.COLUMN_INDEX_TUE);
		int wedVal = cursor.getInt(DBHelper.COLUMN_INDEX_WED);
		int thuVal = cursor.getInt(DBHelper.COLUMN_INDEX_THU);
		int friVal = cursor.getInt(DBHelper.COLUMN_INDEX_FRI);
		int satVal = cursor.getInt(DBHelper.COLUMN_INDEX_SAT);
		int sunVal = cursor.getInt(DBHelper.COLUMN_INDEX_SUN);
		
		boolean[] repeatDays = new boolean[7];
		repeatDays[0] = (monVal == 1);
		repeatDays[1] = (tueVal == 1);
		repeatDays[2] = (wedVal == 1);
		repeatDays[3] = (thuVal == 1);
		repeatDays[4] = (friVal == 1);
		repeatDays[5] = (satVal == 1);
		repeatDays[6] = (sunVal == 1);
		
		alarm.setRepeatDays(repeatDays);
		
		alarm.setVideoId(cursor.getString(DBHelper.COLUMN_INDEX_VIDEO_ID));
		alarm.setVideoTitle(cursor.getString(DBHelper.COLUMN_INDEX_VIDEO_TITLE));
		alarm.setVideoDuration(cursor.getInt(DBHelper.COLUMN_INDEX_VIDEO_DURATION));
		
		return alarm;
	}
}
