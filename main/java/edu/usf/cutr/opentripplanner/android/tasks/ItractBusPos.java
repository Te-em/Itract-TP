package edu.usf.cutr.opentripplanner.android.tasks;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;

import edu.usf.cutr.opentripplanner.android.listeners.MetadataRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnItractBusPosCompleteListener;
import edu.usf.cutr.opentripplanner.android.tasks.ItractBusStops.Departure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class ItractBusPos extends AsyncTask<String, Void, ItractBusPos>
{
	
	public class BusPosition
	{
		public Double lat;
		public Double lon;
		public String agencyID;
		public String routeID;
		public String vehicleID;
	}
	
	private OnItractBusPosCompleteListener listener;
	private ArrayList<BusPosition> positions;
	private ProgressDialog progressDialog;
	private WeakReference<Activity> activity;
	
	public ArrayList<BusPosition> getPositions() { return positions; }
	
	public ItractBusPos(OnItractBusPosCompleteListener listener, WeakReference<Activity> activity) 
	{
		this.activity = activity;
        this.listener = listener;
	}

	
	@Override
	protected ItractBusPos doInBackground(String... urls)
	{
		Log.d("test", "in buspos");
		URL url;
		String request, response;
		Scanner scan;
		JSONArray arr, data, vehicles;
		BusPosition pos;
		//JSONObject vehicle;
		try {
			request = urls[0];//"http://itract.cs.kau.se:8081/proxy/api/transit/vehicleLocations?lat=59.376818852286526&lon=13.571013503173836";
			url = new URL(request);
			// read from the URL
			scan = new Scanner(url.openStream());
			if (scan == null) cancel(true);
			response = new String();
			while (scan.hasNext())
				response += scan.nextLine();
			scan.close();
			//Log.d("test", "request: " + request);
			//Log.d("test", "response: " + response);
			if (response.equals("[,]") || response.equals("[]") || response.equals("")) 
			{
				cancel(true);
				Log.d("test", "response: " + response);
			}
			
			positions = new ArrayList<BusPosition>();
			
			arr = new JSONArray(response);
			// Loop through graphs.
			int i = 0;
			while (!arr.isNull(i))
				{
				data = arr.getJSONObject(i).getJSONArray("data");
				// Loop through agencies.
				int d = 0;
				while (!data.isNull(d))
				{
					vehicles = data.getJSONObject(d).getJSONArray("vehicles");
					String ID = data.getJSONObject(d).getString("agencyId");
					
					//Loop through and get vehicle information.
					int k = 0;
					while (!vehicles.isNull(k))
					{
						pos = new BusPosition();
						pos.routeID = vehicles.getJSONObject(k).getString("routeId");
						pos.vehicleID = vehicles.getJSONObject(k).getString("vehicleId");
						pos.lat = Double.parseDouble(vehicles.getJSONObject(k).getString("currentLocationLat"));
						pos.lon = Double.parseDouble(vehicles.getJSONObject(k).getString("currentLocationLon"));
						pos.agencyID = ID;
						positions.add(pos);
						k++;
					} // End vehicle loop.
					d++;
				}// End agency loop.
				i++;
			} // End graph loop.
			
		} catch (IOException e) { cancel(true); } catch (JSONException e) { cancel(true); }
		Log.d("test", "return");
		return this;
	}
	
	public void onCancel()
	{
		try{		
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}catch(Exception e){}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
    	builder.setMessage("Connection error.")
			.setTitle("Routes");
    	AlertDialog dialog = builder.create();
    	dialog.setCanceledOnTouchOutside(true);
    	dialog.show();
	}

		@Override
		protected void onPostExecute(ItractBusPos position) {
	        listener.onItractBusPosComplete(position);
	}
}
