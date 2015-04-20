package com.example.bluetoothtest;

import java.text.DecimalFormat;

import com.example.bluetoothtest.WeightActivity.WeightBthCallBack;
import com.example.bluetoothtest.lib.BleCommThread;
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

public class EarThermActivity extends Activity{
	private static final String TAG = "EarThermActivity";
	private IBleCommThread bthThread;
	private TiWenBthCallBack tiWenBthCallBack = null;
	private static final int RECRIVE_TIWEN_DATA = 0;
	private static final int INIT_BHT_FAILE = 1;
	public static final int RECRIVE_TIWEN_MESSAGE = 2;
	private TextView textTiWenData ,textMessage;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case RECRIVE_TIWEN_DATA:
				String data = (String) msg.obj;
				DealWithData(data);
				break;
			case INIT_BHT_FAILE:
				switch(msg.arg1){
				case -1:
					Toast.makeText(EarThermActivity.this, "该手机不支持蓝牙4.0", Toast.LENGTH_LONG).show();
					break;
				case -2:
					Toast.makeText(EarThermActivity.this, "该手机系统不是4.3以上版本", Toast.LENGTH_LONG).show();
					break;
				case -3:
					Toast.makeText(EarThermActivity.this, "该手机不支持蓝牙", Toast.LENGTH_LONG).show();
					break;
				}
				break;
			case RECRIVE_TIWEN_MESSAGE:
				String message = (String) msg.obj;
				if (message.equals(IBleBluetoothComm.ACTION_GATT_FINISH_SEARCH)) {
					textMessage.setText("扫描完成，没有发现蓝牙设备,3S后继续开始扫描");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_BEGIN_CONNECT)) {
					textMessage.setText("开始连接蓝牙");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_CONNECTED)) {
					textMessage.setText("蓝牙连接完成！！！");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_DISCONNECTED)) {
					textMessage.setText("蓝牙断开！！！");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_SERVICES_DISCOVERED)) {
					textMessage.setText("发现蓝牙通信服务信息");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_DATA_AVAILABLE)) {
					textMessage.setText("进行数据处理");
				}
				break;
			}
		};
	};
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			bthThread = ((BthCommService.MyBinder) service).getThread();
			bthThread.setBthCallBack(tiWenBthCallBack);
			int res = bthThread.init();
			if (res == 0) {
				bthThread.start();
			}else{
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
		setContentView(R.layout.activity_eartherm);
		textTiWenData = (TextView) findViewById(R.id.eartherm_value);
		String tiwenData = String.format(getString(R.string.eartherm_data), 0.0);
		textTiWenData.setText(tiwenData);
		tiWenBthCallBack = new TiWenBthCallBack();
		textMessage = (TextView)findViewById(R.id.eartherm_message);

		Intent BthServiceIntent = new Intent(this, BthCommService.class);
		BthServiceIntent.putExtra("BizCode", 4);
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

	public class TiWenBthCallBack implements IBthMsgCallback {

		@Override
		public void OnReceive(String data, int BizCode, int Models) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_TIWEN_DATA;
			msg.obj = data;
			mHandler.sendMessage(msg);
		}

		@Override
		public void OnMessage(String Action) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_TIWEN_MESSAGE;
			msg.obj = Action;
			mHandler.sendMessage(msg);
		}
	}
	public void DealWithData(String data) {

		if (data != null) {
			Log.d(TAG, "DealWithData data="+data);
			String[] val =data.split(" ");
			for (int i = 0; i < val.length; i++) {
				try {
					String earFE = Integer
							.toHexString(Integer.parseInt(val[i]));
					if (earFE.equals("fe")) {
						String earGao = Integer.toHexString(Integer
								.parseInt(val[i + 1]));
						String earDi = Integer.toHexString(Integer
								.parseInt(val[i + 2]));
						String ear = earGao + earDi;

						float earValue = Integer.parseInt(ear, 16);

						float earValue2 = earValue / 10;

						String tiwenData = String.format(getString(R.string.eartherm_data), earValue2);
						textTiWenData.setText(tiwenData);	
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	
	}
}
