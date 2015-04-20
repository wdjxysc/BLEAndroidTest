package com.example.bluetoothtest;
import java.math.BigDecimal;
import java.util.Calendar;
import com.example.bluetoothtest.WeightActivity.WeightBthCallBack;
import com.example.bluetoothtest.lib.BleCommThread;
import com.example.bluetoothtest.lib.BthCommService;
import com.example.bluetoothtest.lib.IBleBluetoothComm;
import com.example.bluetoothtest.lib.IBleCommThread;
import com.example.bluetoothtest.lib.IBthMsgCallback;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class FatJLActivity extends Activity{
	private TextView txt_weight,txt_zhifanglv,txt_jirouliang,txt_shuifenlv;
	private TextView txt_guliang,txt_BMR,txt_neizangdengji,txt_BMI,txt_message;
	private EditText edit_height,edit_weight,edit_age;
	private RadioGroup sexRadioGroup;
	private RadioButton girlRadioButton , boyRadioButton;
	private int isex = 0;
	private Button btn_connect,btn_disconnect;
	
	private static final String TAG = "WeightActivity";
	private IBleCommThread bthThread;
	private FatBthCallBack fatBthCallBack = null;
	private static final int RECRIVE_FAT_DATA = 0;
	private static final int INIT_BHT_FAILE = 1;
	public static final int RECRIVE_FAT_MESSAGE = 2;
	private int userWork = 0;
	private boolean openService = false;
    private SensorManager sensorManager;  
    private Vibrator vibrator;  

    private static final int SENSOR_SHAKE = 10;  
    
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case RECRIVE_FAT_DATA:
				String data = (String) msg.obj;
				DealWithData(data);
				//sendCloseCommd();
				break;
			case INIT_BHT_FAILE:
				switch(msg.arg1){
				case -1:
					Toast.makeText(FatJLActivity.this, "该手机不支持蓝牙4.0", Toast.LENGTH_LONG).show();
					break;
				case -2:
					Toast.makeText(FatJLActivity.this, "该手机系统不是4.3以上版本", Toast.LENGTH_LONG).show();
					break;
				case -3:
					Toast.makeText(FatJLActivity.this, "该手机不支持蓝牙", Toast.LENGTH_LONG).show();
					break;
				}
				break;
			case RECRIVE_FAT_MESSAGE:
				String message = (String) msg.obj;
				if (message.equals(IBleBluetoothComm.ACTION_GATT_FINISH_SEARCH)) {
					//txt_message.setText("扫描完成，没有发现蓝牙设备,3S后继续开始扫描");
					txt_message.setText("请摇一摇");
			        if (sensorManager != null) {// 注册监听器  
			            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);  
			            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率  
			        } 
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_BEGIN_CONNECT)) {
					txt_message.setText("开始连接蓝牙");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_CONNECTED)) {
					txt_message.setText("蓝牙连接完成！！！");
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_DISCONNECTED)) {
					//txt_message.setText("蓝牙断开！！！");
					txt_message.setText("请摇一摇");
			        if (sensorManager != null) {// 注册监听器  
			            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);  
			            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率  
			        } 
				} else if (message
						.equals(IBleBluetoothComm.ACTION_GATT_SERVICES_DISCOVERED)) {
					txt_message.setText("发现蓝牙通信服务信息");
					sendSettingInfo();
				} else if (message
						.equals(IBleBluetoothComm.ACTION_DATA_AVAILABLE)) {
					txt_message.setText("进行数据处理");
				}
				break;
			case SENSOR_SHAKE:
				if(!openService){
					Intent BthServiceIntent = new Intent(FatJLActivity.this, BthCommService.class);
					BthServiceIntent.putExtra("BizCode", 5);
					BthServiceIntent.putExtra("Models", 1);
					openService = getApplication().bindService(BthServiceIntent,
							mServiceConnection, BIND_AUTO_CREATE);
					Log.i(TAG, "isStartService = " + openService);
				}else{
					getApplicationContext().unbindService(mServiceConnection);
					openService = false;
					
					Intent BthServiceIntent = new Intent(FatJLActivity.this, BthCommService.class);
					BthServiceIntent.putExtra("BizCode", 5);
					BthServiceIntent.putExtra("Models", 1);
					openService = getApplication().bindService(BthServiceIntent,
							mServiceConnection, BIND_AUTO_CREATE);
					Log.i(TAG, "isStartService = " + openService+" Restart");
				}
				
				
				txt_message.setText("请上称");
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
			bthThread.setBthCallBack(fatBthCallBack);
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
		setContentView(R.layout.activity_fatjl);
		
		fatBthCallBack = new FatBthCallBack();
		initControl();
		
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);  
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE); 
        
        txt_message.setText("请摇一摇");
        if (sensorManager != null) {// 注册监听器  
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);  
            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率  
        }  
	}
	public void initControl(){
		txt_weight = (TextView)findViewById(R.id.weight_txt);
		txt_zhifanglv = (TextView)findViewById(R.id.zhifanglv_txt);
		txt_jirouliang = (TextView)findViewById(R.id.jirouliang_txt);
		txt_shuifenlv = (TextView)findViewById(R.id.shuifenlv_txt);
		txt_guliang = (TextView)findViewById(R.id.guliang_txt);
		txt_BMR = (TextView)findViewById(R.id.bmr_txt);
		txt_neizangdengji = (TextView)findViewById(R.id.neizangdengji_txt);
		txt_BMI = (TextView)findViewById(R.id.bmi_txt);
		
		String str_weight = String.format(getString(R.string.fat_weight), 0.0);
		String str_zhifanglv = String.format(getString(R.string.fat_zhifanglv), 0.0);
		String str_jirouliang = String.format(getString(R.string.fat_jirouliang), 0.0);
		String str_shuifenlv = String.format(getString(R.string.fat_shuifenlv), 0.0);
		String str_guliang = String.format(getString(R.string.fat_guliang), 0.0);
		String str_BMR = String.format(getString(R.string.fat_bmr), 0.0);
		String str_neizangdengji = String.format(getString(R.string.fat_neizangdengji), 0.0);
		String str_BMI = String.format(getString(R.string.fat_bmi), 0.0);
		
		txt_weight.setText(str_weight);
		txt_zhifanglv.setText(str_zhifanglv);
		txt_jirouliang.setText(str_jirouliang);
		txt_shuifenlv.setText(str_shuifenlv);
		txt_guliang.setText(str_guliang);
		txt_BMR.setText(str_BMR);
		txt_neizangdengji.setText(str_neizangdengji);
		txt_BMI.setText(str_BMI);
		
		sexRadioGroup = (RadioGroup) this.findViewById(R.id.sex_radioGroup);
		girlRadioButton = (RadioButton) this.findViewById(R.id.sex_girl_radioButton);
		boyRadioButton = (RadioButton) this.findViewById(R.id.sex_boy_radioButton);
		//性别信息修改
		sexRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (boyRadioButton.getId() == checkedId) {
					isex = 0;//男
				} else {
					isex = 1;//女
				}

			}
		});
		
		edit_height = (EditText)findViewById(R.id.edit_height);
		edit_weight = (EditText)findViewById(R.id.edit_weight);
		edit_age = (EditText)findViewById(R.id.edit_age);
		
		btn_connect = (Button)findViewById(R.id.btn_connect);
		btn_connect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btn_disconnect.setEnabled(true);
				btn_connect.setEnabled(false);
				
				Intent BthServiceIntent = new Intent(FatJLActivity.this, BthCommService.class);
				BthServiceIntent.putExtra("BizCode", 5);
				BthServiceIntent.putExtra("Models", 1);
				openService = getApplication().bindService(BthServiceIntent,
						mServiceConnection, BIND_AUTO_CREATE);
				Log.i(TAG, "isStartService = " + openService);
			}
		});
		btn_disconnect = (Button)findViewById(R.id.btn_disconnect);
		btn_disconnect.setEnabled(false);
		btn_disconnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				btn_connect.setEnabled(true);
				btn_disconnect.setEnabled(false);
				if(openService){
					getApplicationContext().unbindService(mServiceConnection);
					openService = false;
				}
			}
		});
		txt_message = (TextView)findViewById(R.id.fatjl_message);
	}
	public class FatBthCallBack implements IBthMsgCallback{

		@Override
		public void OnReceive(String data, int BizCode, int Models) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_FAT_DATA;
			msg.obj = data;
			mHandler.sendMessage(msg);
		}

		@Override
		public void OnMessage(String Action) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain();
			msg.what = RECRIVE_FAT_MESSAGE;
			msg.obj = Action;
			mHandler.sendMessage(msg);
		}
		
	}
	public void DealWithData(String data) {
		try {
			String BodyFatResult = data.toString().trim();
			System.out.println("开始解析体成分数据！！！！");
			String[] val = data.split(" ");
			//解析体重数据
			String weightGao = Integer.toHexString(Integer
					.parseInt(val[1]));
			String weightDi = Integer.toHexString(Integer
					.parseInt(val[2]));
			String weight = weightGao.substring(1, 2) + weightDi;
			float weightValue = Integer.parseInt(weight, 16);
			float weightValue2 = weightValue / 10;
			String str_weight = String.format(getString(R.string.fat_weight),weightValue2);
			txt_weight.setText(str_weight);
			//解析BMI
			String heightStr = Integer.toHexString(Integer
					.parseInt(val[10]));
			float height = Integer.parseInt(heightStr, 16);
			float bmiValue2 = weightValue2 / (height * height) * 10000;
			BigDecimal bg = new BigDecimal(bmiValue2);
			bmiValue2 = bg.setScale(1, BigDecimal.ROUND_HALF_UP)
					.floatValue();
			String str_BMI = String.format(getString(R.string.fat_bmi), bmiValue2);
			txt_BMI.setText(str_BMI);
			//解析脂肪率
			String zhifangGao = Integer.toHexString(Integer
					.parseInt(val[12]));
			int gao = Integer.parseInt(zhifangGao, 16);
			int shijidegao = gao / 16;
			String zhifangDi = Integer.toHexString(Integer
					.parseInt(val[11]));
			if (Integer.parseInt(zhifangDi, 16) <= 16) {
				System.out.println("####进行了加0处理。");
				zhifangDi = "0" + zhifangDi;
			}
			
			String zhifang = shijidegao + zhifangDi;
			float zhifangValue = Integer.parseInt(zhifang, 16);
			float zhifangValue2 = zhifangValue / 10;
			
			
			String str_zhifanglv = String.format(getString(R.string.fat_zhifanglv), zhifangValue2);
			txt_zhifanglv.setText(str_zhifanglv);

	
			//解析内帐等级
			String neizhangGao = Integer.toHexString(Integer
					.parseInt(val[17]));
			int neizhangValue2 = Integer.parseInt(neizhangGao, 16);
			String str_neizangdengji = String.format(getString(R.string.fat_neizangdengji),neizhangValue2);
			txt_neizangdengji.setText(str_neizangdengji);
			
			//解析水分
			String waterGao = Integer.toHexString(Integer
					.parseInt(val[12]));
			String waterDi = Integer.toHexString(Integer
					.parseInt(val[13]));
			if (Integer.parseInt(waterGao, 16) <= 16) {
				System.out.println("####进行了加0处理。");
				waterGao = "0" + waterGao;
			}
			if (Integer.parseInt(waterDi, 16) <= 16) {
				System.out.println("####进行了加0处理。");
				waterDi = "0" + waterDi;
			}
			String water = waterGao.substring(1, 2) + waterDi;
			float waterValue = Integer.parseInt(water, 16);
			float waterValue2 = waterValue / 10;
			String str_shuifenlv = String.format(getString(R.string.fat_shuifenlv), waterValue2);
			txt_shuifenlv.setText(str_shuifenlv);
			
			//解析肌肉量
			String jirouGao = Integer.toHexString(Integer
					.parseInt(val[14]));
			String jirouDi = Integer.toHexString(Integer
					.parseInt(val[15]));
			String jirou = jirouGao.substring(0, 1) + jirouDi;
			float jirouValue = Integer.parseInt(jirou, 16);
			float jirouValue2 = jirouValue / 10;
			String str_jirouliang = String.format(getString(R.string.fat_jirouliang), jirouValue2);
			txt_jirouliang.setText(str_jirouliang);
			//解析骨量
			String boneGao = Integer.toHexString(Integer
					.parseInt(val[16]));
			float boneValue = Integer.parseInt(boneGao, 16);
			float boneValue2 = boneValue / 10;
			
			String str_guliang = String.format(getString(R.string.fat_guliang),boneValue2);
			txt_guliang.setText(str_guliang);

			//解析Kacl
			String kaclGao = Integer.toHexString(Integer
					.parseInt(val[18]));
			String kaclDi = Integer.toHexString(Integer
					.parseInt(val[19]));
			if (Integer.parseInt(kaclDi, 16) <= 16) {
				System.out.println("####进行了加0处理。");
				kaclDi = "0" + kaclDi;
			}
			String kacl = kaclGao + kaclDi;
			int kaclValue2 = Integer.parseInt(kacl, 16);
			String str_BMR = String.format(getString(R.string.fat_bmr), kaclValue2);
			txt_BMR.setText(str_BMR);
			if(val[0].equals("255")){
				sendCloseCommd();
			}
		} catch (Exception e) {
			// TODO: handle exception
			//Toast.makeText(MarvinFatJLActivity.this, R.string.bodyfat_data_error , Toast.LENGTH_SHORT).show();
			//e.printStackTrace();
		}
		
	}
	public void sendSettingInfo(){
		//给设备写入一条命令	
		byte weighthigh;
		byte weightlow;
		byte sportlvl = 0;
		byte genderage;
		byte height ;

		byte bheight = (byte)((int)Float.parseFloat(edit_height.getText().toString().trim()));
		int wet = (int) ((Float.parseFloat(edit_weight.getText().toString().trim()))*10);
		weighthigh =(byte) ((byte) ((wet)/256)+64);
		weightlow = (byte) ((wet)%256);
		if(bthThread!=null){
			if(bthThread.getBthName().equals("eBody-Scale")){
				sportlvl = (byte) (userWork*0x10+0x00);
			}else{
				 sportlvl = (byte) (userWork*0x10+0x40+0x00) ;
			}
		}else{
			return;
		}
		
	
		int iage = ((int)Float.parseFloat(edit_age.getText().toString().trim()));
        genderage = (byte)iage;
        
        if(isex==0){
        	isex += 0x80;
        }
        else{
        	isex = 0;
        }
        genderage = (byte) (iage+isex);
        
//        setUserInfo = new byte[] {(byte) 0xfd,0x53,2,-126,32,20,-80};
		
        
        final byte[] setUserInfo = new byte[] {(byte) 0xfd,0x53,weighthigh,weightlow,sportlvl,genderage,bheight};

		if(setUserInfo.length == 0){
				return;
		}
		if(bthThread!=null){
			new Thread(){
				public void run() {
					for(int i = 0;i<3;i++){
						try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						bthThread.writeLlsAlertLevel(3, setUserInfo);
					}
					
				};
			}.start();
			
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
	public void sendCloseCommd(){
		final byte[] closeCommd1 = new byte[] {(byte) 0xfd,0x30};
		final byte[] closeCommd2 = new byte[] {(byte) 0xfd,0x31};
		final byte[] closeCommd3 = new byte[] {(byte) 0xfd,0x32};
		if(bthThread!=null){
			new Thread(){
				public void run() {
					
						try {
							sleep(1000);
							bthThread.writeLlsAlertLevel(3, closeCommd1);
							sleep(1000);
							bthThread.disConnect();
							//bthThread.writeLlsAlertLevel(3, closeCommd2);
							//sleep(1000);
							//bthThread.writeLlsAlertLevel(3, closeCommd3);
							
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

				};
			}.start();
			
		}	
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
            Log.i(TAG, "x轴方向的重力加速度" + x +  "；y轴方向的重力加速度" + y +  "；z轴方向的重力加速度" + z);  
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
}
