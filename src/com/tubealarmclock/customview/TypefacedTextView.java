package com.tubealarmclock.customview;

import com.tubealarmclock.mobile.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TypefacedTextView extends TextView{
	public TypefacedTextView(Context context, AttributeSet attrs){
		super(context, attrs);
		
		if(isInEditMode()){
			return;
		}
		
		TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefaceTextView);
		if(styledAttrs != null){
			String fontName = styledAttrs.getString(R.styleable.TypefaceTextView_typeface);
			if(fontName != null){
				Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
				setTypeface(typeface);				
			}
			
			styledAttrs.recycle();
		}
	}
}
