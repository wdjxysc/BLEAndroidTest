package com.example.bluetoothtest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void OpenWeightActivity(View view){
		Intent intent = new Intent(MainActivity.this,WeightActivity.class);
		startActivity(intent);
	}
	public void OpenBloodJLActivity(View view){
		Intent intent = new Intent(MainActivity.this,BloodJLActivity.class);
		startActivity(intent);
	}
	public void OpenEarThermActivity(View view){
		Intent intent = new Intent(MainActivity.this,EarThermActivity.class);
		startActivity(intent);
	}
	public void OpenFATJLActivity(View view){
		Intent intent = new Intent(MainActivity.this,FatJLActivity.class);
		startActivity(intent);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			android.os.Process
			.killProcess(android.os.Process.myPid());
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
