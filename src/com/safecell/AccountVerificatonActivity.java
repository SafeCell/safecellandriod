package com.safecell;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.safecell.dataaccess.AccountRepository;
import com.safecell.networking.NetWork_Information;
import com.safecell.networking.ValidateAccountRequest;
import com.safecell.networking.ValidateAccountResponceHandler;
import com.safecell.utilities.FlurryUtils;

public class AccountVerificatonActivity extends Activity  {
	EditText accountCodeEditText;
	Button validateAccountButton, cancelButton;
	AsyncCodeVerification asyncCodeVerification;
	Context context;
	ProgressDialog progressDialog;
	String message;
	boolean dialogDismiss = false;
	private boolean cancelNewProfileProgressDialog=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setWindowAnimations(R.anim.null_animation);
        this.initUi();
        
        
        
		validateAccountButton.setOnClickListener(validateAccountButtonOnClickListner);
		context = AccountVerificatonActivity.this;
		progressDialog = new ProgressDialog(context);
		
	}

	private void initUi() {
		// TODO Auto-generated method stub
		setContentView(R.layout.account_verifications_layout);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		accountCodeEditText = (EditText) findViewById(R.id.accountVerificationAccountCodeEditText);
		validateAccountButton = (Button) findViewById(R.id.accountVerificationValidateAccountButton);
		
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
	
	private OnClickListener validateAccountButtonOnClickListner = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if (accountCodeEditText.getText().toString().equalsIgnoreCase("")) {
				Toast.makeText(AccountVerificatonActivity.this,
						"Account Code Should Not Empty", Toast.LENGTH_SHORT)
						.show();
				
				
			} else {
				AccountRepository accountRepository = new AccountRepository(AccountVerificatonActivity.this);
				accountRepository.deleteAccount();

				
			  
				
				if (NetWork_Information.isNetworkAvailable(AccountVerificatonActivity.this)) 
				{

				
					
					asyncCodeVerification = new AsyncCodeVerification();
					asyncCodeVerification.execute();
					 
				}else{
					 
					NetWork_Information.noNetworkConnectiondialog(AccountVerificatonActivity.this);
					
				}
				
			}
			
			}
		};
	
		
		private  String   accountVerification() {		
			
			ValidateAccountRequest validateAccountRequest = new ValidateAccountRequest(context,accountCodeEditText.getText().toString());
			String httpResponse = validateAccountRequest.Request();
			message = validateAccountRequest.getFailureMessage();
			
			
			return httpResponse;
					
			}
		
		public boolean accountVerificationResponse()
		{
			String httpResponse = accountVerification();
			
			if (httpResponse == null)
			{
			
				dialogDismiss = true;
				 //progressDialog.dismiss();
					
					
					new AlertDialog.Builder(context).setMessage(message)
							
							.setNeutralButton(
							"OK", new DialogInterface.OnClickListener() {
								
								public void onClick(DialogInterface dialog, int which) {

									dialog.cancel();

								}
								
							}).show();
			} else {
				ValidateAccountResponceHandler validateAccountResponceHandler = new ValidateAccountResponceHandler(AccountVerificatonActivity.this);
				validateAccountResponceHandler.HandleResponce(httpResponse);
				dialogDismiss = true;
				
				Intent intent = new Intent(AccountVerificatonActivity.this,
						AccountFormActivity.class);
				intent.putExtra("from", "varification");
				startActivity(intent);	
				finish();
			}
			return dialogDismiss;				
		}
		
		 @Override
		 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 	// TODO Auto-generated method stub
		 	
		 	if(keyCode==KeyEvent.KEYCODE_BACK)
		 	{
		 		Intent intent = new Intent(AccountVerificatonActivity.this,
						TrialOrAlreadyAccountActivity.class);
		 		startActivity(intent);
		 		finish();
		 		
		 	}
		 	return super.onKeyDown(keyCode, event);
		 }
	 
				
	 
	 private class AsyncCodeVerification extends AsyncTask<Void, Boolean, Boolean> {

			protected Boolean doInBackground(Void... params) { 
				Looper.prepare();
				//	accountVerificationResponse();	
				 
				publishProgress(accountVerificationResponse());
				Looper.loop();
			
				return dialogDismiss;

			}

			@Override
			protected void onProgressUpdate(Boolean... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			if (dialogDismiss) {
				onPostExecute(dialogDismiss);
			}
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (progressDialog.isShowing()&& dialogDismiss == true) {
					
					progressDialog.dismiss();
					
				}
			}

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				progressDialog.setMessage("Loading Please Wait");
				progressDialog.show();
				progressDialog.setCancelable(cancelNewProfileProgressDialog);
				
			}
		}
	 
	 
}