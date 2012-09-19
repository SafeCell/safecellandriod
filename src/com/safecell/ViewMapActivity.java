package com.safecell;



import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.safecell.dataaccess.TempTripJourneyWayPointsRepository;
import com.safecell.utilities.FlurryUtils;

public class ViewMapActivity extends MapActivity
{
	 MapView mapView;
		/*private Resources resources;
		private AssetManager assetManager;
		private StringBuilder strFile;*/
		//private JSONArray wayPointArray;
		Context context;
		String stringFile;
		String stringFromFile;
		StringEntity stringEntity;
		JSONObject outerJsonObject;
		HttpResponse response;
		/*private int accountID;
		private int profileID;
		private String apiKey;
		private String tripName, startDateTime, endDateTime;*/
		int totalMiles;
		public double[] latitude={};
		public double[] langitude={};	
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.map_layout);
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    context = ViewMapActivity.this;
	
	    readWayPoints();
	    
	    MyOverlay myOverlay = new MyOverlay(mapView);
	    List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();	
		listOfOverlays.add(myOverlay);		
	//	mapView.invalidate();
		
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
	
	void readWayPoints(){
		TempTripJourneyWayPointsRepository tempTripJourneyWayPointsRepository = new TempTripJourneyWayPointsRepository(ViewMapActivity.this);
		Cursor cursor=tempTripJourneyWayPointsRepository.getTrip();
		
		//String jsonString="";
		if(cursor.getCount()>0)
		{
			latitude = new double[cursor.getCount()];
			langitude = new double[cursor.getCount()];
			
			cursor.moveToFirst();
			try {				
			
			for(int i=0;i<cursor.getCount();i++)
			{					
				langitude[i]= cursor.getDouble(3);
				latitude[i] =cursor.getDouble(2);
				cursor.moveToNext();
			}
			
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	
	
	class MyOverlay extends com.google.android.maps.Overlay {
		GeoPoint g1;
		ArrayList<GeoPoint> mGeoPoint;
		
		public MyOverlay(MapView mapView){				
				mGeoPoint = new ArrayList<GeoPoint>();
				MapController mapControler = mapView.getController();
				
					for(int i=0;i<latitude.length;i++)
					{
						//Log.v("Safecell :"+"mGeoPint",""+i);
						//Log.v("Safecell :"+"latitude",""+(int)(latitude[i] * 1E6 ));
						//Log.v("Safecell :"+"langitude",""+(int)(langitude[i] * 1E6));
						g1 = new GeoPoint( (int)(latitude[i] * 1E6 ), (int)(langitude[i] * 1E6) );
						mGeoPoint.add(g1);
					}
					
					mapControler.setZoom(13);
					mapControler.animateTo(g1);			
		}		
		@Override
		public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
			//super.draw(canvas, mv, shadow);
			
			drawPath(mv, canvas);
			return false;
		}
		public void drawPath(MapView mv, Canvas canvas) {
//			Log.v("Safecell :"+"Draw Path", "Called");
			int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
			Paint paint = new Paint();
			paint.setColor(Color.BLUE);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);			
				Point point = new Point();
				//Point point1 = new Point();				
				for(int i = 0;i<mGeoPoint.size();i++)
				{					
					mv.getProjection().toPixels(mGeoPoint.get(i), point);						
					x2 = point.x;
					y2 = point.y;
					
					//canvas.drawLine(point.x, point.y, point1.x, point1.y, paint);
					if(x1!=-1){
					canvas.drawLine(x1, y1, x2, y2, paint);
					}
					x1=x2;
					y1=y2;												
				}			
		}

	}

}