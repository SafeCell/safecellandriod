package com.safecell;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.safecell.dataaccess.ContactRepository;
import com.safecell.model.SCContact;
import com.safecell.utilities.FlurryUtils;

public class AddEmergencyContactsActivity extends Activity {

	EditText nameEditText, phoneNumberEditText;
	Button saveContactButton;
	private int contactID;
	private boolean insertOrUpdate = true;
	private ContactRepository contactRepository;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.initUi();
		saveContactButton.setOnClickListener(saveButtonOnClickListener);
	}

	private void initUi() {
		// TODO Auto-generated method stub
		setContentView(R.layout.add_contact_layout);
		nameEditText = (EditText) findViewById(R.id.AddContactNameEditText);
		phoneNumberEditText = (EditText) findViewById(R.id.AddContactNumberEditText);
		saveContactButton = (Button) findViewById(R.id.AddContactSaveContactButton);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		contactID = bundle.getInt("ContactId");

		contactRepository = new ContactRepository(AddEmergencyContactsActivity.this);

		Cursor ContactCursor = contactRepository
				.SelectContactsByWhere(contactID);

		if (ContactCursor.getCount() > 0) {

			ContactCursor.moveToFirst();
			int nameIndex = ContactCursor.getColumnIndex("name");
			int phonenumberIndex = ContactCursor.getColumnIndex("number");

			String personName = ContactCursor.getString(nameIndex);
			int phoneNumber = ContactCursor.getInt(phonenumberIndex);

			nameEditText.setText(personName);
			phoneNumberEditText.setText(phoneNumber + "");
			insertOrUpdate = false;

		}
		ContactCursor.close();
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

	OnClickListener saveButtonOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!nameEditText.getText().toString().equalsIgnoreCase("")
					&& !phoneNumberEditText.getText().toString()
							.equalsIgnoreCase("")) {
				
				SCContact scContact = new SCContact();
				scContact.setName(nameEditText.getText().toString());
				scContact.setNumber(phoneNumberEditText.getText().toString());
				scContact.setId(contactID);
				if (insertOrUpdate) {
					
					contactRepository = new ContactRepository(AddEmergencyContactsActivity.this);
					contactRepository.insertContact(scContact);
				} else {
					contactRepository = new ContactRepository(AddEmergencyContactsActivity.this);
					contactRepository.updateContact(scContact);
				}
			}
			
		}
	};
}
