package com.example.bluetoothtest.lib;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleCommThread extends IBleCommThread {
	protected static final String TAG = "BleCommThread";
	private IBleBluetoothComm bleBthComm;
	private boolean mConnected = false;
	private String receiveData = new String();
	private Context mContext;
	private int BizCode, Models;
	private boolean mdisConnect = false;
	private IBthMsgCallback callback;
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (IBleBluetoothComm.ACTION_GATT_CONNECTED.equals(action)) {
				Log.i(TAG, "蓝牙连接完成！！！");
				mConnected = true;
				receiveData = "";
				// 停止扫描
			} else if (IBleBluetoothComm.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				// 检测到断开
				//if (mConnected) {
					Log.i(TAG, "蓝牙断开！！！");
					mConnected = false;
					bleBthComm.close();
					mHandler.postDelayed(mRunable, 3000);
					receiveData = "";
				//}
			} else if (IBleBluetoothComm.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
			} else if (IBleBluetoothComm.ACTION_DATA_AVAILABLE
					.equals(action)) {
				// 数据的处理
				Log.i(TAG, "进行数据处理");
				String data = intent
						.getStringExtra(IBleBluetoothComm.EXTRA_DATA);
				if (data.equals(receiveData)) {// 重复数据过滤
					Log.i(TAG, "进行重复数据过滤处理");
				} else {
					receiveData = data;
					if (callback != null) {
						callback.OnReceive(data, BizCode, Models);
					}

				}

			} else if (IBleBluetoothComm.ACTION_GATT_BEGIN_CONNECT
					.equals(action)) {
				Log.i(TAG, "开始连接蓝牙");
				if (!mConnected) {
					bleBthComm.connect();
				}
			} else if (IBleBluetoothComm.ACTION_GATT_FINISH_SEARCH
					.equals(action)) {
				Log.i(TAG, "扫描完成，没有发现蓝牙设备,3S后继续开始扫描");
				mHandler.postDelayed(mRunable, 3000);
			}
			if(callback!=null){
				callback.OnMessage(action);
			}
		}
	};

	public BleCommThread(Context context, int BizCode, int Models) {
		mContext = context;
		this.BizCode = BizCode;
		this.Models = Models;
		bleBthComm = BleCommFactory.getClassForBluetoothComm(context, BizCode,
				Models);
		// bleBthComm = new IBleBluetoothComm(context, BizCode,
		// Models);
	}

	public int init() {
		int res = bleBthComm.initBthDeviec();
		if (res == 0) {
			mContext.registerReceiver(mGattUpdateReceiver,
					makeGattUpdateIntentFilter());
		}
		return res;
	}

	public void unInit() {
		mdisConnect = false;
		mHandler.removeCallbacks(mRunable);
		bleBthComm.scanLeDevice(false);
		bleBthComm.close();
		
		mContext.unregisterReceiver(mGattUpdateReceiver);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter
				.addAction(IBleBluetoothComm.ACTION_GATT_CONNECTED);
		intentFilter
				.addAction(IBleBluetoothComm.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(IBleBluetoothComm.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter
				.addAction(IBleBluetoothComm.ACTION_DATA_AVAILABLE);
		intentFilter
				.addAction(IBleBluetoothComm.ACTION_GATT_FINISH_SEARCH);
		intentFilter
				.addAction(IBleBluetoothComm.ACTION_GATT_BEGIN_CONNECT);
		return intentFilter;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// super.run();
		bleBthComm.scanLeDevice(true);
	}

	private Handler mHandler = new Handler();
	private Runnable mRunable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			bleBthComm.scanLeDevice(true);
		}
	};
	public void setBthCallBack(IBthMsgCallback callback) {
		this.callback = callback;
	}
	public String getBthName(){
		String bthName = new String();
		BluetoothDevice device = bleBthComm.getBluetoothDevice();
		if(device!=null){
			bthName = 	device.getName();
		}
		return bthName;
	}
	public void writeLlsAlertLevel(int iAlertLevel, byte[] bb){
		bleBthComm.writeLlsAlertLevel(iAlertLevel, bb);
	}
	public void disConnect(){
		bleBthComm.disConnect();
	}

	@Override
	public void setBthAddress(String address) {
		// TODO Auto-generated method stub
		
	}
}
