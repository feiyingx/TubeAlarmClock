package com.tubealarmclock.mobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailLoader.ErrorReason;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.tubealarmclock.code.Constants;
import com.tubealarmclock.code.Utility;
import com.tubealarmclock.customview.TypefacedTextView;
import com.tubealarmclock.data.Alarm;
import com.tubealarmclock.data.AlarmsDataSource;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmsActivity extends Activity {
	ListView mLvAlarms;
	AlarmListAdapter mAlarmAdapter;
	AlarmsDataSource mAlarmDatasource;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Transparent action bar, do this before the setContentView, else it will throw exception
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#50010509")));
		
		setContentView(R.layout.activity_alarms);
		
		mAlarmDatasource = new AlarmsDataSource(this);
		mAlarmDatasource.open();
		initControls();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarms, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		int menuItemId = item.getItemId();
		switch(menuItemId){
			case R.id.action_add_alarm:
				Intent newAlarm = new Intent(this, EditAlarmActivity.class);
				newAlarm.putExtra(Constants.ExtraTag.AlarmId, 0); //Pass in 0 for alarm id to let the EditAlarmActivity know that it is a creating a new alarm not editing an existing
				startActivity(newAlarm);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		loadAlarms();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mAlarmAdapter.releaseLoaders();
		mAlarmDatasource.close();
	}

	//HELPERS
	private void initControls(){
		mLvAlarms = (ListView)findViewById(R.id.activity_alarms_lv_alarms);
	}
	
	private void loadAlarms(){
		List<Alarm> alarms = mAlarmDatasource.getAllAlarms();
		/*
		List<Alarm> dummyAlarms = new ArrayList<Alarm>();
		Alarm alarm1 = createTestAlarm(1,7, 15);
		Alarm alarm2 = createTestAlarm(2,8, 30);
		Alarm alarm3 = createTestAlarm(3,16, 10);
		Alarm alarm4 = createTestAlarm(4,17, 00);
		Alarm alarm5 = createTestAlarm(5,20, 05);
		dummyAlarms.add(alarm1);
		dummyAlarms.add(alarm2);
		dummyAlarms.add(alarm3);
		dummyAlarms.add(alarm4);
		dummyAlarms.add(alarm5);
		*/
		
		//Check whether there are any alarms, if not, then show the NoAlarmsActivity
		if(alarms == null || alarms.size() == 0){
			Intent noAlarm = new Intent(getApplicationContext(), NoAlarmsActivity.class);
			startActivity(noAlarm);
		}else{
			mAlarmAdapter = new AlarmListAdapter(this, alarms);
			mLvAlarms.setAdapter(mAlarmAdapter);
		}
	}
	
	private Alarm createTestAlarm(int id, int hour, int minute){
		Alarm alarm = new Alarm();
		alarm.setId(id);
		alarm.setHour(hour);
		alarm.setMinute(minute);
		alarm.setFormat(Constants.TimeFormat.HOUR12.value());
		alarm.setRepeatDays(new boolean[]{false, false, false, false, false, false, false});
		alarm.setVideoDuration(Constants.DefaultVideoDuration);
		alarm.setVideoTitle(Constants.DefaultVideoTitle);
		alarm.setVideoId(Constants.DefaultVideoId);
		return alarm;
	}
	
	//ADAPTER CLASSES
	static class AlarmListViewHolder{
		TypefacedTextView txtTime, txtAMPM, txtVideoTitle,
			txtMon, txtTue, txtWed, txtThu, txtFri, txtSat, txtSun;
		TextView txtVideoDuration;
		Switch switchAlarm;
		YouTubeThumbnailView youTubeThumbnailView;
		RelativeLayout rellayContainer;
	}
	
	public class AlarmListAdapter extends BaseAdapter{
		List<Alarm> mAlarms;
		Context mContext;
		Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;
		ThumbnailListener thumbnailListener;
		
		public AlarmListAdapter(Context c, List<Alarm> alarms){
			mContext = c;
			mAlarms = alarms;
			thumbnailViewToLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
			thumbnailListener = new ThumbnailListener();
		}
		
		public void releaseLoaders() {
	      for (YouTubeThumbnailLoader loader : thumbnailViewToLoaderMap.values()) {
	        loader.release();
	      }
	    }
		
		@Override
		public int getCount() {
			if(mAlarms == null)
				return 0;
			return mAlarms.size();
		}

		@Override
		public Object getItem(int index) {
			if(mAlarms == null)
				return null;
			return mAlarms.get(index);
		}

		@Override
		public long getItemId(int index) {
			if(mAlarms == null)
				return 0;
			return mAlarms.get(index).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//Get the appropriate alarm object
			Alarm alarm = (Alarm)getItem(position);
			//Log.d(Constants.LOG_TAG, "Generating alarm for @ position: " + position + "||" + alarm.toDebugString());
			AlarmListViewHolder viewHolder;
			if(alarm != null){
				//Check whether we're passed in a re-usable view. If not, then we will inflate a brand new layout
				if(convertView == null){
					LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.alarm_list_item, null);
					RelativeLayout rellayContainer = (RelativeLayout)convertView.findViewById(R.id.alarm_list_item_rellay_container);
					TypefacedTextView txtTime = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_time);
					TypefacedTextView txtAMPM = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_ampm);
					TypefacedTextView txtVideoTitle = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_video_title);
					TextView txtVideoDuration = (TextView)convertView.findViewById(R.id.alarm_list_item_txt_duration);
					TypefacedTextView txtMon = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_mon);
					TypefacedTextView txtTue = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_tue);
					TypefacedTextView txtWed = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_wed);
					TypefacedTextView txtThu = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_thu);
					TypefacedTextView txtFri = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_fri);
					TypefacedTextView txtSat = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_sat);
					TypefacedTextView txtSun = (TypefacedTextView)convertView.findViewById(R.id.alarm_list_item_txt_sun);  
					Switch switchAlarm = (Switch)convertView.findViewById(R.id.alarm_list_item_switch_alarm);
					YouTubeThumbnailView youTubeThumbnailView = (YouTubeThumbnailView)convertView.findViewById(R.id.alarm_list_item_youtube_thumb);
					youTubeThumbnailView.setTag(alarm.getVideoId()); //Attach video id to the thumbnail view so we can retrieve it later. It will be used by the view during its initialization phase
					youTubeThumbnailView.initialize(Constants.YOUTUBE_API_KEY, thumbnailListener);
					
					//Since this is a newly created view, create a view holder and attach it to this view
					viewHolder = new AlarmListViewHolder();
					viewHolder.txtTime = txtTime;
					viewHolder.txtAMPM = txtAMPM;
					viewHolder.txtVideoTitle = txtVideoTitle;
					viewHolder.txtVideoDuration = txtVideoDuration;
					viewHolder.txtMon = txtMon;
					viewHolder.txtTue = txtTue;
					viewHolder.txtWed = txtWed;
					viewHolder.txtThu = txtThu;
					viewHolder.txtFri = txtFri;
					viewHolder.txtSat = txtSat;
					viewHolder.txtSun = txtSun;
					viewHolder.switchAlarm = switchAlarm;
					viewHolder.youTubeThumbnailView = youTubeThumbnailView;
					viewHolder.rellayContainer = rellayContainer;
					
					viewHolder.switchAlarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							//Update the alarm object, also update the alarm object in DB
							int position = (Integer)buttonView.getTag();
							Alarm alarm = (Alarm)getItem(position);
							alarm.setIsOn(isChecked);
							//Log.d(Constants.LOG_TAG, String.format("Updating alarm %d ON status to: %b", position, isChecked));
							//Log.d(Constants.LOG_TAG, String.format("Updated alarm %d is now: %s", position, alarm.toDebugString()));
							mAlarmDatasource.updateAlarm(alarm);
							setAlarm(alarm, isChecked); //Will turn alarm on/off in AlarmManager
						}
					});
					viewHolder.rellayContainer.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							//Get the position from the view tag and then retrieve the object
							long alarmId = (Long)v.getTag();
							Intent editAlarm = new Intent(getApplicationContext(), EditAlarmActivity.class);
							editAlarm.putExtra(Constants.ExtraTag.AlarmId, alarmId);
							startActivity(editAlarm);
						}
					});
				}else{
					viewHolder = (AlarmListViewHolder)convertView.getTag();
					YouTubeThumbnailLoader loader = thumbnailViewToLoaderMap.get(viewHolder.youTubeThumbnailView);
					if(loader == null){
						//This means the view is already created and is currently being initialized. Store the video Id in the tag
						viewHolder.youTubeThumbnailView.setTag(alarm.getVideoId());
					}else{
						//This means view is created and already initialized. Just set the right video id on the loader
						//viewHolder.youTubeThumbnailView.setImageResource(R.drawable.youtube_loading);
						loader.setVideo(alarm.getVideoId());
					}
				}
				//Update the view controls with the alarm's info
				viewHolder.txtTime.setText(alarm.toTimeString());
				if(alarm.getFormat() == Constants.TimeFormat.HOUR12.value()){
					viewHolder.txtAMPM.setVisibility(View.VISIBLE);
					String ampmText = alarm.getHour() >= 12 ? "PM" : "AM";
					viewHolder.txtAMPM.setText(ampmText);
				}
				else{
					viewHolder.txtAMPM.setVisibility(View.GONE);
				}
				viewHolder.txtVideoTitle.setText(Utility.truncateString(alarm.getVideoTitle(), Constants.VideoTitleMaxLength));
				viewHolder.txtVideoDuration.setText(Utility.getTimeStringFromSeconds(alarm.getVideoDuration()));
				boolean[] repeatDays = alarm.getRepeatDays();
				
				int visibilityMon = repeatDays[0] ? View.VISIBLE : View.GONE;
				int visibilityTue = repeatDays[1] ? View.VISIBLE : View.GONE;
				int visibilityWed = repeatDays[2] ? View.VISIBLE : View.GONE;
				int visibilityThu = repeatDays[3] ? View.VISIBLE : View.GONE;
				int visibilityFri = repeatDays[4] ? View.VISIBLE : View.GONE;
				int visibilitySat = repeatDays[5] ? View.VISIBLE : View.GONE;
				int visibilitySun = repeatDays[6] ? View.VISIBLE : View.GONE;
				viewHolder.txtMon.setVisibility(visibilityMon);
				viewHolder.txtTue.setVisibility(visibilityTue);
				viewHolder.txtWed.setVisibility(visibilityWed);
				viewHolder.txtThu.setVisibility(visibilityThu);
				viewHolder.txtFri.setVisibility(visibilityFri);
				viewHolder.txtSat.setVisibility(visibilitySat);
				viewHolder.txtSun.setVisibility(visibilitySun);
				
				//Set the alarm on/off switch state and handler
				viewHolder.switchAlarm.setTag(position); //Pass in the index
				viewHolder.switchAlarm.setChecked(alarm.isOn());
				viewHolder.rellayContainer.setTag(alarm.getId());
				
				convertView.setTag(viewHolder);
			}
			
			return convertView;
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
					
					SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Log.d(Constants.LOG_TAG, "Alarm set for: " + dateFormatter.format(cal.getTime()));
				}else{
					//Use alarm manager to cancel the alarm
					am.cancel(sender);
					Log.d(Constants.LOG_TAG, "Alarm cancelled: " + alarm.getId());
				}
			}else{
				//If this is a repeat alarm, then
				//Calculate when the first alarm of the repeating series needs to be triggered based on the current day and time
				//Update AlertActivity to handle repeating alarms, since we can no longer use 'setRepeating', we will have to
				//Manually check for when the next alarm of the series needs to be and then set it.	
				Calendar nextAlarmTime = Utility.getNextAlarmTime(alarm);
				Intent repeatAlarmIntent = Utility.prepareAlarmIntent(getApplicationContext(), alarm, RepeatAlarmReceiver.class);
				PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), (int)alarm.getId(), repeatAlarmIntent, 0);
				//Check whether we are setting or canceling this alarm
				if(isSetOn){
					am.set(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), sender);
					SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Log.d(Constants.LOG_TAG, "Repeat Alarm set for: " + dateFormatter.format(nextAlarmTime.getTime()));
				}else{
					am.cancel(sender);
					Log.d(Constants.LOG_TAG, "Repeat Alarm cancelled: " + alarm.getId());
				}
			}
		}
		
		private final class ThumbnailListener implements YouTubeThumbnailView.OnInitializedListener, YouTubeThumbnailLoader.OnThumbnailLoadedListener{

			@Override
			public void onThumbnailError(YouTubeThumbnailView view,
					ErrorReason errorReason) {
				//view.setImageResource(R.drawable.youtube_error);				
			}

			@Override
			public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
				
			}

			@Override
			public void onInitializationFailure(YouTubeThumbnailView view,
					YouTubeInitializationResult loader) {
				//view.setImageResource(R.drawable.youtube_error);
			}

			@Override
			public void onInitializationSuccess(YouTubeThumbnailView view,
					YouTubeThumbnailLoader loader) {
				loader.setOnThumbnailLoadedListener(this);
				thumbnailViewToLoaderMap.put(view, loader);
				//view.setImageResource(R.drawable.youtube_loading);
				String videoId = (String)view.getTag();
				loader.setVideo(videoId);
			}
			
		}
		
	}
}
