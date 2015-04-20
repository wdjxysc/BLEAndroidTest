package com.example.bluetoothtest.lib;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

public interface IBleBluetoothComm {
	public final static String ACTION_GATT_FINISH_SEARCH = "com.example.bluetooth.le.ACTION_GATT_FINISH_SEARCH";
	public final static String ACTION_GATT_BEGIN_CONNECT = "com.example.bluetooth.le.ACTION_GATT_BEGIN_CONNECT";
	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	public final String LIST_NAME = "NAME";
	public final String LIST_UUID = "UUID";
	
	
	public void close();
	public List<BluetoothGattService> getSupportedGattServices();
	public boolean connect();
	public boolean connect(String address);
	public int initBthDeviec();
	public void scanLeDevice(boolean enable);
	public void readCharacteristic(BluetoothGattCharacteristic characteristic);
	public void disConnect();
	public void writeLlsAlertLevel(int iAlertLevel, byte[] bb);
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled);
	public BluetoothDevice getBluetoothDevice(); 
}
