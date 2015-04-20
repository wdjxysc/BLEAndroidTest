package com.example.bluetoothtest;

import java.text.DecimalFormat;

import android.util.Log;

public class DataUtil {
	// lb与kg转换
	public static String kgTolb(String data) {
		if (data == null || data.length() == 0) {
			return "";
		}

		double result = 0.0;

		try {
			double tmp = Double.parseDouble(data);

			result = tmp * 1155845 / 16;
			result = result / 65536;

			DecimalFormat df = new DecimalFormat("#.0");
			String strResult = df.format(result);
			result = Double.parseDouble(strResult);

			result = result*2;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("kgTolb", "eorr");
		}


		return ""+result;
	}


	public static String lbTokg(String data){
		if (data == null || data.length() == 0) {
			return "";
		}

		double result = 0.0;
		try {
			double tmp = Double.parseDouble(data);
			tmp = tmp/2;
			result = tmp*16/1155845;
			result = result * 65536;

			DecimalFormat df = new DecimalFormat("#");
		    String strResult = df.format(result);
			result = Double.parseDouble(strResult);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("lbTokg", "eorr");
		}

		return ""+result;
	}
}
