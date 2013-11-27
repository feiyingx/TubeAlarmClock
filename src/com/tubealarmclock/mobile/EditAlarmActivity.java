package com.tubealarmclock.mobile;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.tubealarmclock.code.Constants;
import com.tubealarmclock.code.Utility;
import com.tubealarmclock.customview.TypefacedTextView;
import com.tubealarmclock.data.Alarm;
import com.tubealarmclock.data.AlarmsDataSource;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//EditAlarm will be used for both 'Editing' an alarm or 'Creating' a new alarm. The only difference is that in an 'Edit', the alarm fields will already be prepopulated
public class EditAlarmActivity extends YouTubeFailureRecoveryActivity{
	TypefacedTextView mTxtTime, mTxtVideoTitle;
	TextView mTxtDuration;
	ToggleButton mToggleBtnAM, mToggleBtnPM, mToggleBtnMon, mToggleBtnTue, mToggleBtnWed, mToggleBtnThu, mToggleBtnFri, mToggleBtnSat, mToggleBtnSun;
	YouTubePlayerView youTubeView;
	YouTubePlayer youTubePlayer;
	Button mBtnAlarmSettings, mBtnAlarmSettingsOn, mBtnEditVid, mBtnSelectRingtone;
	LinearLayout mLinlaySettings, mLinlayVideoSectionContainer;
	boolean mIsNewMode = false;
	private Alarm mAlarm;
	AlarmsDataSource mAlarmDatasource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(Constants.LOG_TAG, "onCreate");
		
		//Setup view mode between EDIT or NEW
		//If this is for creating a NEW alarm, then the alarmId passed in will be 0
		//If this is for EDITing an existing alarm, then the alarmId passed in will be greater than 0
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		long alarmId = extras.getLong(Constants.ExtraTag.AlarmId);
		mIsNewMode = (alarmId <= 0);
		//Log.d(Constants.LOG_TAG, "Edit/Create Alarm id: " + alarmId);
		mAlarmDatasource = new AlarmsDataSource(this);
		mAlarmDatasource.open();
		
		if(mIsNewMode){
			mAlarm = new Alarm();
			mAlarm.setHour(7);
			mAlarm.setMinute(15);
			mAlarm.setFormat(Constants.TimeFormat.HOUR12.value());
			mAlarm.setVideoId(Constants.DefaultVideoId);
			mAlarm.setVideoTitle(Constants.DefaultVideoTitle);
			mAlarm.setVideoDuration(Constants.DefaultVideoDuration);
			mAlarm.setIsOn(true); //Default to ON
			Uri defaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
			mAlarm.setRingtoneUri(defaultRingtone);
		}else{
			//retrieve alarm from db here
			mAlarm = mAlarmDatasource.getAlarm(alarmId);
			if(mAlarm == null){
				//Alarm wasnt found
				Log.d(Constants.LOG_TAG, "Edit Alarm id: " + alarmId +  " NOT FOUND");
				//Set to a default alarm
				mAlarm = new Alarm();
				mAlarm.setHour(7);
				mAlarm.setMinute(15);
				mAlarm.setFormat(Constants.TimeFormat.HOUR12.value());
				mAlarm.setVideoId(Constants.DefaultVideoId);
				mAlarm.setVideoTitle(Constants.DefaultVideoTitle);
				mAlarm.setVideoDuration(Constants.DefaultVideoDuration);
				mAlarm.setIsOn(true); //Default to ON
				Uri defaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
				mAlarm.setRingtoneUri(defaultRingtone);
			}else{
				//If we found the alarm that we want to edit, first turn off its pending intents in AlarmManager and turn them back on once edits r finished
				setAlarm(mAlarm, false);
			}
		}
		
		//Transparent action bar
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#50010509")));
		// Set action bar to display the home link as an 'up' (aka back) link
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		//Set layout xml
		setContentView(R.layout.activity_edit_alarm);
		
		//Find controls and bind event handlers
		initControls();
		
		//Set title to Edit/New accordingly
		if(mIsNewMode){
			setTitle(R.string.title_new);
		}else{
			setTitle(R.string.title_edit);
		}
		
