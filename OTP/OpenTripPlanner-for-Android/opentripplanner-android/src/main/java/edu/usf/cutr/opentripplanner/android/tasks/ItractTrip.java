package edu.usf.cutr.opentripplanner.android.tasks;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opentripplanner.api.model.AgencyAndId;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Leg;
import org.opentripplanner.v092snapshot.api.model.Place;
import org.opentripplanner.v092snapshot.api.model.TripPlan;
import org.opentripplanner.v092snapshot.api.model.WalkStep;

import android.location.Geocoder;
import android.util.Log;

public class ItractTrip {

	private TripPlan tp;
	private boolean success;
	
	public TripPlan getTripPlan() { return tp; }
	public boolean isSuccessful() { return success; }
	
	private double getBearing(double lon1, double lon2, double lat1, double lat2){
		  double longitude1 = lon1;
		  double longitude2 = lon2;
		  double latitude1 = Math.toRadians(lat1);
		  double latitude2 = Math.toRadians(lat2);
		  double longDiff= Math.toRadians(longitude2-longitude1);
		  double y= Math.sin(longDiff)*Math.cos(latitude2);
		  double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);
		  return (Math.toDegrees(Math.atan2(y, x))+360)%360;
		}

	public void parse(Request requestParams) throws IOException, JSONException
	{
		Place from;
		Place to;
		DateFormat dateFormat;
		Date date;
		String dateRes, timeRes;
		URL url;
		String request, response;
		Scanner scan;
		JSONArray arr;
		JSONObject obj;
		String addrFrom, addrTo;
		OTPGeocoding geocoder;
		JSONObject geoResult, locationName;
		ArrayList <Itinerary> it;
		JSONObject data;
		Leg leg;
		AgencyAndId fromID, toID;
		String params = "";
		
		success = false;
		
		String f[] = requestParams.getFrom().split("%2C");
		String t[] = requestParams.getTo().split("%2C");
		Log.d("test", requestParams.getFrom());
		String mode = "";
		if (requestParams.getModes().getWalk()) mode = "&mode=WALK";
		if (requestParams.getModes().getBus()) mode = "&mode=TRANSIT";
		else mode = "&mode=WALK&mode=TRANSIT";
		
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		date = requestParams.getDateTime();
		dateRes = dateFormat.format(date);
		dateFormat = new SimpleDateFormat("HH:mm");
		timeRes = dateFormat.format(date);
		params = "?lat=" + f[0] + "&lon=" + f[1] + "&fromLat=" + f[0] + "&fromLon=" + f[1] +
				"&toLat=" + t[0] + "&toLon=" + t[1] + "&date=" +
				dateRes + "&time=" + timeRes +"&showIntermediateStops=true" + mode +
				"&maxWalkDistance=" + requestParams.getMaxWalkDistance();
		
		// Test a safe response:
		/*params = "?lat=59.377202890316006&lon=13.490111555730664&fromLat=59.377202890316006&fromLon=13.490111555730664&toLat=59.40588906074499&toLon=13.579284397097581&date=2013-10-24&time=09:07&showIntermediateStops=true";
		f[0] = "59.377202890316006";
		f[1] = "13.490111555730664";
		t[0] = "59.40588906074499";
		t[1] = "13.579284397097581";*/
		
		request = "http://itract.cs.kau.se:8081/proxy/api/transit/plan" + params;
		url = new URL(request);

		// read from the URL
		scan = new Scanner(url.openStream());
		if (scan == null) return;
		response = new String();
		while (scan.hasNext())
			response += scan.nextLine();
		scan.close();
		
		Log.d("test", "request: " + request);
		Log.d("test", "response: " + response);
		if (response.equals("[,]") || response.equals("[]") || response.equals("")) return;
		
		arr = new JSONArray(response);	
		obj = null;
		/*int i = 0;
		while (obj == null && i < 10) 
		{ 
			if (!arr.isNull(i)) 
				{ 
					obj = arr.getJSONObject(i);
				}
			i++;
		}*/
		int i = 0;
		// Loop through graphs.
		while (!arr.isNull(i))
		{
			obj = arr.getJSONObject(i);
			
			// Get names of start and end location.
			
			addrFrom = ""; addrTo = "";
			
			geocoder = new OTPGeocoding();
			geoResult = geocoder.reverseGeocoding(Double.parseDouble(f[0]), Double.parseDouble(f[1])); 
			try {
			    locationName = geoResult.getJSONArray("results").getJSONObject(0);
			    addrFrom = locationName.getString("formatted_address");
			} catch (JSONException e1) { e1.printStackTrace(); }
			
			geoResult = geocoder.reverseGeocoding(Double.parseDouble(t[0]), Double.parseDouble(t[1])); 
			try {
			    locationName = geoResult.getJSONArray("results").getJSONObject(0);
			    addrTo = locationName.getString("formatted_address");
			} catch (JSONException e1) { e1.printStackTrace(); }
			
			// Create a trip plan.
			from = new Place(Double.parseDouble(f[0]), Double.parseDouble(f[1]), addrFrom);
			to = new Place(Double.parseDouble(t[0]), Double.parseDouble(t[1]), addrTo);
			tp = new TripPlan(from, to, new Date());
			
			it = new ArrayList <Itinerary>();
			
			// Loop through and create itineraries.
			int k = 0;
			while (!obj.getJSONArray("data").isNull(k) && k < 10)
			{
				Log.d("test", "JSON it test");
				it.add(new Itinerary());
				long startwalk = 0, endwalk = 0;
				
				// Loop through and create legs.
				int l = 0;
				while (!obj.getJSONArray("data").getJSONArray(k).isNull(l) && l < 1000)
				{
					Log.d("test", "JSON leg test");
					// Leg data:
					data = obj.getJSONArray("data").getJSONArray(0).getJSONObject(l);
	
					from = new Place(data.getJSONObject("from").getDouble("lon"),
							data.getJSONObject("from").getDouble("lat"), data.getJSONObject("from").getString("name"));
					to = new Place(data.getJSONObject("to").getDouble("lon"),
							data.getJSONObject("to").getDouble("lat"), data.getJSONObject("to").getString("name"));
					leg = new Leg();
					leg.setFrom(from);
					leg.setTo(to);
					leg.setStartTime(data.getJSONObject("from").getString("departureTime"));
					leg.setEndTime(data.getJSONObject("to").getString("arrivalTime"));
					leg.setDistance(Double.parseDouble(data.getString("distance")));
					
					if (!data.getJSONObject("from").isNull("id"))
						fromID = new AgencyAndId(data.getJSONObject("from").getString("id"), data.getJSONObject("from").getString("id"));
					else fromID = new AgencyAndId("no ID", "no ID");
					
					if (!data.getJSONObject("to").isNull("id"))
						toID = new AgencyAndId(data.getJSONObject("to").getString("id"), data.getJSONObject("to").getString("id"));
					else toID = new AgencyAndId("no ID", "no ID");
					
					leg.getFrom().stopId = fromID;
					leg.getTo().stopId = toID;
					
					leg.mode = data.getString("mode");
					if (leg.mode.equals("WALK"))
					{
						if (startwalk == 0) startwalk = Long.parseLong(leg.getStartTime());
						endwalk = Long.parseLong(leg.getEndTime());
					}
					
					if (leg.mode.equals("BUS"))
					{
						startwalk = 0;
						leg.agencyName = data.getString("agencyName");
						leg.setHeadsign(data.getString("tripHeadsign"));
						leg.setRouteShortName(data.getString("routeShortName"));
						leg.routeId = data.getString("routeId");
						leg.tripId = data.getString("tripId");
					}
	
					leg.walkSteps = new ArrayList();
					
					// Loop through and create steps.
					int s = 0;
					if (!data.isNull("steps"))
					{
						while (!data.getJSONArray("steps").isNull(s))
						{
							WalkStep ws = new WalkStep();
							ws.lat = Double.parseDouble(data.getJSONArray("steps").getJSONObject(s).getString("lat"));
							ws.lon = Double.parseDouble(data.getJSONArray("steps").getJSONObject(s).getString("lon"));
							
							if (s > 0)
							{
								double b = getBearing(leg.walkSteps.get(s - 1).lon, ws.lon, leg.walkSteps.get(s - 1).lat, ws.lat);
								double index = b;
								if (index < 0)
								    index += 360;
								index = (int)(index / 45);
								
								ws.direction = ws.absoluteDirection.values()[(int) index].toString();
							}
							else 
							{
								double b = getBearing(Double.parseDouble(f[1]), ws.lon, Double.parseDouble(f[0]), ws.lat);
								double index = b;
								if (index < 0)
								    index += 360;
								index = (int)(index / 45);
								
								ws.direction = ws.absoluteDirection.values()[(int) index].toString();
							}
							
							geoResult = geocoder.reverseGeocoding(ws.lat, ws.lon); 
							try {
							    locationName = geoResult.getJSONArray("results").getJSONObject(0);
							    ws.streetName = locationName.getString("formatted_address");
							} catch (JSONException e1) { e1.printStackTrace(); }
							
							leg.walkSteps.add(ws);
							s++;
						}
					}
					else if (!data.isNull("intermediates"))
					{
						while (!data.getJSONArray("intermediates").isNull(s))
						{
							WalkStep ws = new WalkStep();
							ws.lat = Double.parseDouble(data.getJSONArray("intermediates").getJSONObject(s).getString("lat"));
							ws.lon = Double.parseDouble(data.getJSONArray("intermediates").getJSONObject(s).getString("lon"));
							leg.walkSteps.add(ws);
							s++;
						}
					} // End step loop
					leg.duration = Long.parseLong(leg.getEndTime()) - Long.parseLong(leg.getStartTime());
					it.get(k).addLeg(leg);
					l++;
				} // End leg loop.
				Calendar startcal = Calendar.getInstance();
				Calendar endcal = Calendar.getInstance();
				startcal.setTimeInMillis(Long.parseLong(it.get(k).legs.get(0).getStartTime()));
				it.get(k).startTime = startcal;
				endcal.setTimeInMillis(Long.parseLong(it.get(k).legs.get(l - 1).getEndTime()));
				it.get(k).endTime = endcal;
				
				it.get(k).walkTime = endwalk - startwalk;
				it.get(k).duration = Long.parseLong(it.get(k).legs.get(it.get(k).legs.size()-1).getEndTime()) - Long.parseLong(it.get(k).legs.get(0).getStartTime());
				tp.addItinerary(it.get(k));
				k++;
			} // End itinerary loop.
			i++;
		} // End graph loop.
		success = true;
	}
}