package com.safecell.networking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetWork_Information {

	public NetWork_Information() {

	}

	public static boolean isNetworkAvailable(Context context) {

		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.w("tag", "couldn't get connectivity manager");

		} else {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			return info != null && info.isConnectedOrConnecting();
		}	
		return false;

	}
	
	public static void noNetworkConnectiondialog(Context context)
	{
		AlertDialog dialog = new AlertDialog.Builder(context)
		.setTitle("No Network Connection")
		.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				 
			}
		})
		.create();
		dialog.show();
		
	}
}
