package com.example.bluetoothtest.lib;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

class BthUtils {
	/**
	 * �����ɼ���������Ϣ��
	 * 
	 * @param FileName
	 *            ������Ҫ�������ļ�·��
	 * @return ���زɼ�����Ϣ���ϵ�һ������
	 */
	public static List<String> getCollectCfgXmlParse(Context context,
			String FileName, int BizCode, int Models) {
		InputStream is = null;
		List<String> listCollectName = null;
		try {
			is = context.getAssets().open(FileName);

			String xmlBizCode = new String();
			String xmlModels = new String();
			String xmlName = new String();
			try {
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				XmlPullParser parser = factory.newPullParser();
				// ���ñ��뷽ʽ
				parser.setInput(is, "utf-8");
				// ��ý��������¼���������п�ʼ�ĵ��������ĵ�����ʼ��ǩ�������ǩ���ı��¼��ȵ�
				int evtType = parser.getEventType();
				// һֱѭ����ֱ���ĵ�����
				while (evtType != parser.END_DOCUMENT) {
					switch (evtType) {
					case XmlPullParser.START_DOCUMENT:
						listCollectName = new ArrayList<String>();
						break;
					case XmlPullParser.START_TAG:
						String tag = parser.getName();
						// ����Ǳ�ǩ��ʼ����˵����Ҫʵ�����
						if (tag.equalsIgnoreCase("CollectStyle")) {

							xmlBizCode = parser.getAttributeValue(null,
									"bizcode");
						} else if (tag.equalsIgnoreCase("CollectModels")) {
							xmlModels = parser
									.getAttributeValue(null, "models");
						} else if (tag.equalsIgnoreCase("ModelName")) {
							xmlName = parser.getAttributeValue(null, "name");
						}
						break;
					case XmlPullParser.END_TAG:
						// ���������ǩ�������HealthSummary��������뼯����
						if (parser.getName().equalsIgnoreCase("ModelName")
								&& listCollectName != null) {
							if (xmlBizCode.equals(String.valueOf(BizCode))
									&& xmlModels.equals(String.valueOf(Models))) {
								listCollectName.add(xmlName);
								xmlName = "";
							}
						}else if(parser.getName().equalsIgnoreCase("CollectModels")
								&& listCollectName != null){
							xmlBizCode = "";
							xmlModels = "";
						}
						break;
					default:
						break;
					}
					// ���xmlû�н����򵼺�����һ���ڵ�
					evtType = parser.next();
				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return listCollectName;
	}
	
	/** 
     * ���豸��� �ο�Դ�룺platform/packages/apps/Settings.git 
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java 
     */  
    static public boolean createBond(Class<?> btClass,BluetoothDevice btDevice) throws Exception {  
        Method createBondMethod = btClass.getMethod("createBond");  
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    }  

    /** 
     * ���豸������ �ο�Դ�룺platform/packages/apps/Settings.git 
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java 
     */  
    static public boolean removeBond(Class<?> btClass,BluetoothDevice btDevice) throws Exception {  
        Method removeBondMethod = btClass.getMethod("removeBond");  
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);  
        return returnValue.booleanValue();  
    }  

    static public boolean setPin(Class<?> btClass, BluetoothDevice btDevice,  
            String str) throws Exception {
    	 Boolean returnValue = false;
        try {             
            Method removeBondMethod = btClass.getDeclaredMethod("setPin",  
                    new Class[] { byte[].class });  
            returnValue = (Boolean) removeBondMethod.invoke(btDevice,  
            new Object[] { str.getBytes() });  
            Log.e("returnValue", "" + returnValue);  
        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IllegalArgumentException e) {  
            e.printStackTrace();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return returnValue;  

    }  
    
 // ȡ���û�����
 	static public boolean cancelPairingUserInput(Class btClass,
 			BluetoothDevice device)
 	throws Exception
 	{
 		Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
 		// cancelBondProcess()
 		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
 		return returnValue.booleanValue();
 	}
}
