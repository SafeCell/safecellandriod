package com.safecell;




import com.android.internal.telephony.ITelephony;
import com.safecell.model.Emergency.Emergencies;
import com.safecell.networking.ConfigurationHandler;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class PhoneReceiver extends BroadcastReceiver {
	 
	
	TelephonyManager telephonyManager;
	 private ITelephony telephonyService;
	 int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTH | 
	 PhoneStateListener.LISTEN_DATA_ACTIVITY | 
	 PhoneStateListener.LISTEN_CELL_LOCATION |
	 PhoneStateListener.LISTEN_CALL_STATE |
	 PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
	 PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
	 PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
	 PhoneStateListener.LISTEN_SERVICE_STATE
	 ;
	 private  boolean isSendSMS;
	private PhoneStateListener phoneStateListener;
	
	private Context mContext;
	
	private String TAG = PhoneReceiver.class.getSimpleName();
	
		
	@Override
    public void onReceive(Context context, Intent intent) {
	       SharedPreferences preferences = context.getSharedPreferences(
	                "TRIP", 1);
	       
	        this.mContext = context;
            telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            connectToTelephonyService();
           /* SharedPreferences sharedPreferences = context.getSharedPreferences("FAKE_LOCATION", 1);
            boolean b  = sharedPreferences.getBoolean("fake_location", false);
           */
            SharedPreferences sharedPreferences = context.getSharedPreferences("TRIP", 1);
            boolean b = sharedPreferences.getBoolean("isTripStarted", false);
    		
            isSendSMS = context.getSharedPreferences("SMSAutoReplyCheckBox",Context.MODE_WORLD_READABLE).getBoolean("isAutoreply", true);
            
            if (!intent.getAction().equals("android.intent.action.PHONE_STATE")) return;
            
            
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			
			if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) && b ) {
	             Log.d(TAG, "extra state ringing");
				//Log.v("Safecell :"+"Line Number", telephonyManager.getVoiceMailNumber());
                	try {
                		TrackingScreenActivity.INCOMING_CALL_OCCUER = true;
                		telephonyManager.listen(phoneStateListener, events);
                		// this is not allowed 
                	//	telephonyService.silenceRinger();
						telephonyService.endCall();
						TrackingScreenActivity.incomingCallCounter++;
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                	
                }
    	}
         
            
	@SuppressWarnings("unchecked")
	private void connectToTelephonyService() {
        try 
        {               
                Class c = Class.forName(telephonyManager.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
             //   telephonyService = (ITelephony)m.invoke(tm);
                telephonyService = (ITelephony)m.invoke(telephonyManager);
               
                
        } catch (Exception e) {
                e.printStackTrace();
                Log.e("call prompt","FATAL ERROR: could not connect to telephony subsystem");
                Log.e("call prompt","Exception object: "+e);
               // finish();
        }               
        
        phoneStateListener = new PhoneStateListener(){
        	
        	@Override
        	public void onCallStateChanged(int state, String incomingNumber) {
        		super.onCallStateChanged(state, incomingNumber);
        		String callState = TelephonyManager.EXTRA_STATE_RINGING;
        		switch (state) {
        		case TelephonyManager.CALL_STATE_RINGING : 	callState = "Ringing (" + incomingNumber + ")";
        		if (!TrackingService.incomingNumberArrayList.contains(incomingNumber)) {
        			TrackingService.incomingNumberArrayList.add(incomingNumber);
        			if (isSendSMS) {   
            			sendmessage(incomingNumber);
    				}
				}	
        		
        		//Log.v("Safecell :"+"incomingNumber", callState);
        		break;
        		}
        		
        	}
        };
        
      
}
	  private void sendmessage(String destinationAddress) {
		String senderMessage= "The person you are trying to reach is driving and will receive your message upon reaching their destination. Learn more: http://safecellapp.com.";
		sendSMS(destinationAddress, senderMessage);
  	}
	  
	  /**
	   * Send sms
	   * 
	   * @param phoneNumber
	   * @param message
	   */
	  public void sendSMS(String phoneNumber, String message)
	    {        
	        String SENT = "SMS_SENT";
	        String DELIVERED = "SMS_DELIVERED";
	        
	        
	        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0,
	            new Intent(SENT), 0);
	 
	        PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0,
	            new Intent(DELIVERED), 0);
	 
	        SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
	    }
	  	
}