package com.tubealarmclock.data;

import com.tubealarmclock.code.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.RingtoneManager;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{

	public static final String TABLE_ALARMS = "alarms";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_HOUR = "hour"; //hour and minute will always be stored in 24-hr format
	public static final String COLUMN_MINUTE = "minute";
	public static final String COLUMN_DISPLAY_FORMAT = "display_format"; //12 hour or 24 hour
	public static final String COLUMN_IS_ON = "is_on";
	public static final String COLUMN_MON = "mon";
	public static final String COLUMN_TUE = "tue";
	public static final String COLUMN_WED = "wed";
	public static final String COLUMN_THU = "thu";
	public static final String COLUMN_FRI = "fri";
	public static final String COLUMN_SAT = "sat";
	public static final String COLUMN_SUN = "sun";
	//VIDEO COLUMNS
	public static final String COLUMN_VIDEO_ID = "video_id";
	public static final String COLUMN_VIDEO_TITLE = "video_title";
	public static final String COLUMN_VIDEO_DURATION = "video_duration";
	
	public static final String COLUMN_RINGTONE_URI = "ringtone_uri";
	
	private static final String DATABASE_NAME = "alarms.db";
	private static final int DATABASE_VERSION = 2;
	
	//Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_ALARMS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_HOUR
			+ " integer not null, " + COLUMN_MINUTE
			+ " integer not null, " + COLUMN_DISPLAY_FORMAT
			+ " integer not null, " + COLUMN_IS_ON
			+ " integer, " + COLUMN_MON
			+ " integer, " + COLUMN_TUE
			+ " integer, " + COLUMN_WED
			+ " integer, " + COLUMN_THU
			+ " integer, " + COLUMN_FRI
			+ " integer, " + COLUMN_SAT
			+ " integer, " + COLUMN_SUN
			+ " integer, " + COLUMN_VIDEO_ID
			+ " text, " + COLUMN_VIDEO_TITLE
			+ " text, " + COLUMN_VIDEO_DURATION
			+ " integer);";
	
	public static final int COLUMN_INDEX_ID = 0;
	public static final int COLUMN_INDEX_HOUR = 1;
	public static final int COLUMN_INDEX_MINUTE = 2;
	public static final int COLUMN_INDEX_DISPLAY_FORMAT = 3;
	public static final int COLUMN_INDEX_IS_ON = 4;
	public static final int COLUMN_INDEX_MON = 5;
	public static final int COLUMN_INDEX_TUE = 6;
	public static final int COLUMN_INDEX_WED = 7;
	public static final int COLUMN_INDEX_THU = 8;
	public static final int COLUMN_INDEX_FRI = 9;
	public static final int COLUMN_INDEX_SAT = 10;
	public static final int COLUMN_INDEX_SUN = 11;
	public static final int COLUMN_INDEX_VIDEO_ID = 12;
	public static final int COLUMN_INDEX_VIDEO_TITLE = 13;
	public static final int COLUMN_INDEX_VIDEO_DURATION = 14;
	public static final int COLUMN_INDEX_RINGTONE_URI = 15;
	
	public DBHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(newVersion){
			case 2:
				//In version 2, we are adding a new column, ringtone (which is used for back up alarm if no internet to play youtube alarm)
				db.execSQL("ALTER TABLE " + TABLE_ALARMS + " ADD COLUMN " + COLUMN_RINGTONE_URI + " text");
				Log.w(Constants.LOG_TAG, "Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will add new column " + COLUMN_RINGTONE_URI);
				break;
			default:
				break;
		}
		
		//onCreate(db);
	}

}


