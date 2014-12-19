package edu.usf.cutr.opentripplanner.android.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.v092snapshot.api.model.WalkStep;

import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.OnItractBusPosCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnItractBusStopsCompleteListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class ItractBusStops extends AsyncTask<String, Void, ItractBusStops> {
	
	public class Departure
	{
		public String routeShortName;
		public String tripHeadsign;
		public String time;
		public String agencyName;
	}
	
	public class BusStop
	{
		public double lat, lon;
		public String name;
		public String busID;
		public String ID;
		public String marker;
		public ArrayList<Departure> departures;
	}
	
	private ArrayList<BusStop> stops;
	private OnItractBusStopsCompleteListener listener;
	private ProgressDialog errorDialog;
	private WeakReference<Activity> activity;
	
	public ArrayList<BusStop> getStops() { return stops; }
	/*public ArrayList<Departure> getDepartures() { return departures; }
	public double getLat() { return lat; }
	public double getLon() { return lon; }
	public String getBusID() { return busID; }
	public String getName() { return name; }
	public String getMarker() { return marker; }
	public void setMarker(String m) { marker = m; }*/

	public ItractBusStops(OnItractBusStopsCompleteListener listener, WeakReference<Activity> activity) 
	{
		this.activity = activity;
        this.listener = listener;
        if (activity.get() != null)
			errorDialog = new ProgressDialog(activity.get());
	}
	
	public void initDepartures(BusStop stop) throws JSONException, IOException
	{
		String request, response;
		JSONArray arr, deps;
		Date date;
		DateFormat dateFormat;
		String timeRes;
		
		date = new Date();
		//dateFormat = new SimpleDateFormat("HH:mm");
		//timeRes = dateFormat.format(date);
		
		stop.departures = new ArrayList<Departure>();
		request = "http://itract.cs.kau.se:8081/proxy/api/transit/arrivalsAndDeparturesForStop?lat=" +
	 			stop.lat + "&lon=" + stop.lon + "&agencyId=" + stop.busID + "&stopId=" + stop.ID + 
	 			"&startTime=" + date.getTime() + "&endTime=" + (date.getTime() + (3600000 * 24));
	 	//s = "http://itract.cs.kau.se:8081/proxy/api/transit/arrivalsAndDeparturesForStop?lat=59.37919176435972&lon=13.498671776845185&agencyId=601&stopId=7421275&startTime=1382602879730&endTime=1382689279730";
	 	URL url = new URL(request);
		Scanner scan = new Scanner(url.openStream());
		if (scan == null) cancel(true);
		response = new String();
	 	while (scan.hasNext())
		{
			response += scan.nextLine();
		}
		scan.close();

		if (response.length() < 100)//str.contains("\"departures\":[]")) 
		{
			// do nothing
		}
		else
		{
			arr = new JSONArray(response);
			int i = 0;
			while (!arr.isNull(i))
				{
				deps = arr.getJSONObject(i).getJSONObject("data").getJSONArray("departures");
				// Get departure information.
				int k = 0;
				while (!deps.isNull(k) || k > 10)
				{
		    		Departure dep = new Departure();
		    		if (!deps.getJSONObject(k).isNull("routeShortName"))
		    			dep.routeShortName = deps.getJSONObject(k).getString("routeShortName");
		    		else
		    			dep.routeShortName = deps.getJSONObject(k).getString("tripShortName");
		    		dep.tripHeadsign = deps.getJSONObject(k).getString("tripHeadsign");
		    		dep.time = deps.getJSONObject(k).getString("time");
		    		dep.agencyName = deps.getJSONObject(k).getString("agencyName");
					stop.departures.add(dep);
					k++;
				}
				i++;
			}
		}
	}
	
	protected ItractBusStops doInBackground(String... urls) {
        try {
        	JSONArray arr;
        	JSONObject obj;
    		String request = urls[0];
    		URL url = new URL(request);
    		String response = "";
    		Scanner scan = new Scanner(url.openStream());
    		if (scan == null) cancel(true);
    		//response = "[]";
    		Log.d("test2", "request: " + request);
    		response = new String();
    		while (scan.hasNext()) response += scan.nextLine();
    		scan.close();
    		
    		Log.d("test2", "response: " + response);
    		
    		if (response.equals("[]") || response.equals(""))
    		{
    			request = "http://itract.cs.kau.se:8081/proxy/api/transit/stopsInArea?lat=59.377398154058596&lon=13.518367104629524&southWestLat=59.35938051789711&southWestLon=13.44953089247133&northEastLat=59.39540622331862&northEastLon=13.587203316787736";
    			url = new URL(request);
    			scan = new Scanner(url.openStream());
        		response = new String();
        		while (scan.hasNext()) response += scan.nextLine();
        		scan.close();
    		}
    		arr = new JSONArray(response);	
    		obj = null;
    		stops = new ArrayList<BusStop>();
    		
    		// Loop through graphs.
    		int i = 0;
    		while (!arr.isNull(i)) 
    		{
    			obj = arr.getJSONObject(i);
    			
    			// Loop through and create stops.
	    		int j = 0;
	    		while (!obj.getJSONArray("data").isNull(j))
				{
					BusStop stop = new BusStop();
					stop.lat = Double.parseDouble(obj.getJSONArray("data").getJSONObject(j).getString("lat"));
					stop.lon = Double.parseDouble(obj.getJSONArray("data").getJSONObject(j).getString("lon"));
					stop.busID = (obj.getJSONArray("data").getJSONObject(j).getString("agencyId"));
					stop.name = (obj.getJSONArray("data").getJSONObject(j).getString("name"));
					stop.ID = (obj.getJSONArray("data").getJSONObject(j).getString("id"));
					//initDepartures(stop);
					stops.add(stop);
					j++;
				} // End stop loop.
	    		i++;
    		} // End graph loop.
        } catch (IOException e) { cancel(true);} catch (JSONException e) { cancel(true); }
		return this;
    }
	
	public void onCancel()
	{
		Log.d("test", "cancelled!");
		try{		
			Log.d("test", "dialogue");
			if (errorDialog != null && errorDialog.isShowing()) {
				errorDialog.dismiss();
			}
		}catch(Exception e){}
		
		if (activity.get() != null)
		{
			Log.d("test", "build");
			AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
	    	builder.setMessage("Connection error.")
				.setTitle("Routes");
	    	Log.d("test", "dialogue create");
	    	AlertDialog dialog = builder.create();
	    	dialog.setCanceledOnTouchOutside(true);
	    	Log.d("test", "show");
	    	dialog.show();
		}
    	Log.d("test", "end");
	}
	
	@Override
	protected void onPostExecute(ItractBusStops stops) {
		Log.d("test", "in post");
        listener.onItractBusStopsComplete(stops);
}
}
