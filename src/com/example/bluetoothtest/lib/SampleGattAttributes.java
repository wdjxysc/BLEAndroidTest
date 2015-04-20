/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetoothtest.lib;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
	/****************************用于体重称 体脂称 以及血压传输****************************/
    private static HashMap<String, String> attributes = new HashMap();
    public static String General_RATE_MEASUREMENT = "0000FFF4-0000-1000-8000-00805f9b34fb";//"00002a37-0000-1000-8000-00805f9b34fb";
    /********************************用于体温的数据传输********************************/
    private static HashMap<String, String> attributesTiWen = new HashMap();
    public static String TIWEN_RATE_MEASUREMENT = "0000FFF2-0000-1000-8000-00805f9b34fb";
    /****************************************************************************/
    public static String HEART_RATE_MEASUREMENT2 = "00008002-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(General_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }
    static {
        // Sample Services.
    	attributesTiWen.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
    	attributesTiWen.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
    	attributesTiWen.put(TIWEN_RATE_MEASUREMENT, "Heart Rate Measurement");
    	attributesTiWen.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
    public static String lookupTiWen(String uuid, String defaultName) {
        String name = attributesTiWen.get(uuid);
        return name == null ? defaultName : name;
    }
}