		//Prepopulate our view with our Alarm
		prepopulateFields();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Log.d(Constants.LOG_TAG, "onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		// There are two menu xmls, one for NEW alarm, one for EDIT alarm
		if(mIsNewMode){
			getMenuInflater().inflate(R.menu.new_alarm, menu);
		}else{
			getMenuInflater().inflate(R.menu.edit_alarm, menu);
		}
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		int menuItemId = item.getItemId();
		switch(menuItemId){
			case android.R.id.home:
				//App icon in action bar clicked, go back up
				setAlarm(mAlarm, mAlarm.isOn());
				finish();
				return true;
			case R.id.action_accept:
				//If Edit, then save the alarm updates
				//If New, then create the alarm
				mAlarm.setIsOn(true); //Since we're saving our alarm, then automatically turn it on
				//Log.d(Constants.LOG_TAG, mAlarm.toDebugString());
				if(mIsNewMode){
					mAlarm = mAlarmDatasource.createAlarm(mAlarm);
				}else{
					mAlarm = mAlarmDatasource.updateAlarm(mAlarm);
				}
				//Set alarm once it's saved
				setAlarm(mAlarm, mAlarm.isOn());
				Intent alarmList = new Intent(this, AlarmsActivity.class);
				startActivity(alarmList);
				return true;
			case R.id.action_cancel:
				//Keep whatever setting the alarm was already on, since we're canceling out of any changes we made for the alarm
				//Unless this is a brand new alarm, then ignore it
				if(!mIsNewMode){
					setAlarm(mAlarm, mAlarm.isOn());
				}
				finish();
				return true;
			case R.id.action_delete:
				//This should only happen in Edit mode. In New mode, this menu option isn't displayed.
				if(mAlarm.getId() > 0){
					//No need to call turnOffAlarm because alarm should already be turned off on load
					mAlarmDatasource.deleteAlarm(mAlarm.getId());
					Log.d(Constants.LOG_TAG, "Deleting alarm: " + mAlarm.getId());
				}
				Intent alarmListIntent = new Intent(this, AlarmsActivity.class);
				startActivity(alarmListIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		//This means we received a result from the SetTimeActivity
		if(requestCode == Constants.RequestCode.REQUEST_SET_TIME){
			if(resultCode == Constants.ResultCode.REQUEST_OK){
				//Get the time data from the result
				Bundle resultData = data.getExtras();
				
				//Currently only support 12-hour format
				int hourIn24HourFormat = (Integer)resultData.get(Constants.ExtraTag.Hour);
				//Check if it is currently in 12-hour format, and if so then check if it is PM, if so, then add 12 hours to the time
				if(mAlarm.getFormat() == Constants.TimeFormat.HOUR12.value()){
					if(hourIn24HourFormat == 12){
						//If it is AM, then change it to 0
						if(mToggleBtnAM.isChecked()){
							hourIn24HourFormat = 0;
						}
						//Else if it is already PM, then keep 12:00 as 12:00PM
					}else if(mToggleBtnPM.isChecked()){
						//If it's any hour except 12 (1-11) and PM is checked, then we want to add 12 hours to it
						//So 1pm => 1+12=13:00, 11pm => 11+12=23:00
						hourIn24HourFormat += 12;
						hourIn24HourFormat = hourIn24HourFormat % 24; //We don't want to have a time that reads 24:10, should be 00:10 for example.
					}
				}
				int minute = (Integer)resultData.get(Constants.ExtraTag.Minute);
				Log.d(Constants.LOG_TAG, "Time set to: " + hourIn24HourFormat + ":" + minute);
				mAlarm.setHour(hourIn24HourFormat);
				mAlarm.setMinute(minute);
				//Update time in text view
				mTxtTime.setText(mAlarm.toTimeString());
			}
		}else if(requestCode == Constants.RequestCode.REQUEST_SEARCH_VIDEO){
			if(resultCode == Constants.ResultCode.REQUEST_OK){
				//Get video data from result
				Bundle resultData = data.getExtras();
				String videoId = (String)resultData.get(Constants.ExtraTag.VideoId);
				String videoTitle = (String)resultData.get(Constants.ExtraTag.VideoTitle);
				//Log.d(Constants.LOG_TAG, "Selected video is: " + videoId + "|" + videoTitle);
				
				//Update YouTube UI
				youTubePlayer.loadVideo(videoId);
				mTxtVideoTitle.setText(videoTitle);
				showVideo();
				//Update alarm data object
				mAlarm.setVideoId(videoId);
				mAlarm.setVideoTitle(videoTitle);
			}
		}else if(requestCode == Constants.RequestCode.REQUEST_SET_RINGTONE){
			if(resultCode == Activity.RESULT_OK){
				//Update alarm with the new ringtone
				Uri newUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				if(newUri != null){
					mAlarm.setRingtoneUri(newUri);
					mBtnSelectRingtone.setText(getRingtoneName(newUri));
				}
			}
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mAlarmDatasource.close();
	}
	
	//EVENT HANDLERS
	private OnCheckedChangeListener onCheckedChangeAM = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			//If AM gets selected, then PM must get unselected, also update alarm hour, since we are storing alarm hour in 24 hour format, if set to AM, then need to subtract 12
			mToggleBtnPM.setChecked(!isChecked);
			if(mAlarm.getHour() >= 12)
				mAlarm.setHour(mAlarm.getHour()-12);
		}
	};
	private OnCheckedChangeListener onCheckedChangePM = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			//If PM gets selected, then AM must get unselected, also update alarm hour, since we are storing alarm hour in 24 hour format, if set to PM, then need to add 12
			mToggleBtnAM.setChecked(!isChecked);
			if(mAlarm.getHour() < 12)
				mAlarm.setHour(mAlarm.getHour()+12);
		}
	};
	private OnClickListener onClickTime = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent setTimeIntent = new Intent(getApplicationContext(), SetTimeActivity.class);
			startActivityForResult(setTimeIntent, Constants.RequestCode.REQUEST_SET_TIME);
		}
	};

	private OnClickListener onClickEditVid = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent editVidIntent = new Intent(getApplicationContext(), SearchYoutubeActivity.class);
			startActivityForResult(editVidIntent, Constants.RequestCode.REQUEST_SEARCH_VIDEO);
		}
	};
	private OnClickListener onClickShowSettings = new OnClickListener() {
		@Override
		public void onClick(View v) {
			showSettings();
		}
	};
	private OnClickListener onClickShowVideo = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showVideo();
		}
	};
	private OnClickListener onClickSelectRingtone = new OnClickListener(){
		@Override
		public void onClick(View v){
			Intent systemRingtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			systemRingtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
			systemRingtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Back up ringtone");
			//Prepopulate with alarm's ringtone
			systemRingtoneIntent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mAlarm.getRingtoneUri());
			startActivityForResult(systemRingtoneIntent, Constants.RequestCode.REQUEST_SET_RINGTONE);
		}
	};
	private OnCheckedChangeListener onCheckedChangeWeekday = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			int dayIndex = (Integer)buttonView.getTag();
			boolean[] repeatDays = mAlarm.getRepeatDays(); //Get alarm's current repeat days, update it if necessary
			repeatDays[dayIndex] = isChecked;
			//Log.d(Constants.LOG_TAG, String.format("Day %d repeat is set to: %b", dayIndex, isChecked));
			mAlarm.setRepeatDays(repeatDays);
		}
	};

	//HELPERS
	private void initControls(){
		mTxtTime = (TypefacedTextView)findViewById(R.id.activity_edit_alarm_txt_time);
		mTxtVideoTitle = (TypefacedTextView)findViewById(R.id.activity_edit_alarm_txt_video_title);
		//TODO: cannot put text duration view on top of youtube view, it causes the player to throw exception
		mTxtDuration = (TextView)findViewById(R.id.activity_edit_alarm_txt_duration);
		mTxtDuration.setVisibility(TextView.GONE);
		
		mToggleBtnAM = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_am);
		mToggleBtnPM = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_pm);
		mToggleBtnMon = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_mon);
		mToggleBtnTue = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_tue);
		mToggleBtnWed = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_wed);
		mToggleBtnThu = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_thu);
		mToggleBtnFri = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_fri);
		mToggleBtnSat = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_sat);
		mToggleBtnSun = (ToggleButton)findViewById(R.id.activity_edit_alarm_togglebtn_sun);
		
		mBtnAlarmSettings = (Button)findViewById(R.id.activity_edit_alarm_btn_alarm_settings);
		mBtnAlarmSettingsOn = (Button)findViewById(R.id.activity_edit_alarm_btn_alarm_settings_on);
		mBtnEditVid = (Button)findViewById(R.id.activity_edit_alarm_btn_edit_vid);
		mBtnSelectRingtone = (Button)findViewById(R.id.activity_edit_alarm_btn_edit_ringtone);
		
		mLinlaySettings = (LinearLayout)findViewById(R.id.activity_edit_alarm_linlay_settings);
		mLinlayVideoSectionContainer = (LinearLayout)findViewById(R.id.activity_edit_alarm_linlay_video_container);
		
		youTubeView = (YouTubePlayerView)findViewById(R.id.activity_edit_alarm_youtube_player);
		youTubeView.initialize(Constants.YOUTUBE_API_KEY, this);
		
		//Set custom font
		mToggleBtnAM.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnPM.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnMon.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnTue.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnWed.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnThu.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnFri.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnSat.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		mToggleBtnSun.setTypeface(Utility.getTypeface(this, "ROBOTO-BOLDCONDENSED.TTF"));
		
		//Set day index for each toggle button so that our event handler knows which value to set
		mToggleBtnMon.setTag(0);
		mToggleBtnTue.setTag(1);
		mToggleBtnWed.setTag(2);
		mToggleBtnThu.setTag(3);
		mToggleBtnFri.setTag(4);
		mToggleBtnSat.setTag(5);
		mToggleBtnSun.setTag(6);
		
		//If time format is in 24-hour clock, then hide AM/PM
		if(mAlarm.getFormat() == Constants.TimeFormat.HOUR24.value()){
			mToggleBtnAM.setVisibility(View.GONE);
			mToggleBtnPM.setVisibility(View.GONE);
		}
		
		//Set styles
		mBtnSelectRingtone.setTypeface(Utility.getTypeface(getApplicationContext(), "ROBOTO-LIGHT.TTF"));
		
		//Event handlers
		mToggleBtnAM.setOnCheckedChangeListener(onCheckedChangeAM);
		mToggleBtnPM.setOnCheckedChangeListener(onCheckedChangePM);
		mTxtTime.setOnClickListener(onClickTime);
		mBtnEditVid.setOnClickListener(onClickEditVid);
		mBtnAlarmSettings.setOnClickListener(onClickShowSettings);
		mBtnAlarmSettingsOn.setOnClickListener(onClickShowVideo);
		mBtnSelectRingtone.setOnClickListener(onClickSelectRingtone);
		
		mToggleBtnMon.setOnCheckedChangeListener(onCheckedChangeWeekday);
		mToggleBtnTue.setOnCheckedChangeListener(onCheckedChangeWeekday);
		mToggleBtnWed.setOnCheckedChangeListener(onCheckedChangeWeekday);
		mToggleBtnThu.setOnCheckedChangeListener(onCheckedChangeWeekday);
		mToggleBtnFri.setOnCheckedChangeListener(onCheckedChangeWeekday);
		mToggleBtnSat.setOnCheckedChangeListener(onCheckedChangeWeekday);
		mToggleBtnSun.setOnCheckedChangeListener(onCheckedChangeWeekday);
	}
	
	private void showSettings(){
		mLinlaySettings.setVisibility(View.VISIBLE);
		youTubeView.setVisibility(View.GONE);
		
		//Start animation for the transition
		Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.card_slide_left_out);
		youTubeView.startAnimation(slideOutLeft);
		Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.card_slide_right_in);
		mLinlaySettings.startAnimation(slideInRight);
		
		mBtnAlarmSettings.setVisibility(View.GONE);
		mBtnAlarmSettingsOn.setVisibility(View.VISIBLE);
	}
	
	private void showVideo(){
		youTubeView.setVisibility(View.VISIBLE);
		mLinlaySettings.setVisibility(View.GONE);
		
		//Start animation for the transition
		Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.card_slide_left_in);
		youTubeView.startAnimation(slideInLeft);
		Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.card_slide_right_out);
		mLinlaySettings.startAnimation(slideOutRight);
		
		mBtnAlarmSettingsOn.setVisibility(View.GONE);
		mBtnAlarmSettings.setVisibility(View.VISIBLE);
	}
	
	private String getRingtoneName(Uri uri){
		if(uri != null){
			Ringtone alarmBackupRingtone = RingtoneManager.getRingtone(getApplicationContext(), mAlarm.getRingtoneUri());
			String ringtoneName = alarmBackupRingtone.getTitle(getApplicationContext());
			return ringtoneName;
		}
		return "Ringtone error :(";
	}
	
	//Prepopulate view controls
	//If NEW mode, then prepopulate AM/PM and a video
	//If EDIt mode, then prepopulate with the db Alarm object
	private void prepopulateFields(){
		mTxtTime.setText(mAlarm.toTimeString());
		if(mAlarm.getFormat() == Constants.TimeFormat.HOUR12.value()){
			if(mAlarm.getHour() >= 12){
				mToggleBtnPM.setChecked(true);
			}else{
				mToggleBtnAM.setChecked(true);
			}
		}
		
		//Prepopulate repeat days
		boolean[] repeatDays = mAlarm.getRepeatDays();
		mToggleBtnMon.setChecked(repeatDays[0]);
		mToggleBtnTue.setChecked(repeatDays[1]);
		mToggleBtnWed.setChecked(repeatDays[2]);
		mToggleBtnThu.setChecked(repeatDays[3]);
		mToggleBtnFri.setChecked(repeatDays[4]);
		mToggleBtnSat.setChecked(repeatDays[5]);
		mToggleBtnSun.setChecked(repeatDays[6]);
		
		
		//TODO: cannot put text duration view on top of youtube view, it causes the player to throw exception
		//mTxtDuration.setText(Utility.getTimeStringFromSeconds(mAlarm.getVideoDuration()));
		mTxtVideoTitle.setText(Utility.truncateString(mAlarm.getVideoTitle(), Constants.VideoTitleMaxLength));
		
		if(mAlarm.getRingtoneUri() == null){
			//This should only happen the first time, so for the older alarms, set to a default alarm
			Uri defaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
			mAlarm.setRingtoneUri(defaultRingtone);
		}
		
		mBtnSelectRingtone.setText(getRingtoneName(mAlarm.getRingtoneUri()));
		//Log.d(Constants.LOG_TAG, "Video title length: " + mAlarm.getVideoTitle().length());
	}
		
	//Call AlarmManager to turn on/off the alarm
	private void setAlarm(Alarm alarm, boolean isSetOn){
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
		
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		if(!alarm.isRepeat()){
			//Single alarm
			Intent singleAlarmIntent = Utility.prepareAlarmIntent(getApplicationContext(), alarm, SingleAlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), (int)alarm.getId(), singleAlarmIntent, 0);
			
			if(isSetOn){
				//Use alarm manager to set the alarm
				am.set(AlarmManager.RTC_WAKEUP, alarmTime, sender);
				
				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
				Log.d(Constants.LOG_TAG, "Alarm set for: " + dateFormatter.format(cal.getTime()));
				Toast.makeText(getApplicationContext(), "Alarm set for: " + dateFormatter.format(cal.getTime()), Toast.LENGTH_LONG).show();
			}else{
				//Use alarm manager to cancel the alarm
				am.cancel(sender);
				Log.d(Constants.LOG_TAG, "Alarm cancelled: " + alarm.getId());
			}
		}else{
			//Calculate when the first alarm of the repeating series needs to be triggered based on the current day and time
			//Update AlertActivity to handle repeating alarms, since we can no longer use 'setRepeating', we will have to
			//Manually check for when the next alarm of the series needs to be and then set it.		
			
			Calendar nextAlarmTime = Utility.getNextAlarmTime(alarm);
			long firstRepeatAlarmTime = nextAlarmTime.getTimeInMillis();
			Intent repeatAlarmIntent = Utility.prepareAlarmIntent(getApplicationContext(), alarm, RepeatAlarmReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), (int)alarm.getId(), repeatAlarmIntent, 0);
			//Check whether we are setting or canceling this alarm
			if(isSetOn){
				am.set(AlarmManager.RTC_WAKEUP, firstRepeatAlarmTime, sender);
				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
				Log.d(Constants.LOG_TAG, "Repeat Alarm set for: " + dateFormatter.format(cal.getTime()));
				Toast.makeText(getApplicationContext(), "Alarm set for: " + dateFormatter.format(cal.getTime()), Toast.LENGTH_LONG).show();
			}else{
				am.cancel(sender);
				Log.d(Constants.LOG_TAG, "Repeat Alarm cancelled: " + alarm.getId());
			}
				
		}
	}

	@Override
	public void onInitializationSuccess(Provider provider, YouTubePlayer player,
			boolean wasRestored) {
		if(!wasRestored){
			player.cueVideo(mAlarm.getVideoId());
			Log.d(Constants.LOG_TAG, "Video loaded: " + mAlarm.getVideoId());
			//Set settings layout height to be the same as youTubeView
			int youTubeHeight = youTubeView.getHeight();
			mLinlaySettings.getLayoutParams().height = youTubeHeight;
			mLinlayVideoSectionContainer.getLayoutParams().height = youTubeHeight;
		}
		
		player.setPlayerStyle(PlayerStyle.MINIMAL);
		youTubePlayer = player;
		player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
			@Override
			public void onVideoStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onVideoEnded() {
				
			}
			
			@Override
			public void onLoading() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLoaded(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(ErrorReason arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAdStarted() {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	protected Provider getYouTubePlayerProvider() {
		return (YouTubePlayerView)findViewById(R.id.activity_edit_alarm_youtube_player);
	}
}
