package com.example.bluetoothtest.lib;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BthCommService extends Service{
	private IBleCommThread bthThread;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		if(intent!=null){
			int BizCode = intent.getIntExtra("BizCode", 0);
			int Models = intent.getIntExtra("Models", 0);
			if(BizCode!= 0 && Models!=0){
				if(BizCode == 2){
					if(bthThread == null){
						//bthThread = new NewBleCommThread(BthCommService.this, BizCode, Models);
						bthThread = new WeightBleCommThread(BthCommService.this, BizCode, Models);
					}
				}else if(BizCode == 5){
					if(bthThread == null){
						bthThread = new FatBleCommThread(BthCommService.this, BizCode, Models);
					}
				}
				else{
					if(bthThread == null){
						bthThread = new BleCommThread(BthCommService.this, BizCode, Models);
					}
				}
				
			}
		}
		return binder;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		
		if(bthThread!=null){
			bthThread.unInit();
			bthThread = null;
		}
		return super.onUnbind(intent);
	}
	private MyBinder binder = new MyBinder();
	public class MyBinder extends Binder{
		public IBleCommThread getThread(){
			return bthThread;
		}
	}
}
