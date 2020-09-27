package com.infocomm.logpond_v2.util;

import android.content.Context;

import com.infocomm.logpond_v2.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CalendarUtil {

	public static String getTodayDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(Calendar.getInstance().getTime());
	}

	public static String getTodayDateInFormat(String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(Calendar.getInstance().getTime());
	}

	public static String getYear() {
		return Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
	}

	public static String changeDateTimeFormatByString(String oldFormat, String newFormat, String datetime){
		SimpleDateFormat oldSdf = new SimpleDateFormat(oldFormat);
		SimpleDateFormat newSdf = new SimpleDateFormat(newFormat);
		try {
			return newSdf.format(oldSdf.parse(datetime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static long getDifferenceDays(Date d1, Date d2) {
		long diff = d2.getTime() - d1.getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	public static String getDateTimeFormatByDate(String format, Date date){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static String getDateTimeFormatByLong(String format, long time){
		Date date = new Date();
		date.setTime(time);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static String calculateDifference(Context context, long timeInMillis){
		String result = "";
		int hours = (int) ((timeInMillis / (1000 * 60 * 60)));
		if(hours>0){
			if(hours==1) result += hours + " " + context.getString(R.string.hour);
			else result += hours + " " + context.getString(R.string.hours);
		}
		int minutes = (int) ((timeInMillis / (1000 * 60)) % 60);
		if(minutes>0){
			if(result.length()>0) result += " ";
			if(minutes==1) result += minutes + " " + context.getString(R.string.minute);
			else result += minutes + " " + context.getString(R.string.minutes);
		}

		if(result.length()==0){
			int seconds = (int) ((timeInMillis / 1000) % 60);
			if(seconds>0){
				if(result.length()>0) result += " ";
				if(minutes==1) result += seconds + " " + context.getString(R.string.second);
				else result += seconds + " " + context.getString(R.string.seconds);
			}
		}

		return result;
	}

	public static String secondsToString(int seconds) {
		int hour = seconds/3600;
		final int min = seconds/60;
		final int sec = seconds-(min*60);

		final String strMin = placeZeroIfNeede(min);
		final String strSec = placeZeroIfNeede(sec);
		return String.format("%s:%s",strMin,strSec);
	}

	private static String placeZeroIfNeede(int number) {
		return (number >=10)? Integer.toString(number):String.format("0%s",Integer.toString(number));
	}

	public static long dayDifferent(long time1, long time2){
		long diff = time2 - time1;
		System.out.println ("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
}
