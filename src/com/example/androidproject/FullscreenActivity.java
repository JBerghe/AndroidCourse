package com.example.androidproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.example.androidproject.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.androidproject";
	private boolean toggleInfoScreen = true;
	FrameLayout fl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		//Hide info screen
		fl = (FrameLayout) findViewById(R.id.info_screen);
		fl.setVisibility(android.view.View.GONE);
	}
	public void infoButtonClick(View v){
		fl = (FrameLayout) findViewById(R.id.info_screen);
		//Toggle visibility of info screen
		if (toggleInfoScreen){
			fl.setVisibility(android.view.View.VISIBLE);
			toggleInfoScreen = false;
		}
		else{
			fl.setVisibility(android.view.View.GONE);
			toggleInfoScreen = true;
		}
	}
	public void playButtonClick(View v){
		//create new intent
		Intent intent = new Intent(this, GameActivity.class);
		String message = "test";
		intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
}
