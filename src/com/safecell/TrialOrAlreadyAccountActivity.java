package com.safecell;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.safecell.networking.GetTermsConditions;
import com.safecell.networking.GetTermsConditionsResponseHandler;
import com.safecell.utilities.FlurryUtils;
import com.safecell.utilities.URLs;

public class TrialOrAlreadyAccountActivity extends Activity {
	Button btnStartTrial;
	Button btnAlreadyHaveAccount;
	WebView wv;
	private GetTermsConditionsResponseHandler termsConditionsResponseHandler;
	private GetTermsConditions getTermsConditions;
	private Context context;
	private String valuesStr;
	private static final int ACCOUNT_FORM_ACTIVITY = 1;
	private static final int LOGIN_ACTIVITY = 2;
	private static final int ACCOUNT_VERIFICATION_ACTIVITY = 3;
	private AlertDialog alertDialogForTermsConditions;;
	//Dialog dialog;
	
	private class HelloWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
//	        Log.v("url",url);
	        return true;
	    }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = TrialOrAlreadyAccountActivity.this;
		getWindow().setWindowAnimations(R.anim.null_animation);
		initUi();

	}
	void initUi()
	{
		setContentView(R.layout.trial_or_already_account_layout);

		btnAlreadyHaveAccount =(Button)findViewById(R.id.TrialOrAlreadyAccountAlreadyAccountButton);
		btnStartTrial =( Button) findViewById(R.id.TrialOrAlreadyAccountStartTrialButton);
		btnAlreadyHaveAccount.setOnClickListener(AlreadyHaveAccountOnClickListener);
		btnStartTrial.setOnClickListener(startTrialOnClickListener);

		/*	getTermsConditions = new GetTermsConditions(context);
		String result = getTermsConditions.getTermsConditionsStr();
		termsConditionsResponseHandler = new GetTermsConditionsResponseHandler();
		valuesStr = termsConditionsResponseHandler.termsResponseStr(result);*/


	}
	
	@Override
	protected void onStart() {
		super.onStart();
		FlurryUtils.startFlurrySession(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryUtils.endFlurrySession(this);
	}

	OnClickListener startTrialOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			dialogforWebview(ACCOUNT_FORM_ACTIVITY);
		
		}
	};
	OnClickListener AlreadyHaveAccountOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			AlertDialog.Builder builder;
			final AlertDialog alertDialog;

			Context mContext = TrialOrAlreadyAccountActivity.this;
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View dialog = inflater.inflate(R.layout.existing_new_profile_dialog,
					(ViewGroup) findViewById(R.id.layout_root));


			builder = new AlertDialog.Builder(mContext);
			builder.setView(dialog);
			alertDialog = builder.create();
			/*

			final Dialog dialog = new Dialog(TrialOrAlreadyAccountActivity.this);

			//AlertDialog.Builder
			dialog.setContentView(R.layout.existing_new_profile_dialog);
			dialog.setTitle("Profile Status");*/
			Button btnExistingProfile = (Button) dialog.findViewById(R.id.ExistingProfileButton);
			Button btnNewProfile = (Button) dialog.findViewById(R.id.NewProfileButton);
			Button btnCancle =(Button) dialog.findViewById(R.id.CancelButton);

			btnExistingProfile.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					alertDialog.cancel();
					dialogforWebview(LOGIN_ACTIVITY);
					
				}
			});

			btnNewProfile.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {					
					alertDialog.cancel();
					dialogforWebview(ACCOUNT_VERIFICATION_ACTIVITY);

				}
			});

			btnCancle.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {		
			
					alertDialog.cancel();
					finish();
				}
			});			

			alertDialog.show();
		}


	};

	void dialogforWebview(final int to){

		AlertDialog.Builder builder;
		
		Context mContext = TrialOrAlreadyAccountActivity.this;


		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.private_policy_layout,
				(ViewGroup) findViewById(R.id.layout_root));

		final Activity activity = TrialOrAlreadyAccountActivity.this;
		wv =(WebView)layout.findViewById(R.id.webview);


		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		
		wv.setWebViewClient(new HelloWebViewClient());
		wv.setWebChromeClient(new WebChromeClient(){

			public void onProgressChanged(WebView view, int newProgress) {
				activity.setProgress(newProgress * 100);
				
				if (newProgress == 100) {
					
				}
			};});
		
		wv.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//				Log.v("errorCode", "errorcode "+errorCode + description);
				alertDialogForTermsConditions.cancel();
				
				
			}
		});
		wv.loadUrl(URLs.REMOTE_URL+"api/1/site_setting/terms_of_service.html");
		

		builder = new AlertDialog.Builder(mContext);
		builder.setView(layout);
		builder.setTitle("Policy");


		builder.setPositiveButton("Accept",
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				CallingActivity(to);
				dialog.cancel();
			}
		}).setNegativeButton("Don't Accept",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		alertDialogForTermsConditions = builder.create();
		alertDialogForTermsConditions.show();
	}

	private void CallingActivity(int to) {

		switch (to) {

		case ACCOUNT_FORM_ACTIVITY :
			Intent mIntent = new Intent(TrialOrAlreadyAccountActivity.this, AccountFormActivity.class);
			mIntent.putExtra("from", "trial");
			startActivity(mIntent);	
			break;
		case LOGIN_ACTIVITY :

			Intent mIntent1 = new Intent(TrialOrAlreadyAccountActivity.this,LoginActivity.class);
			startActivity(mIntent1);
			finish();	
			break;
		case ACCOUNT_VERIFICATION_ACTIVITY :

			Intent mIntent2 = new Intent(TrialOrAlreadyAccountActivity.this,AccountVerificatonActivity.class);
			mIntent2.putExtra("from", "varification");
			startActivity(mIntent2);	
			finish();
			break;

		}

	}
}
