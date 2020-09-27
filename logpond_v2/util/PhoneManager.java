package com.infocomm.logpond_v2.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

public class PhoneManager {

	/**
	 * Unique id of the device
	 * @return IMEI
	 */
	public static String getImei(Activity activity){
		TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	/**
	 * Get the network operator name
	 * @return network operator name
	 */
	public static String getNetworkOperatorName(Activity activity){
		TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		String networkOperatorName = telephonyManager.getNetworkOperatorName();
		networkOperatorName = networkOperatorName.toUpperCase();
		return networkOperatorName;
	}
	
	
	/**
	 * Get the os release version
	 * @return
	 */
	public static String getVersionRelease(){
		return android.os.Build.VERSION.RELEASE;
	}
	
	/**
	 * Get the SDK version
	 * @return
	 */
	public static String getVersionSDK(){
		return android.os.Build.VERSION.SDK;
	}
	
	/**
	 * Get the device brand
	 * @return
	 */
	public static String getBrand(){
		return android.os.Build.BRAND;
	}
	
	/**
	 * Get the device model
	 * @return
	 */
	public static String getModel(){
		return android.os.Build.MODEL;
	}
	
	/**
	 * Get the device product
	 * @return
	 */
	public static String getProduct(){
		return android.os.Build.PRODUCT;
	}
	
	/**
	 * Get the msisdn
	 * @return
	 */
	public static String getMsisdn(Activity activity){
		try{
			TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
			return telephonyManager.getLine1Number();
		}catch(Exception e){
			return null;
		}		
	}
	
	public static String getBTS(Activity activity){
		try{
			/*
			TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
			List<NeighboringCellInfo> NeighboringList = telephonyManager.getNeighboringCellInfo();
			System.out.println("~~~~~~~~~~~~~~size=" + NeighboringList.size());
			String retStr="";
			for (int i = 0; i < NeighboringList.size(); i++) { 
			   int cid = NeighboringList.get(i).getCid();
			   int lac = NeighboringList.get(i).getLac();   
			   System.out.println("~~~~~~~~~~~~~~cid=" + cid);
			   System.out.println("~~~~~~~~~~~~~~lac=" + lac);
			   Log.d("cid", String.valueOf(cid));
			   Log.d("lac", String.valueOf(lac));
			}
			*/

			final TelephonyManager telephony = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
				final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
				if (location != null) {
					//msg.setText("LAC: " + location.getLac() + " CID: " + location.getCid());
					int CID = location.getCid();  
				    int LAC = location.getLac();  
				    System.out.println("~~~~~~~~~~~~~~cid=" + CID);
				    System.out.println("~~~~~~~~~~~~~~lac=" + LAC);
				    Log.d("cid", String.valueOf(CID));
				    Log.d("lac", String.valueOf(LAC));
				    Toast output = Toast.makeText(activity, "Base station LAC is "+LAC+"\n"
				      +"Base station CID is " +CID, Toast.LENGTH_SHORT);
				    output.show();
				}
			}else if(telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA){
			 	CdmaCellLocation location = (CdmaCellLocation) telephony.getCellLocation();
				if (location != null) {
					//msg.setText("LAC: " + location.getLac() + " CID: " + location.getCid());
					int CID = location.getBaseStationId(); 
				    int lat = location.getBaseStationLatitude();
				    int longitude = location.getBaseStationLongitude();
				    System.out.println("~~~~~~~~~~~~~~lat=" + lat);
				    System.out.println("~~~~~~~~~~~~~~longitude=" + longitude);
				    Log.d("cid", String.valueOf(CID));
				}
			}
			//GsmCellLocation xXx = new GsmCellLocation();  
		    
			return "";
		}catch(Exception e){
			return null;
		}		
	}
	
	public static boolean isLocationServiceEnabled(Context context){
		LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		boolean gps_enabled = false;
		boolean network_enabled = false;

		try {
		    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch(Exception ex) {}

		try {
		    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch(Exception ex) {}
		return gps_enabled || network_enabled;
	}

	public static boolean isGPSEnabled(Context context){
		LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}


	/**
	 * Checking for all possible internet providers
	 * **/
	public static boolean isConnectingToInternet(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Check if there is any connectivity to a Wifi network
	 * @param context
	 * @param type
	 * @return
	 */
	public static boolean isConnectedWifi(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
	}

	/**
	 * Check if there is any connectivity to a mobile network
	 * @param context
	 * @param type
	 * @return
	 */
	public static boolean isConnectedMobileNetwork(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	public static Boolean isMobileNetworkAvailable(Context context) {
		TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return ((tel.getNetworkOperator() != null && tel.getNetworkOperator().equals("")) ? false : true);
	}

	public static int getWifiSignal(Context context){
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		//int linkSpeed = wifiManager.getConnectionInfo().getRssi();
		int numberOfLevels = 5;
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
		System.out.println("Wifi Signal Level is " + level + " out of 5");
		return level;
	}

	public static int getMobileNetworkSignal(Context context){
		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		if(telephonyManager.getAllCellInfo() != null){
			for (final CellInfo info : telephonyManager.getAllCellInfo()) {
				if (info instanceof CellInfoGsm) {
					final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
					return gsm.getLevel();

				} else if (info instanceof CellInfoCdma) {
					final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
					return cdma.getLevel();

				} else if (info instanceof CellInfoLte) {
					final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
					return lte.getLevel();
				}
			}
		}

		/*
		CellInfoGsm cellinfogsm = (CellInfoGsm)telephonyManager.getAllCellInfo().get(0);
		CellSignalStrengthGsm cellSignalStrengthGsm = cellinfogsm.getCellSignalStrength();
		return cellSignalStrengthGsm.getLevel();
		*/
		return -1;
	}

	/**
	 * Check if there is fast connectivity
	 * @param context
	 * @return
	 */
	public static boolean isConnectedFast(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected() && PhoneManager.isConnectionFast(networkInfo.getType(),networkInfo.getSubtype()));
	}

	/**
	 * Check if the connection is fast
	 * @param type
	 * @param subType
	 * @return
	 */
	public static boolean isConnectionFast(int type, int subType){
		if(type==ConnectivityManager.TYPE_WIFI){
			return true;
		}else if(type==ConnectivityManager.TYPE_MOBILE){
			switch(subType){
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					return false; // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_CDMA:
					return false; // ~ 14-64 kbps
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return false; // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					return true; // ~ 400-1000 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					return true; // ~ 600-1400 kbps
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return false; // ~ 100 kbps
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					return true; // ~ 2-14 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPA:
					return true; // ~ 700-1700 kbps
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					return true; // ~ 1-23 Mbps
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
				case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
					return true; // ~ 1-2 Mbps
				case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
					return true; // ~ 5 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
					return true; // ~ 10-20 Mbps
				case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
					return false; // ~25 kbps
				case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
					return true; // ~ 10+ Mbps
				// Unknown
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				default:
					return false;
			}
		}else{
			return false;
		}
	}

	public static int getBatteryLevel(Context context){
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, intentFilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		double batteryLevel = level / (double) scale *100;
		return (int) batteryLevel;
	}

	public static int getBatteryStatus(Context context) {
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent intent = context.registerReceiver(null, intentFilter);
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		String batteryStatus = new String();
		if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
			batteryStatus = "CHARGING";
			return 1;

		} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
			batteryStatus = "DISCHARGING";

		} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
			batteryStatus = "FULL";
		}
		return 0;
	}
}
