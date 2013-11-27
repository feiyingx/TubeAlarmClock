package com.tubealarmclock.code;

public class Constants {
	public static final String LOG_TAG = "TubeAlarmClock"; 
	public static final String YOUTUBE_API_KEY = "AIzaSyATuLF2EtltopvT2pDdljW_RU62YGLHqEg";//NOTE: I had to use the 'browser app' api key in order for our youtube service to work. The 'android app' api key kept returning '403 Access Not Configured'
	//Following 2 keys are used for the YouTube data api to query for videos
	public static final String YOUTUBE_DEVELOPER_KEY = "AI39si6uRcWJPa0a91IAD3wKHdTvQHEkfoRX6ztmQKEcTN8o0IuiTgDWXk0e4Ogng0i_DKp-6cXTqCNwCqlNcX5Mv3iVK0nJ-w";
	public static final String YOUTUBE_CLIENT_ID = "AnonLabs.TubeAlarmClock";
	
	public class ExtraTag{
		public static final String AlarmId = "alarmId";
		public static final String Hour = "hour";
		public static final String Minute = "minute";
		public static final String VideoId = "videoId";
		public static final String VideoTitle = "videoTitle";
		public static final String IsRepeat = "isRepeat";
	}
	
	public class RequestCode{
		public static final int REQUEST_SET_TIME = 7000;
		public static final int REQUEST_SEARCH_VIDEO = 7001;
		public static final int REQUEST_SET_RINGTONE = 7002;
	}
	
	public class ResultCode{
		public static final int REQUEST_OK = 6000;
		public static final int REQUEST_CANCELLED = 6001;
	}
	
	public class IntentAction{
		//It is android convention to prefix application specific action name with package
		public static final String INTENT_ACTION_REPEAT_MON = "com.tubealarmclock.code.ACTION_REPEAT_MON";
		public static final String INTENT_ACTION_REPEAT_TUE = "com.tubealarmclock.code.ACTION_REPEAT_TUE";
		public static final String INTENT_ACTION_REPEAT_WED = "com.tubealarmclock.code.ACTION_REPEAT_WED";
		public static final String INTENT_ACTION_REPEAT_THU = "com.tubealarmclock.code.ACTION_REPEAT_THU";
		public static final String INTENT_ACTION_REPEAT_FRI = "com.tubealarmclock.code.ACTION_REPEAT_FRI";
		public static final String INTENT_ACTION_REPEAT_SAT = "com.tubealarmclock.code.ACTION_REPEAT_SAT";
		public static final String INTENT_ACTION_REPEAT_SUN = "com.tubealarmclock.code.ACTION_REPEAT_SUN";
		public static final String INTENT_ACTION_SNOOZE_ALARM = "com.tubealarmclock.code.ACTION_SNOOZE_ALARM";
	}
	
	public static final String DefaultVideoId = "WcLtNq3XQ5I";
	public static final String DefaultVideoTitle = "Rise & Shine";
	public static final int DefaultVideoDuration = 205;
	
	public static final int VideoTitleMaxLength = 75;
	
	public enum TimeFormat {
		HOUR12(12),
		HOUR24(24);
		
		final int value;
		TimeFormat(int val) { 
			this.value = val; 
		}
		
		public int value() { return value; }
		
		public static TimeFormat getTimeFormat(int code) {
			switch (code) {
				case 12: return HOUR12;
				case 24: return HOUR24;
				default: return HOUR12;
			}
		}
	}
}
