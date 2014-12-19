package edu.usf.cutr.opentripplanner.android.tasks;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Place;

import edu.usf.cutr.opentripplanner.android.fragments.MainFragment;
import edu.usf.cutr.opentripplanner.android.fragments.PosSubscriber;
import edu.usf.cutr.opentripplanner.android.fragments.TripSubscriber;
import edu.usf.cutr.opentripplanner.android.listeners.BusPosSubscriber;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class ItractTripSubscriber extends AsyncTask<String[], Void, ItractTripSubscriber>
{
	
	private MainFragment listener;
	private List<Itinerary> itineraries;
	private Place lastLoc;
	public boolean waiting;
	
	public Place getLastLoc() { return lastLoc; }
	
	public ItractTripSubscriber(MainFragment listener, List<Itinerary> it) 
	{
        this.listener = listener;
        itineraries = it;
	}
	
	@Override
	protected ItractTripSubscriber doInBackground(String[]... params) {
		//pos = new BusPosition();
		waiting = true;
		Log.d("test", "waiting . . .");
		while(waiting)
		{
			try {
				Thread.sleep(10);
				//Log.d("loop", "waiting " + waiting);
			} catch (InterruptedException e) { Log.d("test", "sleep err");	}
		}
		//subscriber = new Sub(params[0], this); // String array are the channels.
		//listener.setSub(subscriber);
		//try {
		//	subscriber.runSub(doSub);
		//} catch (UnknownHostException e) { e.printStackTrace();	Log.d("test", "unknown host");} catch (InterruptedException e) { e.printStackTrace(); Log.d("test", "interrupt");}
		Log.d("test", "waiting " + waiting);
		Log.d("test", "return!");
		return this;
	}
	
	public void callback(String ch, String msg)
	{
		Log.d("test", "in callback");
		if (!waiting) return;
		// Get the trip ID.
		String trip[] = ch.split("\\.");
		// Find the leg for the current trip.
		if (ch.endsWith(".time"))
		{
			Log.d("test", "compare time");
			for (int i = 0; i < itineraries.get(0).legs.size() - 1; i++)
			{
				Log.d("test", itineraries.get(0).legs.get(i).tripId + " ?= " + trip[0]);
				if (itineraries.get(0).legs.get(i).tripId.equals(trip[0]))
				{
					Log.d("test", Long.parseLong(msg) + " compared with " + Long.parseLong(itineraries.get(0).legs.get(i + 1).getStartTime()));
					if (Long.parseLong(msg) > Long.parseLong(itineraries.get(0).legs.get(i + 1).getStartTime()))
					{
						lastLoc = itineraries.get(0).legs.get(i).getTo();
						waiting = false;//Log.d("test", Long.parseLong(msg) + " is larger than " + Long.parseLong(itineraries.get(0).legs.get(i + 1).getStartTime()));
					}
					return;
				}
			}
		}
		if (ch.endsWith(".delay"))
		{
			Log.d("test", "compare with delay");
			for (int i = 0; i < itineraries.get(0).legs.size() - 1; i++)
			{
				Log.d("test", itineraries.get(0).legs.get(i).tripId + " ?= " + trip[0]);
				if (itineraries.get(0).legs.get(i).tripId.equals(trip[0]))
				{
					Log.d("test", Long.parseLong(itineraries.get(0).legs.get(i).getEndTime()) + " + " + Long.parseLong(msg) + " compared with " + Long.parseLong(itineraries.get(0).legs.get(i + 1).getStartTime()));
					if (Long.parseLong(itineraries.get(0).legs.get(i).getEndTime()) + Long.parseLong(msg) > Long.parseLong(itineraries.get(0).legs.get(i + 1).getStartTime()))
					{
						lastLoc = itineraries.get(0).legs.get(i).getTo();
						waiting = false;
					}
					return;
				}	
			}
		}
			
	}
	
	public void returnSub(TripSubscriber sub)
	{
		listener.setTripSub(sub);
	}
	
	@Override
	protected void onPostExecute(ItractTripSubscriber sub) 
	{
        //listener.onTripUpdateNotify(sub);
	}
}

