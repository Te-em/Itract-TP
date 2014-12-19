import java.util.ArrayList;
import java.util.List;

import com.google.transit.realtime.GtfsRealtime;


public class TripUpdate 
{
	String tripId;
	
	class StopUpdate
	{
		int arrivalDelay;
		long arrivalTime;
		int departureDelay;
		long departureTime;
		int stopSeq;
	}
	
	GtfsRealtime.TripUpdate tripUpdate;
	ArrayList<StopUpdate> stopUpdates;
	
	public TripUpdate(GtfsRealtime.TripUpdate tripUpdate, long timestamp)
	{
		List<GtfsRealtime.TripUpdate.StopTimeUpdate> stopTimes = tripUpdate.getStopTimeUpdateList();
		StopUpdate stop;
		this.tripUpdate = tripUpdate;
		tripId = tripUpdate.getTrip().getTripId();
		stopUpdates = new ArrayList<StopUpdate>();
		for (int i = 0; i < stopTimes.size(); i++)
		{
			stop = new StopUpdate();
			stop.arrivalDelay = stopTimes.get(i).getArrival().getDelay();
			stop.arrivalTime = stopTimes.get(i).getArrival().getTime();
			stop.departureDelay = stopTimes.get(i).getDeparture().getDelay();
			stop.departureTime = stopTimes.get(i).getDeparture().getTime();
			stop.stopSeq = stopTimes.get(i).getStopSequence();
			stopUpdates.add(stop);
		}
	}
	
	public TripUpdate() {}
		
	public ArrayList<StopUpdate> initStops()
	{
		return new ArrayList<StopUpdate>();
	}
	
	public StopUpdate addStop(int ad, long at, int dd, long dt, int ss)
	{
		StopUpdate stop;

		stop = new StopUpdate();
		stop.arrivalDelay = ad;
		stop.arrivalTime = at;
		stop.departureDelay = dd;
		stop.departureTime = dt;
		
		return stop;
	}
}
