/*******************************************************************************
* OutgoingCallReceiver.java.java, Created: Apr 25, 2012
*
* Part of Muni Project
*
* Copyright (c) 2012 : NDS Limited
*
* P R O P R I E T A R Y &amp; C O N F I D E N T I A L
*
* The copyright of this code and related documentation together with any
* other associated intellectual property rights are vested in NDS Limited
* and may not be used except in accordance with the terms of the licence
* that you have entered into with NDS Limited. Use of this material without
* an express licence from NDS Limited shall be an infringement of copyright
* and any other intellectual property rights that may be incorporated with
* this material.
*
* ******************************************************************************
* ******     Please Check GIT for revision/modification history    *******
* ******************************************************************************
*/
package com.safecell;

import com.android.internal.telephony.ITelephony;
import com.safecell.model.Emergency.Emergencies;
import com.safecell.model.SCInterruption;
import com.safecell.networking.ConfigurationHandler;
import com.safecell.utilities.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * @author uttama
 *
 */
public class OutgoingCallReceiver extends BroadcastReceiver {
    
    private Context mContext;
    
    private TelephonyManager telephonyManager;
    
    private ITelephony telephonyService;

    private String TAG = OutgoingCallReceiver.class.getSimpleName();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        
        if(null == bundle)
                return;
        this.mContext = context;
        String phonenumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        Log.d("OutgoingCallReceiver","Outgoing Call = "+phonenumber);
        SharedPreferences preferences = context.getSharedPreferences(
                "TRIP", 1);
    
        boolean isTripStarted = preferences.getBoolean("isTripStarted", false);
        if (isTripStarted && !TrackingService.ignoreLocationUpdates) {
            // TODO Is this the right place
            Util.saveInterruption(context , SCInterruption.CALL);
            if (ConfigurationHandler.getInstance().getConfiguration().isDisableCall()) {
                telephonyManager = (TelephonyManager)context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                connectToTelephonyService();
                if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                    if (!isEmergencyNumber(phonenumber)) {
                        try {
                            Log.d(TAG, "End Call");
                            setResultData(null);
                            telephonyService.endCall();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
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
    }
    
    public boolean isEmergencyNumber(String number) {
        boolean isEmergencyNumber = false;
        Cursor cursor = mContext.getContentResolver().query(Emergencies.CONTENT_URI, new String[] {
            Emergencies.NUMBER
        }, null, null, null);
        if (number != null && cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; cursor != null && i < cursor.getCount(); i++) {
                Log.d(TAG, "List "+cursor.getString(cursor.getColumnIndex(Emergencies.NUMBER)));
                if (number.equals(cursor.getString(cursor.getColumnIndex(Emergencies.NUMBER)))) {
                    isEmergencyNumber = true;
                    break;
                }
            }
            cursor.close();
        }
        return isEmergencyNumber;
    }
    

}
