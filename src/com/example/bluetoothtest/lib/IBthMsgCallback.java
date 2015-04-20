package com.example.bluetoothtest.lib;

public interface IBthMsgCallback {
	public void OnReceive(String data,int BizCode,int Models);
	public void OnMessage(String Action);
}
