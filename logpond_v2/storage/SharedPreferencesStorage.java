package com.infocomm.logpond_v2.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesStorage {

	public static final String IS_NEW_INSTALLED = "IS_NEW_INSTALLED";
	public static final String USERNAME = "USERNAME";
	public static final String SERVER_HOST = "SERVER_HOST";
	public static final String FTP_HOST = "FTP_HOST";
	public static final String FTP_PORT = "FTP_PORT";
	public static final String FTP_USERNAME = "FTP_USERNAME";
	public static final String FTP_PASSWORD= "FTP_PASSWORD";
	public static final String AUTO_SYNC_ACTION = "AUTO_SYNC_ACTION";
	public static final String IS_AUTO_SYNC_RUNNING= "IS_AUTO_SYNC_RUNNING";
	public static final String IS_AUTO_SYNC_FINISHED= "IS_AUTO_SYNC_FINISHED";

	public static String getStringValue(Context context, String key){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(key.equalsIgnoreCase(USERNAME)){
			return appPreferences.getString(key, "DEMO");
		}else if(key.equalsIgnoreCase(SERVER_HOST)){
			return appPreferences.getString(key, "http://infocomm.homelinux.com:8830/Lms");
		}else if(key.equalsIgnoreCase(FTP_HOST)){
			return appPreferences.getString(key, "infocomm.homelinux.com");
		}else if(key.equalsIgnoreCase(FTP_USERNAME)){
			return appPreferences.getString(key, "demo");
		}else if(key.equalsIgnoreCase(FTP_PASSWORD)){
			return appPreferences.getString(key, "demo");
		}
		return appPreferences.getString(key, "");
	}

	public static boolean getBooleanValue(Context context, String key){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(key.equalsIgnoreCase(IS_NEW_INSTALLED) || key.equalsIgnoreCase(IS_AUTO_SYNC_FINISHED)) 	return appPreferences.getBoolean(key, true);
		return appPreferences.getBoolean(key, false);
	}

	public static double geDoubleValue(Context context, String key){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return appPreferences.getFloat(key, (float) 0.0);
	}

	public static int getIntValue(Context context, String key){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		if(key.equalsIgnoreCase(FTP_PORT) )	 return appPreferences.getInt(key, 21);
		return appPreferences.getInt(key, 0);
	}

	public static long getLongValue(Context context, String key){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return appPreferences.getLong(key, 0);
	}

	public static void setStringValue(Context context, String key, String value){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = appPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void setBooleanValue(Context context, String key, boolean value){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = appPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static void setIntValue(Context context, String key, int value){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = appPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static void setDoubleValue(Context context, String key, double value){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = appPreferences.edit();
		editor.putFloat(key, (float) value);
		editor.commit();
	}

	public static void setLongValue(Context context, String key, long value){
		SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = appPreferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}
}
