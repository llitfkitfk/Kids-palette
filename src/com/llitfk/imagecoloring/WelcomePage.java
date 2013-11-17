package com.llitfk.imagecoloring;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class WelcomePage extends Activity {

	
	private static final String MAIN_ACTIVITY = "com.llitfk.imagecoloring.MAINCOLORINGBOARD";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		Thread timer = new Thread() {
			@Override
			public void run() {
				try {
					sleep(1000);
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Intent startMainActivity = new Intent(WelcomePage.this, MainColoringBoard.class); 
					int i = R.drawable.test;
					startMainActivity.putExtra(MainColoringBoard.INTENT_PIC_ID, i);
					startActivity(startMainActivity);
				}
			}
		};
		
		timer.start();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		mySound.release();
		finish();
	}
}
