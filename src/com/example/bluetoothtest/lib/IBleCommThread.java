package com.example.bluetoothtest.lib;

public abstract class IBleCommThread extends Thread{
	public abstract int init();
	public abstract void unInit();
	public abstract void setBthCallBack(IBthMsgCallback callback);
	public abstract void setBthAddress(String address);
	public abstract void writeLlsAlertLevel(int iAlertLevel, byte[] bb);
	public abstract void disConnect();
	public abstract String getBthName();
}
