/*
 * Copyright (C) 2017 chodison <c_soft_dev@163.com>
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
package com.chodison.mybreakpad.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import android.media.CamcorderProfile;
import android.os.Build;
import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.chodison.mybreakpad.progma.DebugLog;

public class ABuildHelper {

    public static int FROYO = 8; // June 2010: Android 2.2
    public static int GINGERBREAD = 9; // November 2010: Android 2.3
    public static int GINGERBREAD_MR1 = 10; // February 2011: Android 2.3.3.
    public static int HONEYCOMB = 11; // February 2011: Android 3.0.
    public static int HONEYCOMB_MR1 = 12; // May 2011: Android 3.1.
    public static int HONEYCOMB_MR2 = 13; // June 2011: Android 3.2.
    public static int ICE_CREAM_SANDWICH = 14; // October 2011: Android 4.0.
    public static int ICE_CREAM_SANDWICH_MR1 = 15; // Android 4.0.3.
    public static int JELLY_BEAN = 16; // Android 4.1.
    public static int JELLY_BEAN_MR1 = 17; // Android 4.2.
    public static int JELLY_BEAN_MR2 = 18; // Android 4.3.
    public static int KITKAT = 19; // Android 4.4.
    public static int KITKAT_MR1 = 20; // Android 4.4W.
    public static int LOLLIPOP = 21; // Android 5.0.
    public static int LOLLIPOP_MR1 = 22; // Android 5.1.
    public static int ANDROID_M = 23; // Android 6.0.
    public static int ANDROID_N = 24; // Android 7.0.
    public static int ANDROID_N_MR1 = 25; // Android 7.1.
    public static int ANDROID_O = 26; // Android 8.0.
    public static int ANDROID_UNKNOWN = 27; // Unknown

    public static final String ABI_ARMv7a = "armeabi-v7a";
    public static final String ABI_ARM = "armeabi";
    public static final String ABI_X86 = "x86";
    
    public static final String getModel() {
        return Build.MODEL;
    }
    
    public static final String getManufacturer() {
    	return Build.MANUFACTURER;
    }
    
    public static final int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static final boolean isApi9_GingerBread() {
        int sdk = getSDKVersion();
        return sdk >= FROYO && sdk <= GINGERBREAD_MR1;
    }

    public static final boolean isApi9_GingerBreadOrLater() {
        int sdk = getSDKVersion();
        return sdk >= GINGERBREAD;
    }

    public static final boolean isApi11_HoneyComb() {
        int sdk = getSDKVersion();
        return sdk >= HONEYCOMB && sdk <= HONEYCOMB_MR2;
    }

    public static final boolean isApi11_HoneyCombOrLater() {
        return getSDKVersion() >= HONEYCOMB;
    }

    public static final boolean isIceCreamSandwich() {
        int sdk = getSDKVersion();
        return sdk >= ICE_CREAM_SANDWICH && sdk <= ICE_CREAM_SANDWICH_MR1;
    }
    
    public static final boolean isApi14_IceCreamSandwich() {
        int sdk = getSDKVersion();
        return sdk == ICE_CREAM_SANDWICH && sdk <= ICE_CREAM_SANDWICH_MR1;
    }

    public static final boolean isApi14_IceCreamSandwichOrLater() {
        return getSDKVersion() >= ICE_CREAM_SANDWICH;
    }

    public static final boolean isApi16_JellyBean() {
        int sdk = getSDKVersion();
        return sdk == JELLY_BEAN;
    }

    public static final boolean isApi16_JellyBeanOrLater() {
        return getSDKVersion() >= JELLY_BEAN;
    }
    
    public static final boolean isApi17_JellyBeanMR1() {
        int sdk = getSDKVersion();
        return sdk == JELLY_BEAN_MR1;
    }
    
    public static final boolean isApi17_JellyBeanMR1OrLater() {
        return getSDKVersion() >= JELLY_BEAN_MR1;
    }
    
    public static final boolean isApi18_JellyBeanMR2() {
        int sdk = getSDKVersion();
        return sdk == JELLY_BEAN_MR2;
    }
    
    public static final boolean isApiLess18_JellyBeanMR2() {
        return getSDKVersion() < JELLY_BEAN_MR2;
    }
 
    public static final boolean isApi18_JellyBeanMR2OrLater() {
        return getSDKVersion() >= JELLY_BEAN_MR2;
    }
    
    public static final boolean isApi19_KitKat() {
        int sdk = getSDKVersion();
        return sdk == KITKAT;
    }
    
    public static final boolean isApi19_KitKatOrLater() {
        return getSDKVersion() >= KITKAT;
    }

    public static final boolean isApi20_KitKatMR1() {
        int sdk = getSDKVersion();
        return sdk == KITKAT_MR1;
    }
    
    public static final boolean isApi20_KitKatMR1OrLater() {
        return getSDKVersion() >= KITKAT_MR1;
    }
    
    public static final boolean isApi21_LOLLIPOPO() {
        int sdk = getSDKVersion();
        return sdk == LOLLIPOP;
    }
    
    public static final boolean isApi21_LollipopOrLater() {
        return getSDKVersion() >= LOLLIPOP;
    }

    public static final boolean isApi22_LollipopMR1() {
        int sdk = getSDKVersion();
        return sdk == LOLLIPOP_MR1;
    }
    
    public static final boolean isApi22_LollipopMR1OrLater() {
        return getSDKVersion() >= LOLLIPOP_MR1;
    }

    public static final boolean isApi23_AndroidM() {
        int sdk = getSDKVersion();
        return sdk == ANDROID_M;
    }
    
    public static final boolean isApi23_AndroidMOrLater() {
        return getSDKVersion() >= ANDROID_M;
    }

    public static final boolean isApi24_AndroidN() {
        int sdk = getSDKVersion();
        return sdk == ANDROID_N;
    }

    public static final boolean isApi24_AndroidNOrLater() {
        return getSDKVersion() >= ANDROID_N;
    }

    public static final boolean isApi25_AndroidNMR1() {
        int sdk = getSDKVersion();
        return sdk == ANDROID_N_MR1;
    }

    public static final boolean isApi25_AndroidNMR1OrLater() {
        return getSDKVersion() >= ANDROID_N_MR1;
    }

    public static final boolean isApi26_AndroidO() {
        int sdk = getSDKVersion();
        return sdk == ANDROID_O;
    }

    public static final boolean isApi26_AndroidOOrLater() {
        return getSDKVersion() >= ANDROID_O;
    }

    public static boolean supportARMv7a() {
        return supportABI(ABI_ARMv7a);
    }

    public static boolean isIntelMediaCodec() {
        return  findMediaCodec("OMX.Intel.");
    }
    public static boolean findMediaCodec(String codecname){
        boolean isHardcode = false;
        //读取系统配置文件/system/etc/media_codecc.xml
        File file = new File("/system/etc/media_codecs.xml");
        InputStream inFile = null;
        try {
            inFile = new FileInputStream(file);
        } catch (Exception e) {
            DebugLog.i("ABuildHelper", "findMediaCodec : " + codecname +"FileInputStream failed");
        }

        if(inFile != null) {
            XmlPullParserFactory pullFactory;
            try {
                pullFactory = XmlPullParserFactory.newInstance();
                XmlPullParser xmlPullParser = pullFactory.newPullParser();
                xmlPullParser.setInput(inFile, "UTF-8");
                int eventType = xmlPullParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = xmlPullParser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if ("MediaCodec".equals(tagName)) {
                                String componentName = xmlPullParser.getAttributeValue(0);

                                if(componentName.startsWith("OMX."))
                                {
                                    if(componentName.startsWith(codecname))
                                    {
                                        DebugLog.i("ABuildHelper", "findMediaCodec : " + componentName );
                                        isHardcode = true;
                                    }
                                }
                            }
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (Exception e) {
                DebugLog.i("ABuildHelper", "findMediaCodec : " + codecname +"parser failed");
            }
            //文件关闭
            try {
                inFile.close();
            } catch (Exception e) {
                DebugLog.i("ABuildHelper", "file close failed");
            }
        }
        return isHardcode;
    }

    public static boolean supportX86() {
        boolean isSupportX86 = supportABI(ABI_X86);
        if(!isSupportX86 && isApi16_JellyBeanOrLater()) {
            isSupportX86 = isIntelMediaCodec();
            if(isSupportX86) {
                DebugLog.i("ABuildHelper", "isIntelMediaCodec");
            }
        }
        return isSupportX86;
    }

    public static boolean supportARM() {
        return supportABI(ABI_ARM);
    }

    public static boolean supportABI(String requestAbi) {
        String abi = get_CPU_ABI();
        if (!TextUtils.isEmpty(abi) && abi.equalsIgnoreCase(requestAbi))
            return true;

        String abi2 = get_CPU_ABI2();
        if (!TextUtils.isEmpty(abi2) && abi.equalsIgnoreCase(requestAbi))
            return true;

        return false;
    }

    public static final String getParsedCpuAbiInfo() {
        StringBuilder cpuAbiInfo = new StringBuilder();
        String cpuAbi = ABuildHelper.get_CPU_ABI();
        if (!TextUtils.isEmpty(cpuAbi)) {
            cpuAbiInfo.append("CPU ABI : ");
            cpuAbiInfo.append(cpuAbi);
            cpuAbiInfo.append("\n");
        }

        String cpuAbi2 = ABuildHelper.get_CPU_ABI2();
        if (!TextUtils.isEmpty(cpuAbi)) {
            cpuAbiInfo.append("CPU ABI2 : ");
            cpuAbiInfo.append(cpuAbi2);
            cpuAbiInfo.append("\n");
        }

        return cpuAbiInfo.toString();
    }

    public static final String get_CPU_ABI() {
        return Build.CPU_ABI;
    }

    public static final String get_CPU_ABI2() {
        try {
            Field field = Build.class.getDeclaredField("CPU_ABI2");
            if (field == null)
                return null;

            Object fieldValue = field.get(null);
            if (field == null || !(fieldValue instanceof String)) {
                return null;
            }

            return (String) fieldValue;
        } catch (Exception e) {

        }

        return null;
    }
    
    //samsung NOTE3(GT-N900X),samsung NOTE2(GT-N710x),android 4.0
    public static boolean getNeedSpecialSurface(){
    	String model = Build.MODEL;
		if(	model.startsWith("SM-N900") ||
    		model.startsWith("GT-N710") ||
    		Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH ||
    		Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
    			return true;
    		}
		
    	return false;
    }
    
    //support max resolution width
    public static int getDecodeSupportMaxWidth(){
		CamcorderProfile profile = CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH);
		if(profile != null){
        	return profile.videoFrameWidth;					
		}
    	else{
	    	String model = Build.MODEL;
	    	if(	model.startsWith("SM-N900"))
	    		return 1920;
	    	if(	model.equals("HTC Desire X"))
	    		return 800;
	    	return 0;
    	}
    }
    
    //support max resolution height
    public static int getDecodeSupportMaxHeight(){
		CamcorderProfile profile = CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH);
    	if(profile != null){
        	return profile.videoFrameHeight;	
		} else{
	    	String model = Build.MODEL;
	    	if(	model.startsWith("SM-N900"))
	    		return 1080;
	    	if(	model.equals("HTC Desire X"))
	    		return 480;
	    	return 0;
		}
    }
    
    public static int getNumCores(){
    	return Runtime.getRuntime().availableProcessors();
    }
    
    public static int getCpuFrequence() {  
        ProcessBuilder cmd;  
        try {  
            String[] args = { "/system/bin/cat",  
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };  
            cmd = new ProcessBuilder(args);  
  
            Process process = cmd.start();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(  
                    process.getInputStream()));  
            String line = reader.readLine();  
            return Integer.parseInt(line);
        } catch (IOException ex) {  
            ex.printStackTrace();  
        }  
        return 0;  
    }  
}
