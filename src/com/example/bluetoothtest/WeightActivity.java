package com.example.bluetoothtest;

import com.example.bluetoothtest.lib.BleCommThread;
import com.example.bluetoothtest.lib.BthCommService;
import com.example.bluetoothtest.lib.IBleBluetoothComm;
import com.example.bluetoothtest.lib.IBleCommThread;
import com.example.bluetoothtest.lib.IBthMsgCallback;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class WeightActivity extends Activity {
	private static final String TAG = "WeightActivity";
	private IBleCommThread bthThread;
	private WeightBthCallBack weightBthCallBack = null;
	private static final int RECRIVE_WEIGHT_DATA = 0;
	private static final int INIT_BHT_FAILE = 1;
	private static final int RECRIVE_WEIGHT_MESSAGE = 2;
	private TextView textWeithData,textMessage;
    private SensorManager sensorManager;  
    private Vibrator vibrator;  
    private static final int SENSOR_SHAKE = 10; 
    private boolean openService = false;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case RECRIVE_WEIGHT_DATA:
				String data = (String) msg.obj;
				DealWithData(data);
				
				break;
			case INIT_BHT_FAILE:
				switch (msg.arg1) {
				case -1:
					Toast.makeText(WeightActivity.this, "该手机不支持蓝牙4.0",
							Toast.LENGTH_LONG).show();
					break;
				case -2:
					Toast.makeText(WeightActivity.this, "该手机系统不是4.3以上版本",
							Toast.LENGTH_LONG).show();
					break;
				case -3:
					Toast.makeText(WeightActivity.this, "该手机不支持蓝牙",
							Toast.LENGTH_LONG).show();
					break;
				}
				break;
			case RECRIVE_WEIGHT_MESSAGE:
				String message = (String) msg.obj;
				if (message.equals(IBleBluetoothComm.ACTION_GATT_FINISH_SEARCH)) {
					//textMessage.setText("扫描完成，没有发现蓝牙设备,3S后继续开始扫描");
					textMessage.setText("请摇一摇");
			        if (sensorManager != null) {// 注册监听器  
			            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);  
			            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率  
			        } 
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_BEGIN_CONNECT)) {
					textMessage.setText("开始连接蓝牙");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_CONNECTED)) {
					textMessage.setText("蓝牙连接完成！！！");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_DISCONNECTED)) {
					//textMessage.setText("蓝牙断开！！！");

					textMessage.setText("请摇一摇");
			        if (sensorManager != null) {// 注册监听器  
			            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);  
			            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率  
			        } 
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_SERVICES_DISCOVERED)) {
					textMessage.setText("发现蓝牙通信服务信息");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_DATA_AVAILABLE)) {
					textMessage.setText("进行数据处理");
				}
				break;
			case SENSOR_SHAKE:
				if(!openService){
					Intent BthServiceIntent = new Intent(WeightActivity.this, BthCommService.class);
					BthServiceIntent.putExtra("BizCode", 2);
					BthServiceIntent.putExtra("Models", 1);
					openService = getApplication().bindService(BthServiceIntent,
							mServiceConnection, BIND_AUTO_CREATE);
					Log.i(TAG, "isStartService = " + openService);
				}else{
					getApplicationContext().unbindService(mServiceConnection);
					openService = false;
					
					Intent BthServiceIntent = new Intent(WeightActivity.this, BthCommService.class);
					BthServiceIntent.putExtra("BizCode", 2);
					BthServiceIntent.putExtra("Models", 1);
					openService = getApplication().bindService(BthServiceIntent,
							mServiceConnection, BIND_AUTO_CREATE);
					Log.i(TAG, "isStartService = " + openService+" Restart");
				}
				
				
				textMessage.setText("请上称");
				if (sensorManager != null) {// 取消监听器  
		            sensorManager.unregisterListener(sensorEventListener);  
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
			bthThread.setBthCallBack(weightBthCallBack);
			//bthThread.setBthAddress("20:CD:39:A5:CF:C0");
			//bthThread.setBthAddress("BC:6A:29:26:AB:39");
			//bthThread.setBthAddress("20:CD:39:A7:E2:EE");
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
		setContentView(R.layout.activity_weight);
		textWeithData = (TextView) findViewById(R.id.weight_value);
		textMessage = (TextView)findViewById(R.id.message);
		weightBthCallBack = new WeightBthCallBack();

//		Intent BthServiceIntent = new Intent(this, BthCommService.class);
//		BthServiceIntent.putExtra("BizCode", 2);
//		BthServiceIntent.putExtra("Models", 1);
//		openService = getApplication().bindService(BthServiceIntent,
//				mServiceConnection, BIND_AUTO_CREATE);
//		Log.i(TAG, "isStartService = " + openService);
		
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE); 
        
        textMessage.setText("请摇一摇");
        if (sensorManager != null) {// 注册监听器  
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);  
            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率  
        }  
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy....");
	
		if(openService){
			getApplicationContext().unbindService(mServiceConnection);
			openService = false;
		}
		if (sensorManager != null) {// 取消监听器  
            sensorManager.unregisterListener(sensorEventListener);  
        } 

	}

	public class WeightBthCallBack implements IBthMsgCallback {

		@Override
		public void OnReceive(String data, int BizCode, int Models) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_WEIGHT_DATA;
			msg.obj = data;
			mHandler.sendMessage(msg);
		}

		@Override
		public void OnMessage(String Action) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_WEIGHT_MESSAGE;
			msg.obj = Action;
			mHandler.sendMessage(msg);
		}

	}

	public void DealWithData(String data) {
		String WeightResultData = data.toString().trim();

		String[] val = data.split(" ");
		// 解析体重数据
		String weightGao = Integer.toHexString(Integer.parseInt(val[1]));
		String weightDi = Integer.toHexString(Integer.parseInt(val[2]));

		if (Integer.parseInt(weightDi, 16) <= 16) {
			Log.i(TAG, "####进行了加0处理。");
			weightDi = "0" + weightDi;
		}

		String weight = weightGao.substring(1, 2) + weightDi;

		float weightValue = Integer.parseInt(weight, 16);

		float weightValue2 = weightValue / 10;

		int unit = 2;
		float weightValue3;
		if (unit == 1) {
			weightValue3 = Float.parseFloat(DataUtil.kgTolb(weightValue2 + ""));
			textWeithData.setText(weightValue3 + "");
		} else {
			textWeithData.setText(weightValue2 + "");
		}

		if (weightValue2 > 0 && data.length() > 12) {

			// 需要进行LB转换
			if (unit == 1) {

				weightValue3 = Float.parseFloat(DataUtil.kgTolb(weightValue2
						+ ""));
				textWeithData.setText(weightValue3 + "");
			} else {
				textWeithData.setText(weightValue2 + "");
			}

		}
		if(val[0].equals("255")){
			bthThread.disConnect();
		}
		
		Log.i("weight data = ", "data");
	}
	 /** 
     * 重力感应监听 
     */  
    private SensorEventListener sensorEventListener = new SensorEventListener() {  
  
        @Override  
        public void onSensorChanged(SensorEvent event) {  
            // 传感器信息改变时执行该方法  
            float[] values = event.values;  
            float x = values[0]; // x轴方向的重力加速度，向右为正  
            float y = values[1]; // y轴方向的重力加速度，向前为正  
            float z = values[2]; // z轴方向的重力加速度，向上为正  
            //Log.i(TAG, "x轴方向的重力加速度" + x +  "；y轴方向的重力加速度" + y +  "；z轴方向的重力加速度" + z);  
            // 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。  
            int medumValue = 12;// 三星 i9250怎么晃都不会超过20，没办法，只设置19了  
            if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {  
                vibrator.vibrate(200);  
                Message msg = new Message();  
                msg.what = SENSOR_SHAKE;  
                mHandler.sendMessage(msg);  
            }  
        }  
  
        @Override  
        public void onAccuracyChanged(Sensor sensor, int accuracy) {  
  
        }  
    }; 
    public void startbluethbtn(View view){
        Message msg = new Message();  
        msg.what = SENSOR_SHAKE;  
        mHandler.sendMessage(msg);  
    }
 
}
