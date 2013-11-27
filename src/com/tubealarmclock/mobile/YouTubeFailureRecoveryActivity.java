package com.tubealarmclock.mobile;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.tubealarmclock.code.Constants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.Toast;

public abstract class YouTubeFailureRecoveryActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{
	private static final int RECOVERY_DIALOG_REQUEST = 1;

	@Override
	public void onInitializationFailure(Provider provider,
			YouTubeInitializationResult errorReason) {
		//If the error is a recoverable error, then display the recovery dialog to allow user to fix error
		if(errorReason.isUserRecoverableError()){
			errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
		}else{
			String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		//If the request code is from our recovery dialog, then perform recovery action
		if(requestCode == RECOVERY_DIALOG_REQUEST){
			getYouTubePlayerProvider().initialize(Constants.YOUTUBE_API_KEY, this);
		}
	}

	protected abstract YouTubePlayer.Provider getYouTubePlayerProvider();
}
