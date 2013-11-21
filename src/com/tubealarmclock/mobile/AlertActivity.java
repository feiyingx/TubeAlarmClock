package com.tubealarmclock.mobile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import com.tubealarmclock.code.Constants;
import com.tubealarmclock.code.Utility;
import com.tubealarmclock.data.Alarm;
import com.tubealarmclock.data.AlarmsDataSource;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class AlertActivity extends YouTubeFailureRecoveryActivity {
	Button mBtnSnooze, mBtnWake;
	YouTubePlayerView mYouTubePlayerView;
	long mAlarmId;
	YouTubePlayer mYouTubePlayer;
	AlarmsDataSource mDatasource;
	Alarm mAlarm;
	boolean mIsAutoSnooze = true;
	BroadcastReceiver mLockUnlockReceiver = null;
	boolean mDidPhoneWakeFromSleep = false;
	int mStopCounter = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(Constants.LOG_TAG, "AlertActivity onCreate");
		//Make activity full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		setContentView(R.layout.activity_alert);
		
		//Check whether screen is off, if so then we need to make use of WakeLock
		if(!AlarmWakeLock.isScreenOn(this)){
			AlarmWakeLock.acquireWakeLock(this);
			mDidPhoneWakeFromSleep = true;
		}
		
		mDatasource = new AlarmsDataSource(this);
		mDatasource.open();
		//Get alarmId and videoId
		Bundle intentBundle = getIntent().getExtras();
		mAlarmId = intentBundle.getLong(Constants.ExtraTag.AlarmId);
		//mAlarm = mDatasource.getAlarm(mAlarmId);
		GetAlarmTask getAlarmTask = new GetAlarmTask();
		getAlarmTask.attach(this);
		getAlarmTask.execute(mAlarmId);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		//Log.d(Constants.LOG_TAG, "AlertActivity onResume");
		if(mYouTubePlayer != null && mAlarm != null)
			mYouTubePlayer.loadVideo(mAlarm.getVideoId());
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		mStopCounter++;
		//In the case of when screen is already on, onPause doesn't get called until user clicks 'home' or navigates away
		//so we check for pauseCount > 0
		//In the case when screen is off, onPause gets called once on initial load
		//Used to autmoatically snooze alarm if user clicks 'home', 'power' buttons
		//Log.d(Constants.LOG_TAG, "AlertActivity onStop");
		
		if(mAlarm != null && mIsAutoSnooze){
			if(mDidPhoneWakeFromSleep){
				if(mStopCounter > 1){
					snoozeAlarm();
					finish();
				}
			}else{
				snoozeAlarm();
				finish();
			}
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		//Log.d(Constants.LOG_TAG, "AlertActivity onDestroy");
		AlarmWakeLock.releaseWakeLock();
		mDatasource.close();
	}

	@Override
	public void onInitializationSuccess(Provider provider, YouTubePlayer player,
			boolean wasRestored) {
		Log.d(Constants.LOG_TAG, "YouTube Player restored? " + wasRestored);
		//This is needed to ensure every alarm plays its own video, instead of resuming the video from a previously triggered alarm
		player.loadVideo(mAlarm.getVideoId());
		if(!wasRestored){
			
		}else{
			player.play();
		}
		player.setPlayerStyle(PlayerStyle.CHROMELESS);
		player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
			@Override
			public void onVideoStarted() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onVideoEnded() {
				mYouTubePlayer.seekToMillis(0); //Restart the video
				mYouTubePlayer.play();
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
		mYouTubePlayer = player;
	}

	@Override
	protected Provider getYouTubePlayerProvider() {
		return (YouTubePlayerView)findViewById(R.id.activity_alert_youtube_player);
	}
	
	private void onCompleteGetAlarm(Alarm alarm){
		mAlarm = alarm;
		Log.d(Constants.LOG_TAG, "ALARM LOAD COMPLETE: " + mAlarm.toDebugString());
		initControls();
	}
	
	//EVENT HANDLERS
	private OnClickListener onClickSnooze = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mIsAutoSnooze = false; //Since we're manually snoozing here, don't trigger another snooze thru the 'onPause' callback
			//TODO: Easter egg, if snooze more than 5 times, play a different video
			AlarmWakeLock.releaseWakeLock();
			snoozeAlarm();

			finish();
		}
	};
	private OnClickListener onClickWake = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mIsAutoSnooze = false; //Since we're manually stopping the alarm here, don't trigger another snooze thru the 'onPause' callback
			AlarmWakeLock.releaseWakeLock();
			//Once we click wake on the single alarm, then delete the single alarm
			if(!mAlarm.isRepeat()){
				//Delete the one time alarm from DB now that it has been triggered
				mDatasource.deleteAlarm(mAlarmId);
			}else{
				//Set the next alarm in the series
				Calendar nextAlarm = Utility.getNextAlarmTime(mAlarm);
				AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
				
				Intent repeatAlarmIntent = Utility.prepareAlarmIntent(getApplicationContext(), mAlarm, RepeatAlarmReceiver.class);
				PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), (int)mAlarm.getId(), repeatAlarmIntent, 0);
				
				//Use alarm manager to set the alarm
				am.set(AlarmManager.RTC_WAKEUP, nextAlarm.getTimeInMillis(), sender);
				
				SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Log.d(Constants.LOG_TAG, "Next Repeat Alarm set for: " + dateFormatter.format(nextAlarm.getTime()));
			}
			
			//Remove snooze notification once they wake up
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// using alarmId to link to the notification allows you to update the notification later on.
			notificationManager.cancel((int) mAlarm.getId());
			
			finish();
		}
	};
	
	//HELPERS
	private void initControls(){
		mBtnSnooze = (Button)findViewById(R.id.activity_alert_btn_snooze);
		mBtnWake = (Button)findViewById(R.id.activity_alert_btn_wake);
		mYouTubePlayerView = (YouTubePlayerView)findViewById(R.id.activity_alert_youtube_player);
		mYouTubePlayerView.initialize(Constants.YOUTUBE_API_KEY, this);
		
		//Set font style
		mBtnSnooze.setTypeface(Utility.getTypeface(this, "ROBOTO-LIGHT.TTF"));
		
		//Bind event handlers
		mBtnSnooze.setOnClickListener(onClickSnooze);
		mBtnWake.setOnClickListener(onClickWake);
	}
	
	private void snoozeAlarm(){
		//Schedule alarm to be triggered 
		//Determine the alarm time
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		long alarmTime = cal.getTimeInMillis();
		
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		//Set a single snooze alarm
		Intent snoozeAlarmIntent = Utility.prepareAlarmIntent(getApplicationContext(), mAlarm, SingleAlarmReceiver.class);
		snoozeAlarmIntent.setAction(Constants.IntentAction.INTENT_ACTION_SNOOZE_ALARM);
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), (int)mAlarm.getId(), snoozeAlarmIntent, 0);
		
		//Use alarm manager to set the alarm
		am.set(AlarmManager.RTC_WAKEUP, alarmTime, sender);
		
		//Set a notification action to dismiss the snooze alarm, which will cancel it
		buildNotification(cal);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Log.d(Constants.LOG_TAG, "SNOOZE Alarm set for: " + dateFormatter.format(cal.getTime()));
		
	}
	
	private void buildNotification(Calendar snoozeTime){
		Intent cancelSnoozeIntent = new Intent(this, CancelSnoozeActivity.class);
		cancelSnoozeIntent.putExtra(Constants.ExtraTag.AlarmId, mAlarm.getId()); //Pass alarm id to the CancelSnoozeActivity so we can construct the correct snooze PendingIntent and cancel it
		PendingIntent cancelSnoozePendingIntent = PendingIntent.getActivity(this, (int) mAlarm.getId(), cancelSnoozeIntent, 0);
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mma");
		String timeString = dateFormatter.format(snoozeTime.getTime());
		String notificationText = getString(R.string.snooze_notification_text).replace("(TIME)", timeString.toLowerCase());
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.icon_time)
		.setContentTitle(getString(R.string.app_name))
		.setContentText(notificationText)
		.addAction(R.drawable.icon_cancel, getString(R.string.cancel_snooze), cancelSnoozePendingIntent);
		
		Intent resultIntent = new Intent(this, AlarmsActivity.class);
		
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(AlarmsActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		builder.setContentIntent(resultPendingIntent);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// using alarmId to link to the notification allows you to update the notification later on.
		notificationManager.notify((int) mAlarm.getId(), builder.build());
	}
	
	private class GetAlarmTask extends AsyncTask<Long, Integer, Alarm>{
		AlertActivity activity;
		
		@Override
		protected Alarm doInBackground(Long... params) {
			Alarm alarm = activity.mDatasource.getAlarm(params[0]);
			return alarm;
		}
		
		@Override
		protected void onPostExecute(Alarm result){
			activity.onCompleteGetAlarm(result);
		}
		
		void attach(AlertActivity activity){
			this.activity = activity;
		}
		
		void detach(){
			this.activity = null;
		}		
	}
}
