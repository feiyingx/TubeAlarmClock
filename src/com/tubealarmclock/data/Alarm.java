package com.tubealarmclock.data;

import android.net.Uri;

public class Alarm {
	private long id;
	private int hour; //Always store hour in 24 hour format
	private int minute;
	private int displayFormat;
	private boolean isSetMon;
	private boolean isSetTue;
	private boolean isSetWed;
	private boolean isSetThu;
	private boolean isSetFri;
	private boolean isSetSat;
	private boolean isSetSun;
	private boolean isOn;
	//Video fields
	private String videoId;
	private String videoTitle;
	private int durationSeconds;
	private Uri ringtoneUri;
	
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}
	
	public int getHour(){
		return hour;
	}
	
	public void setHour(int hourIn24HourFormat){
		this.hour = hourIn24HourFormat;
	}
	
	public int getMinute(){
		return minute;
	}
	
	public void setMinute(int minute){
		this.minute = minute;
	}
	
	public int getFormat(){
		return displayFormat;
	}
	
	public void setFormat(int format){
		this.displayFormat = format;
	}
	
	public boolean isRepeat(){
		return isSetMon || isSetTue || isSetWed || isSetThu || isSetFri || isSetSat || isSetSun;
	}
	
	public boolean[] getRepeatDays(){
		boolean[] repeatDays = new boolean[7];
		repeatDays[0] = isSetMon;
		repeatDays[1] = isSetTue;
		repeatDays[2] = isSetWed;
		repeatDays[3] = isSetThu;
		repeatDays[4] = isSetFri;
		repeatDays[5] = isSetSat;
		repeatDays[6] = isSetSun;
		return repeatDays;
	}
	
	public void setRepeatDays(boolean[] repeatDays){
		if(repeatDays == null || repeatDays.length != 7)
			return;
		
		this.isSetMon = repeatDays[0];
		this.isSetTue = repeatDays[1];
		this.isSetWed = repeatDays[2];
		this.isSetThu = repeatDays[3];
		this.isSetFri = repeatDays[4];
		this.isSetSat = repeatDays[5];
		this.isSetSun = repeatDays[6];
	}
	
	public Uri getRingtoneUri(){
		return this.ringtoneUri;
	}
	
	public void setRingtoneUri(Uri ringtone){
		this.ringtoneUri = ringtone;
	}
	
	public String toTimeString(){
		String time = "";
		if(this.displayFormat == 12){
			if(hour == 12 || hour == 0){ //If the time is 12, then we want to display it as 12:xx
				time = String.format("12:%02d", minute); 
			}else{ //Otherwise, we can just mod the hour by 12
				time = String.format("%02d:%02d", hour % 12, minute);
			}
		}else{
			time = String.format("%02d:%02d", hour, minute);
		}
		
		return time;
	}
	
	public String toTimeString(boolean includeAMPM){
		String timeString = this.toTimeString();
		if(includeAMPM){
			if(hour >= 12){
				timeString += "pm";
			}else{
				timeString += "am";
			}
		}
		return timeString;
	}
	
	public boolean isOn(){
		return isOn;
	}
	
	public void setIsOn(boolean isOn){
		this.isOn = isOn;
	}
	
	public String getVideoId(){
		return videoId;
	}
	
	public void setVideoId(String videoId){
		this.videoId = videoId;
	}
	
	public String getVideoTitle(){
		return videoTitle;
	}
	
	public void setVideoTitle(String title){
		this.videoTitle = title;
	}
	
	public int getVideoDuration(){
		return durationSeconds;
	}
	
	public void setVideoDuration(int durationInSeconds){
		this.durationSeconds = durationInSeconds;
	}
	
	//Will return a debug string containing information about the alarm's properties
	public String toDebugString(){
		boolean[] repeatDays = getRepeatDays();
		String debugString = String.format("[Alarm Info] ID: %d | Time: %s | Format: %d | IsOn: %b | VideoId: %s | VideoTitle: %s | Duration: %d secs | Repeat: %b,%b,%b,%b,%b,%b,%b | Backup ringtone: %s", getId(), toTimeString(), getFormat(), isOn(), getVideoId(), getVideoTitle(), getVideoDuration(), repeatDays[0], repeatDays[1], repeatDays[2], repeatDays[3], repeatDays[4], repeatDays[5], repeatDays[6], Uri.encode(getRingtoneUri().toString()));
		return debugString;
	}
}
