package com.safecell.receiver;

import java.sql.Date;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.safecell.TrackingScreenActivity;
import com.safecell.dataaccess.SMSRepository;
import com.safecell.model.SCSms;

public class SMSReceiver extends BroadcastReceiver {
    
    private String TAG = SMSReceiver.class.getSimpleName();
	
	public static final Uri uriSms = Uri.parse("content://sms");
	
	private SMSRepository smsRepository;
	private SCSms scSms;
	private boolean isSendSMS = true;

	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	Parcel parcel;
	Date date;
	
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
	    Log.d(TAG, "Intent Action = "+intent.getAction());
		smsRepository = new SMSRepository(context);
		scSms = new SCSms();
		this.mContext = context;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"TRIP", 1);
		boolean smsBlock = sharedPreferences.getBoolean("isTripStarted", false);
		isSendSMS = context.getSharedPreferences("SMSAutoReplyCheckBox",Context.MODE_WORLD_READABLE).getBoolean("isAutoreply", true);
		Log.d(TAG, "Intent "+intent.getAction());
		if ((!(intent.getAction()
				.equals("android.provider.Telephony.SMS_RECEIVED")))
				|| !smsBlock)
		{

			return;
		}
		if ((intent.getAction()
				.equals("android.provider.Telephony.SMS_RECEIVED"))
				&& smsBlock) {

			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			String str = "";
			long timeMillisecond = 0;

			if (bundle != null) {
				// ---retrieve the SMS message received---
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];

				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

					str += "SMS from " + msgs[i].getOriginatingAddress();
					str += " :";
					str += msgs[i].getMessageBody().toString();
					str += "\n";
					timeMillisecond = msgs[i].getTimestampMillis();

					scSms.setAddress(msgs[i].getOriginatingAddress());
					scSms.setBody(msgs[i].getMessageBody());
					scSms.setDate(msgs[i].getTimestampMillis());

					scSms.setProtocol(msgs[i].getProtocolIdentifier());
					scSms.setRead(0);
					scSms.setReply_path_present(0);
					scSms.setService_center(msgs[i].getServiceCenterAddress());
					scSms.setStatus(msgs[i].getStatus());
					scSms.setSubject(msgs[i].getPseudoSubject());
					scSms.setThread_id(0);
					scSms.setType(0);
					scSms.setLocked(0);

					smsRepository.insertQuery(scSms);

				}
				// ---display the new SMS message---
				date = new Date(timeMillisecond);
				if (isSendSMS) {
					sendmessage();
				}
				this.abortBroadcast();
				

			}
		}
	}

	private void sendmessage() {
		
		String destinationAddress = scSms.getAddress();
		String senderMessage= "The person you are trying to reach is driving and will receive your message upon reaching their destination. Learn more: http://safecellapp.com.";
		/*SmsManager smsManager = SmsManager.getDefault();
		String destinationAddress = scSms.getAddress();
		String senderMessage= "The person you are trying to reach is driving and will receive your message upon reaching their destination. Learn more: http://safecellapp.com.";
		smsManOager.sendTextMessage(destinationAddress, null,senderMessage , null, null);*/
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
