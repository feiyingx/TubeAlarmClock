package com.tubealarmclock.mobile;

import com.tubealarmclock.code.Constants;
import com.tubealarmclock.customview.TypefacedTextView;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

public class SetTimeActivity extends Activity {
	Button mBtnNum1,mBtnNum2,mBtnNum3,mBtnNum4,mBtnNum5,mBtnNum6,mBtnNum7,mBtnNum8,mBtnNum9,mBtnNum10,mBtnNum11,mBtnNum12,mBtnNum0;
	Button mBtnSet, mBtnCancel;
	ImageButton mImgBtnDelete;
	TypefacedTextView mTxtHour, mTxtMin1, mTxtMin2;
	private int mTimeDigitIndex = 0; //This will be used to track which time digit we are working on, in order to validate and update ui
	private int[] mTimeDigits = new int[3]; //Keep track of the time digits
	private boolean mIs24HourFormat = false; //Default to 12 hour format
	private TypefacedTextView[] mDigitDisplay = new TypefacedTextView[4];
	private Button[] mNumButtons = new Button[13];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Hide Dialog title bar before the layout gets set to avoid the flicker
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set_time);
		
		initControls();
		setValidButtons();
	}
	
	//EVENT HANDLERS
	private OnClickListener onClickNumButton = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//Get the number of the button being clicked
			if(mTimeDigitIndex < 3){
				int num = (Integer)v.getTag();
				mTimeDigits[mTimeDigitIndex] = num;
				mDigitDisplay[mTimeDigitIndex].setText(Integer.toString(num));
				
				mTimeDigitIndex++; //Move the digit index to the next digit
				setValidButtons();
			}
		}
	};
	private OnClickListener onClickBackspace = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mTimeDigitIndex > 0){
				mTimeDigitIndex--;
				setValidButtons();
				mTimeDigits[mTimeDigitIndex]=0;
				mDigitDisplay[mTimeDigitIndex].setText("-");
			}
		}
	};
	private OnClickListener onClickSetTime = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//Get the selected hour in 24 hour format, always, because we want to store all hours in 24 hour format, just display them differently
			//Since we are currently only doing inputs in 12-hour format, then this number will always be 12 or lower
			int hour = mTimeDigits[0];
			int min = mTimeDigits[1]*10+mTimeDigits[2];
			Intent returnData = new Intent();
			returnData.putExtra(Constants.ExtraTag.Hour, hour);
			returnData.putExtra(Constants.ExtraTag.Minute, min);
			setResult(Constants.ResultCode.REQUEST_OK, returnData);
			finish();
		}
	};
	private OnClickListener onClickCancel = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	//HELPERS
	//Find all the views and bind event handlers if any
	private void initControls(){
		mBtnNum0 = (Button)findViewById(R.id.activity_set_time_btn_num0);
		mBtnNum1 = (Button)findViewById(R.id.activity_set_time_btn_num1);
		mBtnNum2 = (Button)findViewById(R.id.activity_set_time_btn_num2);
		mBtnNum3 = (Button)findViewById(R.id.activity_set_time_btn_num3);
		mBtnNum4 = (Button)findViewById(R.id.activity_set_time_btn_num4);
		mBtnNum5 = (Button)findViewById(R.id.activity_set_time_btn_num5);
		mBtnNum6 = (Button)findViewById(R.id.activity_set_time_btn_num6);
		mBtnNum7 = (Button)findViewById(R.id.activity_set_time_btn_num7);
		mBtnNum8 = (Button)findViewById(R.id.activity_set_time_btn_num8);
		mBtnNum9 = (Button)findViewById(R.id.activity_set_time_btn_num9);
		mBtnNum10 = (Button)findViewById(R.id.activity_set_time_btn_num10);
		mBtnNum11 = (Button)findViewById(R.id.activity_set_time_btn_num11);
		mBtnNum12 = (Button)findViewById(R.id.activity_set_time_btn_num12);
		mBtnSet = (Button)findViewById(R.id.activity_set_time_btn_set);
		mBtnCancel = (Button)findViewById(R.id.activity_set_time_btn_cancel);
		mImgBtnDelete = (ImageButton)findViewById(R.id.activity_set_time_imgbtn_delete);
		
		mTxtHour = (TypefacedTextView)findViewById(R.id.activity_set_time_txt_hour);
		mTxtMin1 = (TypefacedTextView)findViewById(R.id.activity_set_time_txt_min1);
		mTxtMin2 = (TypefacedTextView)findViewById(R.id.activity_set_time_txt_min2);
		
		//Bind events
		mBtnNum0.setOnClickListener(onClickNumButton);
		mBtnNum1.setOnClickListener(onClickNumButton);
		mBtnNum2.setOnClickListener(onClickNumButton);
		mBtnNum3.setOnClickListener(onClickNumButton);
		mBtnNum4.setOnClickListener(onClickNumButton);
		mBtnNum5.setOnClickListener(onClickNumButton);
		mBtnNum6.setOnClickListener(onClickNumButton);
		mBtnNum7.setOnClickListener(onClickNumButton);
		mBtnNum8.setOnClickListener(onClickNumButton);
		mBtnNum9.setOnClickListener(onClickNumButton);
		mBtnNum10.setOnClickListener(onClickNumButton);
		mBtnNum11.setOnClickListener(onClickNumButton);
		mBtnNum12.setOnClickListener(onClickNumButton);
		mImgBtnDelete.setOnClickListener(onClickBackspace);
		mBtnSet.setOnClickListener(onClickSetTime);
		mBtnCancel.setOnClickListener(onClickCancel);
		
		//Set each button's value to itself
		mBtnNum0.setTag(0);
		mBtnNum1.setTag(1);
		mBtnNum2.setTag(2);
		mBtnNum3.setTag(3);
		mBtnNum4.setTag(4);
		mBtnNum5.setTag(5);
		mBtnNum6.setTag(6);
		mBtnNum7.setTag(7);
		mBtnNum8.setTag(8);
		mBtnNum9.setTag(9);
		mBtnNum10.setTag(10);
		mBtnNum11.setTag(11);
		mBtnNum12.setTag(12);
		
		//Set the time display text views to our array so we can easily access them for updating
		mDigitDisplay[0] = mTxtHour;
		mDigitDisplay[1] = mTxtMin1;
		mDigitDisplay[2] = mTxtMin2;
		//Set the button views to our array so we can easily access them
		mNumButtons[0] = mBtnNum0;
		mNumButtons[1] = mBtnNum1;
		mNumButtons[2] = mBtnNum2;
		mNumButtons[3] = mBtnNum3;
		mNumButtons[4] = mBtnNum4;
		mNumButtons[5] = mBtnNum5;
		mNumButtons[6] = mBtnNum6;
		mNumButtons[7] = mBtnNum7;
		mNumButtons[8] = mBtnNum8;
		mNumButtons[9] = mBtnNum9;
		mNumButtons[10] = mBtnNum10;
		mNumButtons[11] = mBtnNum11;
		mNumButtons[12] = mBtnNum12;
	}
	

	//Will set the valid buttons for the current digit, for example
	//If we are starting with --:--, in 12 hour format,
	//First digit can be 1,2,3,...,11,12
	//If selected a single digit number (e.g. 5), then display it as (05:--)
	//If 08:--, third digit can be 0-5
	//If 08:0-, then last digit can be 0-9
	private void setValidButtons(){
		//First check which digit we are working on, and then go from there
		switch (mTimeDigitIndex) {
		case 0: //First hour digit
			//Allow all numbers except 0
			if(!mIs24HourFormat){
				for(int i = 0; i < 13; i++){
					boolean isEnabled = i != 0;
					mNumButtons[i].setEnabled(isEnabled);
				}
			}
			
			//Do not enable the 'Set' button unless all digits are completed
			mBtnSet.setEnabled(false);
			break;
		case 1: //First minute digit
			if(!mIs24HourFormat){
				//First minute digit can only be from 0-5
				for(int i = 0; i < 13; i++){
					boolean isEnabled = (i == 0 || i == 1 || i == 2 || i == 3 || i == 4 || i == 5);
					mNumButtons[i].setEnabled(isEnabled);
				}
			}
			
			//Do not enable the 'Set' button unless all digits are completed
			mBtnSet.setEnabled(false);
			break;
		case 2: //Second minute digit
			//Last digit can be any number, 0-9
			for(int i = 0; i < 13; i++){
				boolean isEnabled = (i < 10);
				mNumButtons[i].setEnabled(isEnabled);
			}
			
			//Do not enable the 'Set' button unless all digits are completed
			mBtnSet.setEnabled(false);
			break;
		case 3: //This means all the digits have been filled, so enable the 'Set' button
			mBtnSet.setEnabled(true);
			break;
		default:
			break;
		}
	}
}
