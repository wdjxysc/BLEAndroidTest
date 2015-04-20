package com.example.bluetoothtest;
import com.example.bluetoothtest.lib.BthCommService;
import com.example.bluetoothtest.lib.IBleBluetoothComm;
import com.example.bluetoothtest.lib.IBleCommThread;
import com.example.bluetoothtest.lib.IBthMsgCallback;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class BloodJLActivity extends Activity {
	private static final String TAG = "WeightActivity";
	private IBleCommThread bthThread;
	private BooldJLBthCallBack booldJLBthCallBack = null;
	private static final int RECRIVE_BLOODYL_DATA = 0;
	private static final int INIT_BHT_FAILE = 1;
	public static final int RECRIVE_BLOODYL_MESSAGE = 2;
	private TextView blood_sys,blood_dia,blood_plues,blood_message;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case RECRIVE_BLOODYL_DATA:
				String data = (String) msg.obj;
				DealWithData(data);
				break;
			case INIT_BHT_FAILE:
				switch (msg.arg1) {
				case -1:
					Toast.makeText(BloodJLActivity.this, "该手机不支持蓝牙4.0",
							Toast.LENGTH_LONG).show();
					break;
				case -2:
					Toast.makeText(BloodJLActivity.this, "该手机系统不是4.3以上版本",
							Toast.LENGTH_LONG).show();
					break;
				case -3:
					Toast.makeText(BloodJLActivity.this, "该手机不支持蓝牙",
							Toast.LENGTH_LONG).show();
					break;
				}
				break;
			case RECRIVE_BLOODYL_MESSAGE:
				String message = (String) msg.obj;
				if (message.equals(IBleBluetoothComm.ACTION_GATT_FINISH_SEARCH)) {
					blood_message.setText("扫描完成，没有发现蓝牙设备,3S后继续开始扫描");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_BEGIN_CONNECT)) {
					blood_message.setText("开始连接蓝牙");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_CONNECTED)) {
					blood_message.setText("蓝牙连接完成！！！");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_DISCONNECTED)) {
					blood_message.setText("蓝牙断开！！！");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_SERVICES_DISCOVERED)) {
					blood_message.setText("发现蓝牙通信服务信息");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_DATA_AVAILABLE)) {
					blood_message.setText("进行数据处理");
				}
				break;
			}
		}
	};
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			bthThread = ((BthCommService.MyBinder) service).getThread();
			bthThread.setBthCallBack(booldJLBthCallBack);
			int res = bthThread.init();
			if (res == 0) {
				bthThread.start();
			} else {
				Message msg = Message.obtain();
				msg.arg1 = res;
				mHandler.sendMessage(msg);
			}
			Log.i(TAG, "res = " + res);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bloodjl);
		blood_sys = (TextView) findViewById(R.id.bloodjl_sys);
		blood_plues = (TextView) findViewById(R.id.bloodjl_pulse);
		blood_dia = (TextView) findViewById(R.id.bloodjl_dia);
		String sys = String.format(getResources().getString(R.string.blood_sys), 0.0);
		String plues = String.format(getResources().getString(R.string.blood_pulse), 0.0);
		String dia = String.format(getResources().getString(R.string.blood_dia), 0.0);
		blood_sys.setText(sys);
		blood_plues.setText(plues);
		blood_dia.setText(dia);
		booldJLBthCallBack = new BooldJLBthCallBack();
		
		blood_message = (TextView)findViewById(R.id.blood_message);

		Intent BthServiceIntent = new Intent(this, BthCommService.class);
		BthServiceIntent.putExtra("BizCode", 3);
		BthServiceIntent.putExtra("Models", 1);
		boolean isStartService = getApplication().bindService(BthServiceIntent,
				mServiceConnection, BIND_AUTO_CREATE);
		Log.i(TAG, "isStartService = " + isStartService);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy....");
		getApplicationContext().unbindService(mServiceConnection);
	}

	public class BooldJLBthCallBack implements IBthMsgCallback {

		@Override
		public void OnReceive(String data, int BizCode, int Models) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_BLOODYL_DATA;
			msg.obj = data;
			mHandler.sendMessage(msg);
		}

		@Override
		public void OnMessage(String Action) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_BLOODYL_MESSAGE;
			msg.obj = Action;
			mHandler.sendMessage(msg);
		}

	}

	private void DealWithData(String data) {
		// TODO Auto-generated method stub
		if (data != null) {

			String[] val = data.split(" ");
//			// 解析血压数据
//			int dia = Integer.parseInt(val[1]);
//			blood_sys.setText(dia + "");
//
//			int boolear = Integer.parseInt(blood_sys.getText().toString());
//
//			if (boolear < 80) {
//				blood_dia.setText(0 + "");
//				blood_plues.setText(0 + "");
//			}

			if (data.length() > 10) {
				String BloodResultData = data.toString().trim();

				int isys = Integer.parseInt(val[2]);
				int idia2 = Integer.parseInt(val[4]);
				int iplues = Integer.parseInt(val[8]);

				String sys = String.format(getResources().getString(R.string.blood_sys), isys);
				String plues = String.format(getResources().getString(R.string.blood_pulse), iplues);
				String dia = String.format(getResources().getString(R.string.blood_dia), idia2);
				
				blood_sys.setText(sys);
				blood_dia.setText(dia);
				blood_plues.setText(plues);
			}else{
				int idia = Integer.parseInt(val[1]);
				
	
				String sys = String.format(getResources().getString(R.string.blood_sys), 0);
				String plues = String.format(getResources().getString(R.string.blood_pulse), 0);
				String dia = String.format(getResources().getString(R.string.blood_dia), idia);
				
				blood_sys.setText(sys);
				blood_dia.setText(dia);
				blood_plues.setText(plues);
			}

		}

	};
}
