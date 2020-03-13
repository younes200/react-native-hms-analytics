/**
Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 

   Licensed under the Apache License, Version 2.0 (the "License");

   you may not use this file except in compliance with the License.

   You may obtain a copy of the License at

 

     http://www.apache.org/licenses/LICENSE-2.0

 

   Unless required by applicable law or agreed to in writing, software

   distributed under the License is distributed on an "AS IS" BASIS,

   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

   See the License for the specific language governing permissions and

   limitations under the License.
*/

package com.huawei.hianalytics;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsTools;
import com.huawei.hms.analytics.type.HAEventType;
import com.huawei.hms.analytics.type.HAParamType;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HaInterfaceModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final HiAnalyticsInstance instance;
    private final String TAG="RNHASDK";
    private Map<String, Object> haParamTypeMap;

    public HaInterfaceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.i( "RNLifeCycle","Module construct");
        LifeCycleListener.init((Application) reactContext.getApplicationContext());
        this.reactContext = reactContext;
        this.instance=HiAnalytics.getInstance(reactContext);
        haParamTypeMap=getHAParams();
    }

    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        return getHAEventType(getHAParams(constants));
    }

    private Map<String, Object> getHAParams(Map<String, Object> map){
        Map<String, Object> transMap=map;
        Field[] fields = HAParamType.class.getDeclaredFields();
        if(fields != null){
            for(Field fie : fields){
                try {
                    transMap.put(fie.getName(),fie.get(fie.getName()));
                    Log.i(TAG,fie.getName()+": "+fie.get(fie.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return transMap;
    }



    /**
	* open console log, default level: debug
     */
    @ReactMethod
    public void enableLog(){
        Log.i("TAG","enable log, default level.");
        HiAnalyticsTools.enableLog();
    }

    /**
	* open console log with level
    * @param level level of log，ranges:“debug”、“info”、“warn”和“error”
     */
    @ReactMethod
    public void enableLogWithLevel(String level) {
        if (!level.equals("debug") && !level.equals("info") && !level.equals("warn") && !level.equals("error")) {
            Log.i("TAG", "enable log, parameter is wrong.");
            return;
        }

        switch (level) {
            case "debug":
                Log.i("TAG", "enable log, level: debug.");
                HiAnalyticsTools.enableLog(3);
                break;
            case "info":
                Log.i("TAG", "enable log, level: info.");
                HiAnalyticsTools.enableLog(4);
                break;
            case "warn":
                Log.i("TAG", "enable log, level: warn.");
                HiAnalyticsTools.enableLog(5);
                break;
            case "error":
                Log.i("TAG", "enable log, level: error.");
                HiAnalyticsTools.enableLog(6);
                break;
            default:
                HiAnalyticsTools.enableLog(3);
                break;
        }
    }
	    private Map<String, Object> getHAParams(){
        Map<String, Object> transMap=new HashMap<>();
        Field[] fields = HAParamType.class.getDeclaredFields();
        if(fields != null){
            for(Field fie : fields){
                try {
                    transMap.put(fie.getName(),fie.get(fie.getName()));
                    Log.i(TAG,fie.getName()+": "+fie.get(fie.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return transMap;
    }
	
	private Map<String, Object> getHAEventType(Map<String, Object> map){
        Map<String, Object> transMap=map;
        Field[] fields = HAEventType.class.getDeclaredFields();
        if(fields != null){
            for(Field fie : fields){
                try {
                    transMap.put(fie.getName(),fie.get(fie.getName()));
                    Log.i(TAG,fie.getName()+": "+fie.get(fie.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return transMap;
    }

    @Override
    public String getName() {
        return "HaInterface";
    }

    /**
     * report customize event to server
     * @param event The event id
     * @param contentMap The parameters of this event. map can belongs values of type: boolean,double, String and Map.
     */
    @ReactMethod
    public void onEvent(String event,ReadableMap contentMap){
        Log.i( TAG,"onEvent:"+event);
        Bundle bundle=new Bundle();
        saveMapToBundle(contentMap,bundle);
        instance.onEvent(event,bundle);
    }

    private String transToHaParamType(String key){
        if (null!=haParamTypeMap.get(key)){
            Log.i(TAG,"find HAParamType: "+key+", transform to: "+haParamTypeMap.get(key));
            return (String)haParamTypeMap.get(key);
        }else{
            return key;
        }
    }

    private void saveMapToBundle(ReadableMap map,Bundle bundle) {
        if (null != map) {
            ReadableMapKeySetIterator keySetIterator = map.keySetIterator();
            while (keySetIterator.hasNextKey()) {
                String key = keySetIterator.nextKey();
                switch (map.getType(key)) {
                    case Null:
                        //do nothing
                        break;
                    case Boolean:
                        bundle.putBoolean(transToHaParamType(key), map.getBoolean(key));
                        break;
                    case Number:
                        bundle.putDouble(transToHaParamType(key), map.getDouble(key));
                        break;
                    case String:
                        bundle.putString(transToHaParamType(key), map.getString(key));
                        break;
                    case Map:
                        //not supported in AGC
                        break;
                    case Array:
                        //not supported in JAVA
                        break;
                    default: break;
                }
            }
        }
    }
    /**
     * Set up the name of current page, different names can be defined for different pages.
     * @param screenName The name that you want to set up
     * @param screenClassOverride default: MainActivity, your can change it
     */
    @ReactMethod
    public void setCurrentScreen(String screenName,String screenClassOverride){
        Log.i(TAG,"setCurrentScreen: screenName:"+screenName+" currentScreen:"+screenClassOverride);
        instance.setCurrentActivity(getCurrentActivity(),screenName,screenClassOverride);
    }

    /**
     * Set up user ID, use of user ID requires compliance with relevant privacy regulations
     * @param id
     */
    @ReactMethod
    public void setUserId(String id){
        Log.d(TAG,"Set user id, at " + new Date().toString());
        instance.setUserId(id);
    }

    /**
     * Set user properties. User attribute values will remain the same throughout the application lifecycle and session.
     * @param name  property name 
     * @param value  property value
     */
    @ReactMethod
    public void setUserProfile(String name, String value){
        Log.d(TAG, "Set user profile, key: " + name + ", value: " + value);
        instance.setUserProfile(name, value);
    }

    /**
     * Clear all collected data from the local cache
     */
    @ReactMethod
    public void clearCachedData(){
        Log.d(TAG, "Clear cache data at " + new Date().toString());
        instance.clearCachedData();
    }

    /**
     * Get the App instance ID
     * @return
     */
    @ReactMethod
    public void getAAID(final Promise promise){
        final Task<String> myTask = instance.getAAID();

        myTask.addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                final String successStr = s;
                promise.resolve(successStr);
                Log.d(TAG, "Get AAID success!");
            }
        });

        myTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                final String failureStr = "";
                promise.resolve(failureStr);
                Log.e(TAG, "Get AAID failure!");
            }
        });
    }

    /**
     * How long does it take to terminate the current session after switching to the background, the default is 30 seconds.
     * @param milliseconds
     */
    @ReactMethod
    public void setMinActivitySessions(int milliseconds){
        Log.d(TAG, "Set minimum session refresh time: " + milliseconds);
        instance.setMinActivitySessions(milliseconds);
    }

    /**
     * Set the session timeout period. When the time interval between two adjacent events exceeds this threshold, a new session is generated. The default is 30 minutes.
     * @param milliseconds
     */
    @ReactMethod
    public void setSessionDuration(int milliseconds){
        Log.d(TAG, "Set session duration time: " + milliseconds);
        instance.setSessionDuration(milliseconds);
    }

}
