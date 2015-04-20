package com.example.bluetoothtest.lib;

import android.content.Context;

public class BleCommFactory {
/**
 * 蓝牙通信工厂类
 */
	public static IBleBluetoothComm getClassForBluetoothComm(Context context,int BizCode,int Models){
		IBleBluetoothComm bluethComm = null;
		switch(BizCode){
		case 4:
			bluethComm = new BleBluetoothCommForTiWen(context,BizCode,Models);
		break;
		default:
			bluethComm = new BleBluetoothCommForGeneral(context,BizCode,Models);
			break;
		}
		return bluethComm;
	}
}
