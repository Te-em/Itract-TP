package edu.usf.cutr.opentripplanner.android.tasks;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;

import edu.usf.cutr.opentripplanner.android.listeners.OnItractBusStopsCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnItractDeparturesCompleteListener;
import edu.usf.cutr.opentripplanner.android.tasks.ItractBusStops.BusStop;
import edu.usf.cutr.opentripplanner.android.tasks.ItractBusStops.Departure;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class ItractDepartures extends AsyncTask<BusStop, Void, ItractDepartures> {

	public class Departure
	{
		public String routeShortName;
		public String tripHeadsign;
		public String time;
		public String agencyName;
	}
	
	private OnItractDeparturesCompleteListener listener;
	private ProgressDialog errorDialog;
	private WeakReference<Activity> activity;
	
	private ArrayList<Departure> departures;
	
	public ArrayList<Departure> getDepartures() { return departures; }
	
	public ItractDepartures(OnItractDeparturesCompleteListener listener, WeakReference<Activity> activity) 
	{
		this.activity = activity;
        this.listener = listener;
        if (activity.get() != null)
			errorDialog = new ProgressDialog(activity.get());
	}
	
	@Override
	protected ItractDepartures doInBackground(BusStop... stops) 
	{
		try {
			String request, response;
			JSONArray arr, deps;
			Date date;
			DateFormat dateFormat;
			String timeRes;
			BusStop stop = stops[0];
			
			date = new Date();
			//dateFormat = new SimpleDateFormat("HH:mm");
			//timeRes = dateFormat.format(date);
			
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
			
			departures = new ArrayList<Departure>();
	
			if (response.length() < 100)//str.contains("\"departures\":[]")) 
			{
				// do nothing
			}
			else
			{
				arr = new JSONArray(response);
				// Loop through graphs.
				int i = 0;
				while (!arr.isNull(i))
					{
					deps = arr.getJSONObject(i).getJSONObject("data").getJSONArray("departures");
					// Loop through and get departure information.
					int k = 0;
					while (!deps.isNull(k) && k < 11)
					{
						Departure dep = new Departure();
			    		if (!deps.getJSONObject(k).isNull("routeShortName"))
			    			dep.routeShortName = deps.getJSONObject(k).getString("routeShortName");
			    		else
			    			dep.routeShortName = deps.getJSONObject(k).getString("tripShortName");
			    		dep.tripHeadsign = deps.getJSONObject(k).getString("tripHeadsign");
			    		dep.time = deps.getJSONObject(k).getString("time");
			    		dep.agencyName = deps.getJSONObject(k).getString("agencyName");
						departures.add(dep);
						k++;
					} // End departure loop.
					i++;
				} // End graph loop.
			} 
		} catch (IOException e) { cancel(true);} catch (JSONException e) { cancel(true); }
		return this;
	}
	
	@Override
	protected void onPostExecute(ItractDepartures deps) {
        listener.onItractDeparturesComplete(deps);
}

}
