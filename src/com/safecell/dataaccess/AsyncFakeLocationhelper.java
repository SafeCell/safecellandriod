package com.safecell.dataaccess;

import android.content.Context;
import android.os.AsyncTask;

import com.safecell.networking.FakeLocationJSONhelper;

public class AsyncFakeLocationhelper  extends AsyncTask<Context,Void,Void>
{

	
	@Override
	protected Void doInBackground(Context... params) {
		//storeFakeLocationFiles();
		FakeLocationJSONhelper fakeLocationJSONhelper = new FakeLocationJSONhelper(params[0]) ;
		fakeLocationJSONhelper.storeFakeLocationFiles();
		return null;
	}
	


}
