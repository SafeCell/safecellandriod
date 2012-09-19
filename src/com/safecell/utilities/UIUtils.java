package com.safecell.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class UIUtils {
	 static boolean okButtonFalg = false;
	 static boolean result=false;
	 
	public UIUtils()
	{
		
	}
	public static boolean OkDialog(Context context,String title)
	{
		okButtonFalg = false;
		AlertDialog dialog = new AlertDialog.Builder(context)
		.setMessage(title)
		.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				 okButtonFalg = true;
				dialog.cancel();
				
			  
			}
		})
		.create();
		dialog.show();
		return okButtonFalg;
		
	}
	
	public static boolean doYouWantToExit(Context ctx)
	{
		result = false;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ctx);
		dialogBuilder.setMessage("Are you sure you want to exit?")
	       .setCancelable(false)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   
	        	   result = true;
	        	  
	        	  // Log.v("Safecell :"+"doYouWantToExit1",""+result);
	              //  HomeScreenActivity.this.finish();
	           }
	       })
	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {	        	   
	        	   result = false;
	        	   dialog.cancel();
	           }
	       });		
		
		dialogBuilder.create().show();		
		//Log.v("Safecell :"+"doYouWantToExit2",""+result);
		return result;
		
	}
	

}
