package com.tubealarmclock.mobile;

import com.tubealarmclock.code.Constants;
import com.tubealarmclock.code.Utility;
import com.tubealarmclock.customview.TypefacedTextView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class NoAlarmsActivity extends Activity {
	Button mBtnAddAlarm, mBtnPlus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_no_alarms);
		
		initControls();
	}
	
	//LISTENERS
	private OnClickListener onClickAddAlarm = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//When user clicks on "add alarm", take user to the NewAlarm activity
			Intent newAlarm = new Intent(getApplicationContext(), EditAlarmActivity.class);
			newAlarm.putExtra(Constants.ExtraTag.AlarmId, 0);
			startActivity(newAlarm);
		}
	};

	//HELPER FUNCTIONS
	private void initControls(){
		mBtnPlus = (Button)findViewById(R.id.activity_alarms_btn_plus);
		mBtnAddAlarm = (Button)findViewById(R.id.activity_alarms_btn_add_alarm);
		
		//Set button font
		mBtnAddAlarm.setTypeface(Utility.getTypeface(this, "ROBOTO-THIN.TTF"));
		mBtnPlus.setTypeface(Utility.getTypeface(this, "ROBOTO-THIN.TTF"));
		
		//Start animation for the plus text
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_around_center);
		mBtnPlus.startAnimation(animation);
		
		//Set listeners
		mBtnAddAlarm.setOnClickListener(onClickAddAlarm);
		mBtnPlus.setOnClickListener(onClickAddAlarm);
	}
}
