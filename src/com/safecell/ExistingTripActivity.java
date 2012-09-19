package com.safecell;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.safecell.dataaccess.TripRepository;
import com.safecell.utilities.FlurryUtils;

public class ExistingTripActivity extends ListActivity{
	
	Context context;
	private String [] tripNameList = {}; 
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setWindowAnimations(R.anim.null_animation);
		context = ExistingTripActivity.this;
		
		TripRepository tripRepository = new TripRepository(context); 
		tripNameList = tripRepository.SelectListOfTripName();
		
		setContentView(R.layout.existing_trip_name_layout);
		
	    setListAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, tripNameList));
	    	
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
	
		Intent intent = new Intent();
		intent.putExtra("Trip_name",tripNameList[position]);
		setResult(RESULT_OK, intent);
		finish();
		
	
	}
}
