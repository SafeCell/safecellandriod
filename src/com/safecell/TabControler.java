package com.safecell;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;

public class TabControler  {
	OnClickListener onClickListener;
	Context context;
	
	public TabControler(Context context){
		this.context =context;
		
	}
	
	public OnClickListener getHomeTabOnClickListner(){
	onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(TrackingService.homeScreenActivity !=null)
			{
				TrackingService.homeScreenActivity.finish();
			}
			Intent mIntent = new Intent(context, HomeScreenActivity.class);
			context.startActivity(mIntent);			
		}
	};	
	return onClickListener;
	}
	public OnClickListener getMyTripsOnClickListner(){
		onClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent mIntent = new Intent(context, MyTripsActivity.class);
				context.startActivity(mIntent);	
				
			}
		};	
		return onClickListener;
		}
	public OnClickListener getRulesOnClickListner(){
		onClickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent mIntent = new Intent(context, ViewRulesListActivity.class);
				context.startActivity(mIntent);	
				
			}
		};	
		return null;
		}
	
	public OnClickListener getSettingOnClickListener(){
	    //disabling setting
	    SharedPreferences preferences = context.getSharedPreferences("SETTINGS", Context.MODE_WORLD_READABLE);
	    boolean isDisabled = preferences.getBoolean("isDisabled", false);
	    if(isDisabled){
	        onClickListener = null;
	    }else{
	        onClickListener=new OnClickListener() {
	            
	            @Override
	            public void onClick(View v) {
	            
	                Intent mIntent=new Intent(context,SettingScreenActivity.class);
	                context.startActivity(mIntent);
	                
	                
	            }
	        };
	    }
		return onClickListener;
		
	}
}
