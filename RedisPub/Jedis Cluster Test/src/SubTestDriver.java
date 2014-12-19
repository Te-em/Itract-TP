import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;

/* Test subscriber for Redis */
public class SubTestDriver 
{	
	// Some fake OTP classes that are necessary for trip-update subscriptions.
	static class Place
	{
		double lon, lat;
		String stopId;
	}
	
	static class Leg
	{
		String routeId, tripId;
		Place from, to;
		public int arrivalDelay;
		public long departureDelay;
		public void setStartTime(String long1) {
			// TODO Auto-generated method stub
			
		}
		public void setEndTime(String long1) {
			// TODO Auto-generated method stub
			
		}
		public String getEndTime() {
			// TODO Auto-generated method stub
			return null;
		}
		public String getStartTime() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	static class Itinerary
	{
		ArrayList<Leg> legs;
	}
	
	static ArrayList<Itinerary> its;
	
	// Get vehicle IDs from Itract.
	public static String[] getVehicleID(String[] latlon)
	{
		String request, response, v = "";
		URL url;
		Scanner scan;
		JSONArray arr, data, vehicles;
		// This range determines the area for showing vehicles.
		// 0.04 is a quite small area.
		double range = 0.04;
		try {
			// The first request guarantees a response and gives about 200 vehicle positions.
			request = "http://itract.cs.kau.se:8081/proxy/api/transit/vehicleLocations?lat=59.376818852286526&lon=13.571013503173836";
			// This request is based on co-ordinates from a file and the range variables. 
			request = "http://itract.cs.kau.se:8081/proxy/api/transit/vehicleLocations?lat=" + latlon[0] + "&lon=" + latlon[1]
			+ "&southWestLon=" + (Double.parseDouble(latlon[1]) - range * 3) + "&southWestLat=" + (Double.parseDouble(latlon[0]) - range * 4)
			+ "&northEastLon=" + (Double.parseDouble(latlon[1]) + range * 3) + "&northEastLat=" + (Double.parseDouble(latlon[0]) + range * 4);
			url = new URL(request);
			// read from the URL
			scan = new Scanner(url.openStream());
			response = new String();
			while (scan.hasNext())
				response += scan.nextLine();
			scan.close();
			System.out.println("request: " + request);
			System.out.println("response: " + response);
			
			// This is an empty response from the Itract server.
			if (response.equals("[,]") || response.equals("[]") || response.equals("")) 
			{
				return new String[] {"[,]"};
			}
			
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
						v += vehicles.getJSONObject(k).getString("vehicleId") + ",";
						k++;
					} // End vehicle loop.
					d++;
				}// End agency loop.
				i++;
			} // End graph loop.
			
		} catch (IOException e) {  } catch (JSONException e) {  }
		
		return v.split(",");
	}
	
	public static void main(String[] args) throws IOException
	{
		Random rnd = new Random();
		//int num_vehicles = rnd.nextInt(100);
		String vehicleID[];// = new String[num_vehicles];
		Itinerary it;
		String tripID = null;
		String line;
		List<String> lines;
		String[] latlon;
		BufferedReader reader;
		Thread t;

		lines = new ArrayList<String>();
		// Read some co-ordinates from file.
		reader = new BufferedReader(new FileReader("coordinates"));
		lines = new ArrayList<String>();
		
		line = reader.readLine();

		while( line != null ) 
		{
		    lines.add(line);
		    line = reader.readLine();
		}

		line = lines.get(rnd.nextInt(lines.size()));
		latlon = line.split(",");
		
		// If command line argument is pos<subs>, where <subs> is the number of subscribers.
		if (args.length > 0)
			{ 
				try
					{
						int subs = Integer.parseInt(args[0]); 
						System.out.println(subs + " subscribers!");
						for (int i = 0; i < subs; i++)
						{
							// Get vehicle IDs for subscription.
							vehicleID = getVehicleID(latlon);
							System.out.println(vehicleID.clone().length + " channels for subscriber " + i);
							// Run subscriber.
							new Thread(new PosSubscriber(vehicleID, new SubTestDriver(), i)).start();
						}
					} catch (NumberFormatException e) { System.out.println("Argument must be a number!"); }
			}
		else System.out.println("Please give no. of subscribers as command line argument.");
		
		/* Vehicle positions should be enough for performance testing.
		 * Other testing should be done on the Android application,
		 * so the code below is not really needed.
		 */
		
		/*
		reader = new BufferedReader(new FileReader("TripIDs"));
		
		lines.clear();
		lines = new ArrayList<String>();
		
		line = reader.readLine();

		while( line != null ) 
		{
		    lines.add(line);
		    line = reader.readLine();
		}
		
		tripID = lines.get(rnd.nextInt(lines.size()));
		
		TripSubscriber tripSub = new TripSubscriber(new String[] { tripID }, new SubTestDriver());
		if (args.length > 0 && (args[0].equals("trips") ||
			args.length > 1 && 	args[1].equals("trips") ||
			args.length > 2 && args[2].equals("trips"))) 
			{
				for (int i = 0; i < 100; i++) new Thread(tripSub).start(); 
			}
		
		reader = new BufferedReader(new FileReader("RouteIDs"));
		
		lines.clear();
		lines = new ArrayList<String>();
		
		line = reader.readLine();

		while( line != null ) 
		{
		    lines.add(line);
		    line = reader.readLine();
		}
		
		it = new Itinerary();
		it.legs = new ArrayList<Leg>();
		
		for (int i = 0; i < rnd.nextInt(6) + 1; i++)
		{
			Leg l = new Leg();
			l.routeId = lines.get(rnd.nextInt(lines.size()));
			it.legs.add(l);
		}
		
		it.legs.get(0).from = new Place();
		it.legs.get(0).from.stopId = "testStop";
		
		its = new ArrayList<Itinerary>();
		its.add(it);
		
		AlertSubscriber alertSub = new AlertSubscriber(its, new SubTestDriver());
		if (args.length > 0 && (args[0].equals("alerts") ||
			args.length > 1 && args[1].equals("alerts") ||
			args.length > 2 && args[2].equals("alerts"))) 
			{ t = new Thread(alertSub); t.start(); }*/
	}

}
