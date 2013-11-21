package com.tubealarmclock.mobile;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.tubealarmclock.data.Alarm;
import com.tubealarmclock.data.AlarmsDataSource;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class SplashActivity extends Activity {
	AlarmsDataSource mAlarmsDatasource;
	CheckAlarmsTask mTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_splash);
		
		//TODO: check whether there is alarm, if so, then go to AlarmsActivity, else go to NoAlarmsActivity
		mAlarmsDatasource = new AlarmsDataSource(this);
		mAlarmsDatasource.open();
		/*
		Intent intent = new Intent(this, SearchYoutubeActivity.class);
		startActivity(intent);
		*/
		
		mTask = new CheckAlarmsTask();
		mTask.attach(this);
		mTask.execute();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mAlarmsDatasource.close();
	}
	
	private void onCompleteCheckAlarms(Boolean hasAlarms){
		if(!hasAlarms){
			Intent intent = new Intent(this, NoAlarmsActivity.class);
			startActivity(intent);
		}else{
			Intent intent = new Intent(this, AlarmsActivity.class);
			startActivity(intent);
		}
	}
	
	private class CheckAlarmsTask extends AsyncTask<Void, Integer, Boolean>{
		SplashActivity activity;
		
		@Override
		protected Boolean doInBackground(Void... params) {
			List<Alarm> alarms = activity.mAlarmsDatasource.getAllAlarms();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return !(alarms == null || alarms.size() == 0);
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			activity.onCompleteCheckAlarms(result);
		}
		
		void attach(SplashActivity activity){
			this.activity = activity;
		}
		
		void detach(){
			this.activity = null;
		}		
	}
}
