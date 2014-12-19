package edu.usf.cutr.opentripplanner.android.tasks;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.fragments.PosSubscriber;
import edu.usf.cutr.opentripplanner.android.listeners.BusPosSubscriber;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class ItractSubscriber extends AsyncTask<String[], Void, ItractSubscriber>
{
	private PosSubscriber subscriber;
	private BusPosSubscriber listener;
	private boolean doSub;
	public boolean waiting;
	
	public class BusPosition
	{
		public Double lat;
		public Double lon;
		public String agencyID;
		public String routeID;
		public String vehicleID;
	}
	ArrayList<BusPosition> positions;
	public ArrayList<BusPosition> getPositions() { return positions; }
	
	public ItractSubscriber(BusPosSubscriber listener, boolean subscribe) 
	{
        this.listener = listener;
        doSub = subscribe;
	}
	
	@Override
	protected ItractSubscriber doInBackground(String[]... params) {
		//pos = new BusPosition();
		positions = new ArrayList<BusPosition>();
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
	
	public void returnSub(PosSubscriber sub)
	{
		listener.setSub(sub);
	}
	
	public void callback(String ch, String msg)
	{
		if (!waiting) return;
		System.out.println("callback got " + msg);
		//System.out.println("Message arrived in asynch : " + ch + " : message : " + msg);
		if (ch.endsWith("pubStatus") && msg.equals("New")) positions.add(new BusPosition());
		else if (ch.endsWith("pubStatus") && msg.equals("Complete")) waiting = false;
		else if (!positions.isEmpty())
		{
			if (ch.endsWith("lat")) positions.get(positions.size() - 1).lat = Double.parseDouble(msg);
			else if (ch.endsWith("lon")) positions.get(positions.size() - 1).lon = Double.parseDouble(msg);
			else if (ch.endsWith("agencyId")) positions.get(positions.size() - 1).agencyID = msg;
			else if (ch.endsWith("routeId")) positions.get(positions.size() - 1).routeID = msg;
			else if (ch.endsWith("vehicleId")) positions.get(positions.size() - 1).vehicleID = msg;
		}
		//System.out.println("Lat/lon: " + positions.get(positions.size() - 1).lat + "," + positions.get(positions.size() - 1).lon);
		//positions.add(pos);
	}
	@Override
	protected void onPostExecute(ItractSubscriber sub) {
		System.out.println("is cancelled " + isCancelled());
		//sub.execute();
        listener.onItractBusPosNotify(sub);
	}
}
