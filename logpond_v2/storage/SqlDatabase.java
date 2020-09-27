package com.infocomm.logpond_v2.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONObject;

import java.util.regex.Pattern;

public class SqlDatabase {

	private static final String DATABASE_NAME = "LOGPOND";

	private static final String INVENTORY_TABLE = "INVENTORY";
	private static int INVENTORY_TABLE_COLUMN_COUNT = 34;
    /*
	private static final String CREATE_INVENTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + INVENTORY_TABLE
        								   		   		+ " (label_number VARCHAR, species_code VARCHAR, boom_voet_1 VARCHAR, boom_voet_2 VARCHAR, boom_top_1 VARCHAR, boom_top_2 VARCHAR, "
														+	"boom_length VARCHAR, boom_diameters VARCHAR, boom_volume VARCHAR, boom_h VARCHAR, boom_r VARCHAR, old_label_number VARCHAR, "
														+	"mother_label_number VARCHAR, logpond_name VARCHAR, logpond_pilee VARCHAR, logpond_grade VARCHAR, kapregister_no VARCHAR, "
														+	"stamp_time VARCHAR, retribution_no VARCHAR, concession_name VARCHAR, kapvak_no VARCHAR, parcel_no VARCHAR, co_id_no VARCHAR, "
														+	"pv_no VARCHAR, log_resource VARCHAR, buyer_reject VARCHAR, status VARCHAR);";

												*/
    				/*
    private static final String CREATE_INVENTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + INVENTORY_TABLE
                                                        + " (label_number VARCHAR, species_code VARCHAR, boom_voet_1 VARCHAR, boom_voet_2 VARCHAR, boom_top_1 VARCHAR, boom_top_2 VARCHAR, "
                                                        +	"boom_length VARCHAR, boom_diameters VARCHAR, boom_volume VARCHAR, boom_h VARCHAR, boom_r VARCHAR, old_label_number VARCHAR, "
                                                        +	"mother_label_number VARCHAR, logpond_name VARCHAR, logpond_pile VARCHAR, logpond_grade VARCHAR, kapregister_no VARCHAR, "
                                                        +	"stamp_date VARCHAR, retribution_no VARCHAR, concession_name VARCHAR, kapvak_no VARCHAR, parcel_no VARCHAR, co_id_no VARCHAR, "
                                                        +	"pv_no VARCHAR, license_no VARCHAR, log_resource VARCHAR, buyer_reject VARCHAR, buyer_allocation VARCHAR, container VARCHAR, status VARCHAR);";
	*/

	private static final String CREATE_INVENTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + INVENTORY_TABLE
			+ " (label_number VARCHAR, species_code VARCHAR, boom_voet_1 VARCHAR, boom_voet_2 VARCHAR, boom_top_1 VARCHAR, boom_top_2 VARCHAR, "
			+	"boom_length VARCHAR, boom_diameters VARCHAR, boom_volume VARCHAR, boom_h VARCHAR, boom_r VARCHAR, old_label_number VARCHAR, "
			+	"mother_label_number VARCHAR, logpond_name VARCHAR, logpond_pile VARCHAR, logpond_grade VARCHAR, kapregister_no VARCHAR, "
			+	"stamp_date VARCHAR, retribution_no VARCHAR, concession_name VARCHAR, kapvak_no VARCHAR, parcel_no VARCHAR, co_id_no VARCHAR, "
			+	"pv_no VARCHAR, pv_inspection_date VARCHAR, pv_expire_date VARCHAR, export_vergunning VARCHAR, export_no VARCHAR, license_no VARCHAR, "
			+	"buyer_reject VARCHAR, buyer_allocation VARCHAR, container_allocation VARCHAR, log_resource VARCHAR, status VARCHAR);";

	/**
	 * Open the database
	 * If doesn't exist then create the database
	 */
	public static SQLiteDatabase openDatabase(Context context){
		SQLiteDatabase db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
		return db;
	}

	public static boolean isColumnCountMatch(Context context){
		Cursor cursor = null;
		SQLiteDatabase db = openDatabase(context);
		SQLiteStatement sqlLiteStmt = null;
		try{
			String stmt =  "SELECT * FROM INVENTORY";
			cursor  =   db.rawQuery(stmt, null);
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~cursor.getColumnCount()=" + cursor.getColumnCount());
			return cursor.getColumnCount() == INVENTORY_TABLE_COLUMN_COUNT;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(sqlLiteStmt!=null) sqlLiteStmt.close();
			if(cursor!=null&&!cursor.isClosed()) cursor.close();
			if(db.isOpen()) db.close();
		}
		return false;
	}

