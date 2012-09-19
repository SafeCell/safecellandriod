package com.safecell.utilities;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateUtils {

	public DateUtils() {
		// TODO Auto-generated constructor stub
	}

	public static long dateInMillSecond(String dateString) {		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");	
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		java.util.Date date=null;
		try {
			
			date = simpleDateFormat.parse(dateString);
			
			//System.out.println("" + date.getTime());
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date.getTime();

	}
	
	public static String dateInString(long timeInMillSecond) {
		Date date = new Date(timeInMillSecond);
		SimpleDateFormat simpleDate = new SimpleDateFormat(
		"MMM dd',' yyyy hh:mm a");
		String dateString = simpleDate.format(date);
		//Log.v("Safecell :"+"Date", dateString);
		return dateString;
	}
	
	
	public static  String getTimeStamp(long timeInMillSecond) {
		//Log.v("Safecell :"+"timeInMillSecond",""+timeInMillSecond);
		Date date = new Date(timeInMillSecond);
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		simpleDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		String dateString = simpleDate.format(date);

		return dateString;
	}
	
	public static  String getDate(long timeInMillSecond) {
		//Log.v("Safecell :"+"timeInMillSecond",""+timeInMillSecond);
		Date date = new Date(timeInMillSecond);
		SimpleDateFormat simpleDate = new SimpleDateFormat("MMM dd',' yyyy");
		String dateString = simpleDate.format(date);
		//Log.v("Safecell :"+"Date", dateString);
		return dateString;
	}
}
