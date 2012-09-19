package com.safecell.utilities;

import com.flurry.android.FlurryAgent;

import android.content.Context;

import java.util.Map;

public class FlurryUtils {

	public static final String FLURRY_KEY = "ZPHTDBWVX3ZB5C28NYCC";
	
	public static void startFlurrySession(Context currActivity) {
		FlurryAgent.onStartSession(currActivity, FLURRY_KEY);
	}
	
	public static void endFlurrySession(Context currActivity) {
		FlurryAgent.onEndSession(currActivity);
	}
	
	public static void logEvent(String eventId, Map<String, String> parameters) {
		FlurryAgent.onEvent(eventId, null);
	}
}
