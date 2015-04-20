package com.example.bluetoothtest.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleBluetoothCommForTiWen implements IBleBluetoothComm {
	protected static final String TAG = "BleBluetoothCommForTiWen";
	private Context mContext;
	private Handler mHandler;
	private BluetoothAdapter mBluetoothAdapter;
	// 10秒后停止查找搜索.
	private static final long SCAN_PERIOD = 10000;

	private boolean mScanning;
	private BluetoothDevice device = null;
	// 采集器蓝牙名称
	private List<String> listCollectName;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public static final UUID SERVIE_UUID = UUID
			.fromString("00001910-0000-1000-8000-00805f9b34fb");
	public static final UUID RED_LIGHT_CONTROL_UUID = UUID
			.fromString("0000FFF4-0000-1000-8000-00805f9b34fb");
	public static final UUID RED_LIGHT_CONTROL_UUID_TWO = UUID
			.fromString("0000FFF1-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_TIWEN_RATE_MEASUREMENT = UUID
			.fromString(SampleGattAttributes.TIWEN_RATE_MEASUREMENT);
	public String receiveData;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private String mBluetoothDeviceAddress;
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				Log.i(TAG, "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				Log.i(TAG, "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				displayGattServices(getSupportedGattServices());
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
				System.out.println("onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, "onCharacteristicRead");
			if (status == BluetoothGatt.GATT_SUCCESS) {

				Log.i("TAG", characteristic.getValue().toString());

				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}

		/**
		 * 返回数据。
		 */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {

			// 数据
			String str = "";
			for (int i = 0; i < characteristic.getValue().length; i++) {
				str = str + (characteristic.getValue()[i] & 0xff) + " ";
			}

			if (str.indexOf("254") > 1) {

				receiveData = str.toString().trim();
				Log.i(TAG, str);
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
		}
	};
	private Runnable searchRunnable = new Runnable() {
		@Override
		public void run() {
			if (device == null) {
				broadcastUpdate(ACTION_GATT_FINISH_SEARCH);
				Log.i(TAG, "searchRunnable device=null");
			}
			mScanning = false;
			Log.i(TAG, "searchRunnable mScanning=" + mScanning);
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mHandler.removeCallbacks(searchRunnable);
		}
	};

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			Log.i(TAG, "BluetoothDevice  name=" + device.getName()
					+ " address=" + device.getAddress());

			String name = device.getName();
			for (String tempName : listCollectName) {
				if (tempName.equals(name)) {
					BleBluetoothCommForTiWen.this.device = device;

					if (mScanning) {
						scanLeDevice(false);
					}
					Log.i(TAG, "蓝牙设备找到......mScanning+" + mScanning);
					String intentAction = ACTION_GATT_BEGIN_CONNECT;
					broadcastUpdate(intentAction);
					break;
				}
			}

		}
	};

	public BleBluetoothCommForTiWen(Context context, int BizCode, int Models) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mHandler = new Handler();
		listCollectName = BthUtils.getCollectCfgXmlParse(context,
				"collectcfg.xml", BizCode, Models);
	}

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		mContext.sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
			final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (UUID_TIWEN_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
			int flag = characteristic.getProperties();
			int format = -1;
			if ((flag & 0x01) != 0) {
				format = BluetoothGattCharacteristic.FORMAT_UINT16;
				Log.d(TAG, "Heart rate format UINT16.");
			} else {
				format = BluetoothGattCharacteristic.FORMAT_UINT8;
				Log.d(TAG, "Heart rate format UINT8.");
			}
			final int heartRate = characteristic.getIntValue(format, 1);
			Log.d(TAG, String.format("Received heart rate: %d", heartRate));

			// 传值
			Log.i(TAG, "传变量值到对应的界面：" + receiveData);
			intent.putExtra(EXTRA_DATA, receiveData);// String.valueOf(heartRate)

		} else {
			// For all other profiles, writes the data formatted in
			// HEX.对于所有其他的配置文件，写入十六进制格式的数据。
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(
						data.length);
				for (byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				intent.putExtra(EXTRA_DATA, new String(data) + "\n"
						+ stringBuilder.toString());
			}
		}
		mContext.sendBroadcast(intent);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		//mBluetoothGatt.disconnect();

		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	@Override
	public List<BluetoothGattService> getSupportedGattServices() {
		// TODO Auto-generated method stub
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}

	@Override
	public boolean connect() {
		// TODO Auto-generated method stub
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// Previously connected device. Try to reconnect. (先前连接的设备。 尝试重新连接)
		if (mBluetoothDeviceAddress != null
				&& device.getAddress().equals(mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mConnectionState = STATE_CONNECTING;
				Log.i(TAG, "mBluetoothGatt.connect() success");
				return true;
			} else {
				Log.i(TAG, "mBluetoothGatt.connect() faile");
				return false;
			}
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		if(mBluetoothGatt!=null&&!device.getAddress().equals(mBluetoothDeviceAddress)){
			close();
		}
		mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
		Log.d(TAG, "Trying to create a new connection.");
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	@Override
	public int initBthDeviec() {
		// TODO Auto-generated method stub
		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
		if (!mContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return -1;// 不支持蓝牙4.0
		}
		if (android.os.Build.VERSION.SDK_INT < 18) {
			return -2;// sdk 4.3以下的版本不支持
		}
		// 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) mContext
				.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// 检查设备上是否支持蓝牙
		if (mBluetoothAdapter == null) {
			return -3;// 设备不支持蓝牙
		}
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		return 0;
	}

	@Override
	public void scanLeDevice(boolean enable) {
		// TODO Auto-generated method stub
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(searchRunnable, SCAN_PERIOD);
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			device = null;
			Log.i(TAG, "enable=true mScanning=" + mScanning);
		} else {
			mScanning = false;
			mHandler.removeCallbacks(searchRunnable);
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			Log.i(TAG, "enable=fasle mScanning=" + mScanning);
		}
	}

	@Override
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		// TODO Auto-generated method stub
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	@Override
	public void disConnect() {
		// TODO Auto-generated method stub
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	@Override
	public void writeLlsAlertLevel(int iAlertLevel, byte[] bb) {
		// TODO Auto-generated method stub
		BluetoothGattService linkLossService = mBluetoothGatt
				.getService(SERVIE_UUID);
		if (linkLossService == null) {
			Log.e(TAG, "link loss Alert service not found!");
			return;
		}
		// enableBattNoti(iDevice);
		BluetoothGattCharacteristic alertLevel = null;
		switch (iAlertLevel) {
		case 1: // red
			alertLevel = linkLossService
					.getCharacteristic(RED_LIGHT_CONTROL_UUID);
			break;
		case 2:
			alertLevel = linkLossService
					.getCharacteristic(RED_LIGHT_CONTROL_UUID_TWO);
			break;
		}
		if (alertLevel == null) {
			Log.e(TAG, "link loss Alert Level charateristic not found!");
			return;
		}
		boolean status = false;
		int storedLevel = alertLevel.getWriteType();
		Log.d(TAG, "storedLevel() - storedLevel=" + storedLevel);

		alertLevel.setValue(bb);
		Log.e("发送的指令", "bb" + bb[0]);

		alertLevel
				.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		status = mBluetoothGatt.writeCharacteristic(alertLevel);
		Log.d(TAG, "writeLlsAlertLevel() - status=" + status);
	}

	@Override
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		// TODO Auto-generated method stub
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
		/**
		 * 打开数据FFF4
		 */
		// This is specific to Heart Rate Measurement.
		if (UUID_TIWEN_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUID
							.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	// 演示如何遍历支持关贸总协定服务/特性。在此示例中，我们填充绑定到ExpandableListView UI上的数据结构。
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = "Unknown service";
		String unknownCharaString = "Unknown characteristic";
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			currentServiceData.put(LIST_NAME, SampleGattAttributes.lookupTiWen(
					uuid, unknownServiceString));
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
				currentCharaData.put(LIST_NAME, SampleGattAttributes
						.lookupTiWen(uuid, unknownCharaString));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);
			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
		}

		try {
			final BluetoothGattCharacteristic characteristic = mGattCharacteristics
					.get(2).get(0);
			setCharacteristicNotification(characteristic, true);
			readCharacteristic(characteristic);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
	}

	@Override
	public BluetoothDevice getBluetoothDevice() {
		// TODO Auto-generated method stub
		return device;
	}

	@Override
	public boolean connect(String address) {
		// TODO Auto-generated method stub
		return false;
	}
}
