import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;


public class TripUpdateTest 
{
	
	ArrayList<TripUpdate> tripUpdates;
	ArrayList<AlertTest> alerts;
	
	public List<AlertTest> testAlert(String routeID)
	{
		Random rnd = new Random();
		List<String> lines = null;
		String[] params;
		String line;
		
		BufferedReader reader;
		try 
		{ 
			reader = new BufferedReader(new FileReader("Alerts"));
			lines = new ArrayList<String>();

			line = reader.readLine();

			while( line != null ) {
			    lines.add(line);
			    line = reader.readLine();
			}		
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		alerts = new ArrayList<AlertTest>();
		
		for (int i = 0; i < rnd.nextInt(6); i++)
		{
			line = lines.get(rnd.nextInt(lines.size()));
			params = line.split(",");
			
			AlertTest alert = new AlertTest();
			alert.ID = "test" + i;
			alert.routeID = new ArrayList<String>();
			alert.stopID = new ArrayList<String>();
			alert.agencyID = new ArrayList<String>();
			alert.routeID.add("testRoute" + i);
			//System.out.println(alert.getEffect().name());
			//System.out.println(alert.getDescriptionText().getTranslation(0));
			alert.cause = params[0];
			alert.effect = params[1];
			alert.text = params[2];
			alert.time = "time";
			alerts.add(alert);
		}
		return alerts;
	}
	
	public List<TripUpdate> testFileDelay(String tripID)
	{
		Random rnd = new Random();
		List<String> lines = null;
		String[] params;
		String line;
		
		BufferedReader reader;
		try 
		{ 
			reader = new BufferedReader(new FileReader("TripUpdates"));
			lines = new ArrayList<String>();

			line = reader.readLine();

			while( line != null ) {
			    lines.add(line);
			    line = reader.readLine();
			}		
		} 
		catch (IOException e) { e.printStackTrace(); }
		
		tripUpdates = new ArrayList<TripUpdate>();
		
		for (int i = 0; i < 100; i++)
		{
			TripUpdate tp = new TripUpdate();
			tp.tripId = tripID + i;
			tp.stopUpdates = tp.initStops();
			
			line = lines.get(rnd.nextInt(lines.size()));
			params = line.split(",");
			
			tp.stopUpdates.add(tp.addStop(Integer.parseInt(params[0]), Long.parseLong(params[1]), Integer.parseInt(params[2]), Long.parseLong(params[3]), 1));
			
			line = lines.get(rnd.nextInt(lines.size()));
			params = line.split(",");
		
			tp.stopUpdates.add(tp.addStop(Integer.parseInt(params[0]), Long.parseLong(params[1]), Integer.parseInt(params[2]), Long.parseLong(params[3]), 2));
			
			tripUpdates.add(tp);
		}
		
		return tripUpdates;
	}
	
	public List<TripUpdate> testRandomDelay(String tripID0, String tripID1)
	{
		Random rnd = new Random();
		
		tripUpdates = new ArrayList<TripUpdate>();
		TripUpdate tp = new TripUpdate();
		tp.tripId = tripID0;
		tp.stopUpdates = tp.initStops();
		tp.stopUpdates.add(tp.addStop(rnd.nextInt(1800), 0, rnd.nextInt(1800), 0, 1));
		tripUpdates.add(tp);
		
		if (rnd.nextInt(1) == 1)
		{
			tp = new TripUpdate();
			tp.tripId = tripID1;
			tp.stopUpdates = tp.initStops();
			tp.stopUpdates.add(tp.addStop(rnd.nextInt(1800), 0, rnd.nextInt(1800), 0, 2));
			tripUpdates.add(tp);
		}
		
		return tripUpdates;
	}
	
	public List<TripUpdate> testDepArrDelay(String tripID0, String tripID1)
	{
		tripUpdates = new ArrayList<TripUpdate>();
		TripUpdate tp = new TripUpdate();
		tp.tripId = tripID0;
		tp.stopUpdates = tp.initStops();
		tp.stopUpdates.add(tp.addStop(60 * 60, 0, 0, 0, 1));
		tripUpdates.add(tp);
		
		tp = new TripUpdate();
		tp.tripId = tripID1;
		tp.stopUpdates = tp.initStops();
		tp.stopUpdates.add(tp.addStop(0, 0, 60 * 60, 0, 2));
		tripUpdates.add(tp);
		
		return tripUpdates;
	}
	
	public List<TripUpdate> testTripUpdate(String tripID)
	{
		tripUpdates = new ArrayList<TripUpdate>();
		TripUpdate tp = new TripUpdate();
		tp.tripId = tripID;
		tp.stopUpdates = tp.initStops();
		tp.stopUpdates.add(tp.addStop(60 * 60, 0, 0, 0, 1));
		tripUpdates.add(tp);
		return tripUpdates;
	}
	
	public List<TripUpdate> testDepartureDelay(String tripID)
	{
		tripUpdates = new ArrayList<TripUpdate>();
		TripUpdate tp = new TripUpdate();
		tp.tripId = tripID;
		tp.stopUpdates = tp.initStops();
		tp.stopUpdates.add(tp.addStop(0, 0, 5 * 60, 0, 1));
		tripUpdates.add(tp);
		return tripUpdates;
	}
	
	public List<TripUpdate> testArrivalDelay(String tripID)
	{
		tripUpdates = new ArrayList<TripUpdate>();
		TripUpdate tp = new TripUpdate();
		tp.tripId = tripID;
		tp.stopUpdates = tp.initStops();
		tp.stopUpdates.add(tp.addStop(5 * 60, 0, 0, 0, 1));
		tripUpdates.add(tp);
		return tripUpdates;
	}
}
