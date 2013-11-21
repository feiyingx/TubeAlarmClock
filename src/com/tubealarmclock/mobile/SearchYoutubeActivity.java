package com.tubealarmclock.mobile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.android.youtube.player.YouTubeThumbnailLoader.ErrorReason;
import com.google.api.services.youtube.model.SearchResult;
import com.tubealarmclock.code.Constants;
import com.tubealarmclock.code.Utility;
import com.tubealarmclock.customview.TypefacedTextView;
import com.tubealarmclock.data.YouTubeSearchService;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchYoutubeActivity extends Activity {
	EditText mEditQuery;
	LinearLayout mLinlayLoader;
	ListView mLvResults;
	TypefacedTextView txtLoaderSymbol;
	SearchYouTubeTask mSearchTask;
	SearchResultAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Hide Dialog title bar before the layout gets set to avoid the flicker
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_youtube);
		
		initControls();
	}
	
	//TODO: handle config changes for async task
	
	//EVENT HANDLERS
	private OnEditorActionListener onEditorActionEditQuery = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(actionId == EditorInfo.IME_ACTION_SEARCH){
				showLoading();
				//start async task to search youtube
				String query = mEditQuery.getText().toString();
				
				mSearchTask = new SearchYouTubeTask();
				mSearchTask.attach(SearchYoutubeActivity.this);
				mSearchTask.execute(query);
				
				//Hide the search keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mEditQuery.getWindowToken(), 0);
				return true;
			}
			
			return false;
		}
	};
	
	private void onCompleteSearchYouTube(List<SearchResult> results){
		Log.d(Constants.LOG_TAG, "Num YouTube results: " + (results == null ? 0 : results.size()));
		hideLoading();
		mAdapter = new SearchResultAdapter(getApplicationContext(), results);
		mLvResults.setAdapter(mAdapter);
	}
	
	//CLASSES
	private class SearchYouTubeTask extends AsyncTask<String, Integer, List<SearchResult>>{
		SearchYoutubeActivity activity = null;

		@Override
		protected List<SearchResult> doInBackground(String... params) {
			String searchQuery = params[0];
			return YouTubeSearchService.Search(searchQuery);
		}
		
		@Override
		protected void onPostExecute(List<SearchResult> result){
			activity.onCompleteSearchYouTube(result);
		}
		
		void attach(SearchYoutubeActivity activity){
			this.activity = activity;
		}
		
		void detach(){
			this.activity = null;
		}		
	}
	
	static class SearchResultViewHolder{
		YouTubeThumbnailView thumbnailView;
		TypefacedTextView txtTitle;
		RelativeLayout rellayContainer;
	}
	
	public class SearchResultAdapter extends BaseAdapter{
		Context mContext;
		List<SearchResult> mEntries;
		Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;
		ThumbnailListener thumbnailListener;
		
		public SearchResultAdapter(Context c, List<SearchResult> entries){
			mContext = c;
			mEntries = entries;
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
			if(mEntries == null)
				return 0;
			return mEntries.size();
		}

		@Override
		public Object getItem(int position) {
			if(mEntries == null)
				return null;
			return mEntries.get(position);
		}

		@Override
		public long getItemId(int position) {
			//this is not used
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SearchResult entry = (SearchResult)getItem(position);
			SearchResultViewHolder holder;
			//Make sure entry isn't null before we do anything
			if(entry != null){
				//Check whether there is a reuseable convertView already, if not then inflate a new one and attach a viewholder to it so we can reuse it later
				if(convertView == null){
					LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.search_result_list_item, null);
					YouTubeThumbnailView thumbnailView = (YouTubeThumbnailView)convertView.findViewById(R.id.search_result_list_item_youtube_thumb);
					TypefacedTextView txtTitle = (TypefacedTextView)convertView.findViewById(R.id.search_result_list_item_txt_video_title);
					RelativeLayout rellayContainer = (RelativeLayout)convertView.findViewById(R.id.search_result_list_item_rellay_container);

					thumbnailView.setTag((String)entry.getId().get("videoId"));
					thumbnailView.initialize(Constants.YOUTUBE_API_KEY, thumbnailListener);
					
					holder = new SearchResultViewHolder();
					holder.thumbnailView = thumbnailView;
					holder.txtTitle = txtTitle;
					holder.rellayContainer = rellayContainer;
					
					holder.rellayContainer.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String[] vidData = (String[])v.getTag();
							Intent returnData = new Intent();
							returnData.putExtra(Constants.ExtraTag.VideoId, vidData[0]);
							returnData.putExtra(Constants.ExtraTag.VideoTitle, vidData[1]);
							setResult(Constants.ResultCode.REQUEST_OK, returnData);
							finish();
						}
					});
				}else{
					holder = (SearchResultViewHolder)convertView.getTag();
					YouTubeThumbnailLoader loader = thumbnailViewToLoaderMap.get(holder.thumbnailView);
					if(loader == null){
						//This means the view is already created and is being initialized. Store the video id in the view
						
						holder.thumbnailView.setTag((String)entry.getId().get("videoId"));
					}else{
						//This means the view is already initialized. Just set the right video id
						
						loader.setVideo((String)entry.getId().get("videoId"));
					}
				}
				
				//Update viewholder with current position's video data
				
				holder.txtTitle.setText(Utility.truncateString((String)entry.getSnippet().get("title"), Constants.VideoTitleMaxLength));
				String[] vidData = new String[2];
				vidData[0] = (String)entry.getId().get("videoId");
				vidData[1] = (String)entry.getSnippet().get("title");
				holder.rellayContainer.setTag(vidData);
				
				convertView.setTag(holder);
			}
			return convertView;
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
	
	

	//HELPERS
	private void initControls(){
		mEditQuery = (EditText)findViewById(R.id.activity_search_youtube_edit_query);
		mLinlayLoader = (LinearLayout)findViewById(R.id.activity_search_youtube_linlay_loading_results);
		mLvResults = (ListView)findViewById(R.id.activity_search_youtube_lv_results);
		txtLoaderSymbol = (TypefacedTextView)findViewById(R.id.activity_search_youtube_txt_loading);
		
		//Bind event handlers
		mEditQuery.setOnEditorActionListener(onEditorActionEditQuery);
	}
	
	private void showLoading(){
		mLvResults.setVisibility(View.GONE);
		mLinlayLoader.setVisibility(View.VISIBLE);
		//Start animation for the plus text
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_around_center);
		txtLoaderSymbol.startAnimation(animation);
	}
	
	private void hideLoading(){
		mLinlayLoader.setVisibility(View.GONE);
		mLvResults.setVisibility(View.VISIBLE);
	}
}
