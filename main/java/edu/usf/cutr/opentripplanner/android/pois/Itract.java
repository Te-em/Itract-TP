package edu.usf.cutr.opentripplanner.android.pois;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.usf.cutr.opentripplanner.android.OTPApp;

import android.util.Log;

/**
 * @author Khoa Tran
 *
 */

public class Itract implements Places{
	private String request = "http://itract.cs.kau.se:8081/proxy/api/otp/ws/serverinfo?";

	private static String TAG = "OTP";
	
	public static final String PARAM_LAT = "lat";
	public static final String PARAM_LON = "lon";
	public static final String PARAM_RADIUS = "radius";
	public static final String PARAM_NAME = "query";

	// JSON Node names
	private static final String TAG_HTML_ATTRIBUTIONS = "html_attributions";
	private static final String TAG_STATUS = "status";
	private static final String TAG_RESULTS = "results";
	private static final String TAG_GEOMETRY = "geometry";
	private static final String TAG_LOCATION = "location";
	private static final String TAG_VIEWPORT = "viewport"; 
	private static final String TAG_LATITUDE = "lat";
	private static final String TAG_LONGITUDE = "lng";
	private static final String TAG_ICON = "icon";
	private static final String TAG_NAME = "name";
	private static final String TAG_RATING = "rating";
	private static final String TAG_REFERENCE = "reference";
	private static final String TAG_TYPES = "types";
	private static final String TAG_VICINITY = "vicinity";
	private static final String TAG_FORMATTED_ADDRESS = "formatted_address";
	private static final String TAG_EVENTS = "events";
	private static final String TAG_EVENT_ID = "event_id";
	private static final String TAG_SUMMARY = "summary";
	private static final String TAG_URL = "url";

	// contacts JSONArray
	JSONArray results = null;

	public JSONObject requestPlaces(String paramLat, String paramLon, String paramName){
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		
		String encodedParamLat = "";
		String encodedParamLon = "";
		String encodedParamName = "";
		try {
			if ((paramLat != null) && (paramLon != null)){
				encodedParamLat = URLEncoder.encode(paramLat, OTPApp.URL_ENCODING);
				encodedParamLon = URLEncoder.encode(paramLon, OTPApp.URL_ENCODING);
			}
			encodedParamName = URLEncoder.encode(paramName, OTPApp.URL_ENCODING);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		if ((paramLat != null) && (paramLon != null)){
			request += "lon=" + encodedParamLon;
			request += "&lat=" + encodedParamLat;
		}
		else{
			request += "query=" + encodedParamName;
		}

		Log.v(TAG, request);

		HttpGet httpGet = new HttpGet(request);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.v(TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v(TAG, builder.toString());
		
		JSONObject json = null;
		try {
			json = new JSONObject(builder.toString());
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing data " + e.toString());
		}
		
		return json;
	}

	public ArrayList<POI> getPlaces(HashMap<String, String> params){
		ArrayList<POI> pois = new ArrayList<POI>();
		
		String paramLat = params.get(PARAM_LAT);
		String paramLon = params.get(PARAM_LON);
		String paramName = params.get(PARAM_NAME);
		
		// Get JSON
		JSONObject json = this.requestPlaces(paramLat, paramLon, paramName);

		if (json != null){
			// Decrypt JSON
			try {
				results = json.getJSONArray(TAG_RESULTS);

				for(int i = 0; i < results.length(); i++){
					JSONObject r = results.getJSONObject(i);

					String name = r.getString(TAG_NAME);
					String address = r.getString(TAG_FORMATTED_ADDRESS);

					JSONObject geometry = r.getJSONObject(TAG_GEOMETRY);
					JSONObject location = geometry.getJSONObject(TAG_LOCATION);
					double lat = location.getDouble(TAG_LATITUDE);
					double lon = location.getDouble(TAG_LONGITUDE);
					
					POI point = new POI(name, address, lat, lon);
					pois.add(point);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return pois;
	}
}