	public static void insertInventory(Context context, String[] dataArray) throws Exception{
		Cursor cursor = null;
		SQLiteDatabase db = openDatabase(context);
		SQLiteStatement sqlLiteStmt = null;
		try{
			db.execSQL(CREATE_INVENTORY_TABLE);
			String questionMark = "";
			for(int i=0;i<INVENTORY_TABLE_COLUMN_COUNT;i++){
				if(questionMark.length()>0) questionMark += ", ";
				questionMark += "?";
			}
			String stmt =  "INSERT INTO INVENTORY VALUES(" + questionMark + ")";
			sqlLiteStmt = db.compileStatement(stmt);
			db.beginTransaction();
			try{
				for(int i=0;i<dataArray.length;i++){
					String[] columnArray = dataArray[i].split(Pattern.quote("^"), -1);
					for(int j=0;j<INVENTORY_TABLE_COLUMN_COUNT;j++){
						if(j<columnArray.length){
							sqlLiteStmt.bindString(j + 1, columnArray[j]);
						}else{
							sqlLiteStmt.bindString(j + 1, "");
						}
					}
					sqlLiteStmt.executeInsert();
				}
				db.setTransactionSuccessful();
			}finally {
				db.endTransaction();
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Insert Inventory DB Error : " + e.getMessage());
		}finally{
			if(sqlLiteStmt!=null) sqlLiteStmt.close();
			if(cursor!=null&&!cursor.isClosed()) cursor.close();
			if(db.isOpen()) db.close();
		}
	}

	public static boolean emptyInventory(Context context){
		Cursor cursor = null;
		SQLiteDatabase db = openDatabase(context);
		SQLiteStatement sqlLiteStmt = null;
		try{
			String stmt =  "DROP TABLE INVENTORY";
			sqlLiteStmt = db.compileStatement(stmt);
			sqlLiteStmt.executeUpdateDelete();
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(sqlLiteStmt!=null) sqlLiteStmt.close();
			if(cursor!=null&&!cursor.isClosed()) cursor.close();
			if(db.isOpen()) db.close();
		}
		return false;
	}

	public static int queryInventory(Context context){
		int count = 0;
		Cursor cursor = null;
		SQLiteDatabase db = openDatabase(context);
		SQLiteStatement sqlLiteStmt = null;
		try{
			db.execSQL(CREATE_INVENTORY_TABLE);
			String stmt =  "SELECT * FROM INVENTORY";
			cursor  =   db.rawQuery(stmt, null);
			if(cursor.moveToFirst()){
				do{
					String labelNumber = cursor.getString(cursor.getColumnIndex("label_number"));
					Log.i("Label Number", labelNumber);
					count++;
				}while (cursor.moveToNext());
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(sqlLiteStmt!=null) sqlLiteStmt.close();
			if(cursor!=null&&!cursor.isClosed()) cursor.close();
			if(db.isOpen()) db.close();
		}
		return count;
	}

	public static boolean isInventoryTableEmpty(Context context){
		int count = 0;
		Cursor cursor = null;
		SQLiteDatabase db = openDatabase(context);
		SQLiteStatement sqlLiteStmt = null;
		try{
			db.execSQL(CREATE_INVENTORY_TABLE);
			String stmt =  "SELECT Count(label_number) as total FROM INVENTORY";
			cursor  =   db.rawQuery(stmt, null);
			if(cursor.moveToFirst()){
				count = cursor.getInt(cursor.getColumnIndex("total"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(sqlLiteStmt!=null) sqlLiteStmt.close();
			if(cursor!=null&&!cursor.isClosed()) cursor.close();
			if(db.isOpen()) db.close();
		}
		return count==0;
	}

	public static JSONObject queryInventoryDetailsByLabelNumber(Context context, String labelNumber){
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~queryInventoryDetailsByLabelNumber = " + labelNumber);
		JSONObject jsonObject = new JSONObject();
		Cursor cursor = null;
		SQLiteDatabase db = openDatabase(context);
		SQLiteStatement sqlLiteStmt = null;
		try{
			db.execSQL(CREATE_INVENTORY_TABLE);
			String stmt = "SELECT * FROM INVENTORY WHERE label_number = ?";
			cursor =  db.rawQuery(stmt, new String[]{labelNumber});
			if(cursor.moveToFirst()){
				String speciesCode = cursor.getString(cursor.getColumnIndex("species_code"));
				String boomVoet1 = cursor.getString(cursor.getColumnIndex("boom_voet_1"));
				String boomVoet2 = cursor.getString(cursor.getColumnIndex("boom_voet_2"));
				String boomTop1 = cursor.getString(cursor.getColumnIndex("boom_top_1"));
				String boomTop2 = cursor.getString(cursor.getColumnIndex("boom_top_2"));
				String boomLength = cursor.getString(cursor.getColumnIndex("boom_length"));
				String boomDiameters = cursor.getString(cursor.getColumnIndex("boom_diameters"));
				String boomVolume = cursor.getString(cursor.getColumnIndex("boom_volume"));
				String boomH = cursor.getString(cursor.getColumnIndex("boom_h"));
				String boomR = cursor.getString(cursor.getColumnIndex("boom_r"));
				String oldLabelNo = cursor.getString(cursor.getColumnIndex("old_label_number"));
				String motherLabelNo = cursor.getString(cursor.getColumnIndex("mother_label_number"));
				String logpondName = cursor.getString(cursor.getColumnIndex("logpond_name"));
				String logpondPile = cursor.getString(cursor.getColumnIndex("logpond_pile"));
				String logpondGrade = cursor.getString(cursor.getColumnIndex("logpond_grade"));
				String kapregisterNo = cursor.getString(cursor.getColumnIndex("kapregister_no"));
				String stampTime = cursor.getString(cursor.getColumnIndex("stamp_date"));
				String retributionNo = cursor.getString(cursor.getColumnIndex("retribution_no"));
				String concessionName = cursor.getString(cursor.getColumnIndex("concession_name"));
				String kapvakNo = cursor.getString(cursor.getColumnIndex("kapvak_no"));
				String parcelNo = cursor.getString(cursor.getColumnIndex("parcel_no"));
				String coIdNo = cursor.getString(cursor.getColumnIndex("co_id_no"));
				String pvNo = cursor.getString(cursor.getColumnIndex("pv_no"));
				String logResource = cursor.getString(cursor.getColumnIndex("log_resource"));
				String buyerReject = cursor.getString(cursor.getColumnIndex("buyer_reject"));
				String status = cursor.getString(cursor.getColumnIndex("status"));

				String licenseNo = cursor.getString(cursor.getColumnIndex("license_no"));
				String buyerAllocation = cursor.getString(cursor.getColumnIndex("buyer_allocation"));
				String container = cursor.getString(cursor.getColumnIndex("container_allocation"));

				String pvInspectionDate = cursor.getString(cursor.getColumnIndex("pv_inspection_date"));
				String pvExpireDate = cursor.getString(cursor.getColumnIndex("pv_expire_date"));
				String exportVergunning = cursor.getString(cursor.getColumnIndex("export_vergunning"));
				String exportNo = cursor.getString(cursor.getColumnIndex("export_no"));

				jsonObject.put("labelNumber", labelNumber);
				jsonObject.put("speciesCode", speciesCode);
				jsonObject.put("boomVoet1", boomVoet1);
				jsonObject.put("boomVoet2", boomVoet2);
				jsonObject.put("boomTop1", boomTop1);
				jsonObject.put("boomTop2", boomTop2);
				jsonObject.put("boomLength", boomLength);
				jsonObject.put("boomDiameters", boomDiameters);
				jsonObject.put("boomVolume", boomVolume);
				jsonObject.put("boomH", boomH);
				jsonObject.put("boomR", boomR);
				jsonObject.put("oldLabelNo", oldLabelNo);
				jsonObject.put("motherLabelNo", motherLabelNo);
				jsonObject.put("logpondName", logpondName);
				jsonObject.put("logpondPile", logpondPile);
				jsonObject.put("logpondGrade", logpondGrade);
				jsonObject.put("kapregisterNo", kapregisterNo);
				jsonObject.put("stampTime", stampTime);
				jsonObject.put("retributionNo", retributionNo);
				jsonObject.put("concessionName", concessionName);
				jsonObject.put("kapvakNo", kapvakNo);
				jsonObject.put("parcelNo", parcelNo);
				jsonObject.put("coIdNo", coIdNo);
				jsonObject.put("pvNo", pvNo);
				jsonObject.put("logResource", logResource);
				jsonObject.put("buyerReject", buyerReject);
				jsonObject.put("status", status);
				jsonObject.put("licenseNo", licenseNo);
				jsonObject.put("buyerAllocation", buyerAllocation);
				jsonObject.put("container", container);

				jsonObject.put("pvInspectionDate", pvInspectionDate);
				jsonObject.put("pvExpireDate", pvExpireDate);
				jsonObject.put("exportVergunning", exportVergunning);
				jsonObject.put("exportNo", exportNo);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(sqlLiteStmt!=null) sqlLiteStmt.close();
			if(cursor!=null&&!cursor.isClosed()) cursor.close();
			if(db.isOpen()) db.close();
		}
		return jsonObject;
	}

	public static long getMaximumDBSize(Context context){
		long maximumDBSize = 0;
		SQLiteDatabase db = openDatabase(context);
		try{
			maximumDBSize = db.getMaximumSize();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(db.isOpen()) db.close();
		}
		return maximumDBSize;
	}

	/**
	 * Delete the database
	 * @param context
	 */
	public static void deleteDatabase(Context context){
		try{
			context.deleteDatabase(DATABASE_NAME);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
