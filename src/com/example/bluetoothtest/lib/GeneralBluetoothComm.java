package com.example.bluetoothtest.lib;

import android.content.Context;

/**
 * 普通蓝牙通信
 * @author
 */
public class GeneralBluetoothComm implements BaseBluetoothComm{
	public Context mContext;
	public GeneralBluetoothComm(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	public int initBthDeviec() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean connect() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void disConnect() {
		// TODO Auto-generated method stub
		
	}


	
}
