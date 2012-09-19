/*******************************************************************************
* OutgoingSMSDetector.java.java, Created: May 7, 2012
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

import com.safecell.model.SCInterruption;
import com.safecell.utilities.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * @author uttama
 *
 */
public class OutgoingSMSDetector extends ContentObserver{
    
    private Context mContext;
    
    private String TAG = OutgoingSMSDetector.class.getSimpleName();
    
    private static final String CONTENT_SMS = "content://sms";

    /**
     * Constant from Android SDK
     */
    private static final int MESSAGE_TYPE_SENT = 2;

    
    /**
     * @param handler
     */
    public OutgoingSMSDetector(Handler handler , Context context) {
        super(handler);
        this.mContext = context;
    }
    
    public void start(){
        ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS),true, this);
    }
    
    public void stop(){
        ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.unregisterContentObserver(this);
    }
    
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if(isOutgoingSms()){
            Log.d(TAG, "SMS detected ");
            Util.saveInterruption(mContext , SCInterruption.SMS);
            
        }
    }
    
    /**
     * This is invoked directly from the SMS observer to retrieve the outgoing
     * SMS. A more elegant method would be firing a broadcast intent and let the
     * receiver handles the intent naturally.
     * 
     * @see #registerContentObserver(AndroidEvent, int)
     * @param context
     */
    private boolean isOutgoingSms() {
        Cursor cursor = mContext.getContentResolver().query(
                Uri.parse(CONTENT_SMS), null, null, null, null);
        boolean isOutgoingSms = false;
        if (cursor.moveToNext()) {
            String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
            int type = cursor.getInt(cursor.getColumnIndex("type"));
            Log.d(TAG, "Type = "+type);
            // Only processing outgoing sms event & only when it
            // is sent successfully (available in SENT box).
            if (protocol != null || type != MESSAGE_TYPE_SENT) {
                isOutgoingSms = false;
            }else{
                isOutgoingSms = true;
            }
        }
        cursor.close();
        return isOutgoingSms;
    }
}
