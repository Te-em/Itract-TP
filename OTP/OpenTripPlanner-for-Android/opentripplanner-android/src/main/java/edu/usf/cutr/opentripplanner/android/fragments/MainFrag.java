package edu.usf.cutr.opentripplanner.android.fragments;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.miscwidgets.widget.Panel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.TileOverlay;

import edu.usf.cutr.opentripplanner.android.OTPApp;
import edu.usf.cutr.opentripplanner.android.fragments.PosSubscriber.BusPosition;
import edu.usf.cutr.opentripplanner.android.listeners.BusPosSubscriber;
import edu.usf.cutr.opentripplanner.android.listeners.DateCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.MetadataRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OTPGeocodingListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnFragmentListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnItractBusPosCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnItractBusStopsCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.OnItractDeparturesCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.ServerSelectorCompleteListener;
import edu.usf.cutr.opentripplanner.android.listeners.TripRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.tasks.ItractBusPos;
import edu.usf.cutr.opentripplanner.android.tasks.ItractBusStops;
import edu.usf.cutr.opentripplanner.android.tasks.ItractDepartures;
import edu.usf.cutr.opentripplanner.android.tasks.ItractSubscriber;
import edu.usf.cutr.opentripplanner.android.util.RangeSeekBar;
import edu.usf.cutr.opentripplanner.android.util.RangeSeekBar.OnRangeSeekBarChangeListener;

// This class was added to make MainFragment a little smaller.
public class MainFrag implements
	OnItractBusStopsCompleteListener,
	OnItractBusPosCompleteListener,
	//BusPosSubscriber,
	OnItractDeparturesCompleteListener
{
	private ItractBusStops busStops;
	private ItractBusStops stops;
	ItractSubscriber is;
	private String[] vehicleID;

	//boolean willSubscribe;
	
	CameraPosition lastCamPos0;
	CameraPosition lastCamPos1;
	private boolean cameraMoved;
	
	ArrayList<Marker> stopMarkers;
	ArrayList<Marker> posMarkers;

	PosSubscriber subscriber;
	private PosSubscriber mainSub;
	public MainFragment mf;
	
	public MainFrag(MainFragment mf)
	{
		this.mf = mf;
	}
	
	public void setSub(PosSubscriber s)
	{
		subscriber = s;
	}
	
	public void initMarkers()
	{
		stopMarkers = new ArrayList<Marker>();
		posMarkers = new ArrayList<Marker>();
	}
	
	// Place the stop markers on the map.
	public void onItractBusStopsComplete(ItractBusStops stops)
	{
		this.stops = stops;
		// Place markers:
		for (int i = 0; i < stops.getStops().size(); i++)
		{
			Marker marker = (mf.mMap.addMarker(new MarkerOptions()
    	     .position(new LatLng(stops.getStops().get(i).lat, stops.getStops().get(i).lon))
    	     .title(stops.getStops().get(i).name)
    	     .icon(BitmapDescriptorFactory.fromAsset("busstop.png"))));
			stops.getStops().get(i).marker = marker.getId();
			stopMarkers.add(marker);
		}
	}
	public void showBusStopsAux(boolean update)
	{
		String url;
		CameraPosition cp;
		double camLat, camLon, oldCamLat, oldCamLon;
		double range, updateRange;
		WeakReference<Activity> weakContext = new WeakReference<Activity>(mf.getActivity());
		//OnItractBusStopsCompleteListener fm = this;
		
		cp = mf.mMap.getCameraPosition();
		camLat = cp.target.latitude;
		camLon = cp.target.longitude;
		// The area size where stops are to be displayed.
		range = 0.09;
		// The size varies with the zoom (need a formula for this . . .)
		if (cp.zoom > 13) range = 0.03;
		else if (cp.zoom > 12.75) range = 0.035;
		else if(cp.zoom > 12.5) range = 0.04;
		else if(cp.zoom > 12.25) range = 0.045;
		else if(cp.zoom > 12) range = 0.05;
		else if(cp.zoom > 11.75) range = 0.06;
		else if(cp.zoom > 11.5) range = 0.07;
		else if(cp.zoom > 11.25) range = 0.08;
		else if (cp.zoom > 11) range = 0.09;
		
		// If the camera has moved.
		if (update)
		{
			oldCamLat = lastCamPos0.target.latitude;
			oldCamLon = lastCamPos0.target.longitude;
			if (mf.isWidescreen())
			{
				if (!(oldCamLat - camLat > range || oldCamLat - camLat < range * -1
						|| oldCamLon - camLon > range * 4 || oldCamLon - camLon < range * -4))
							return; // Hasn't moved far enough.
			}
			else
			{
				if (!(oldCamLat - camLat > range * 4 || oldCamLat - camLat < range * -4
						|| oldCamLon - camLon > range  * 3 || oldCamLon - camLon < range * -3))
							return; // Hasn't moved far enough.
			}
		}
		lastCamPos0 = cp;
		
		// Remove previous markers.
		for (int i = 0; i < stopMarkers.size(); i++)
		{
			stopMarkers.get(i).remove();
			
		}
		stopMarkers.clear();
		
		// Make request.
		if (mf.isWidescreen())
		{
			url = "http://itract.cs.kau.se:8081/proxy/api/transit/stopsInArea?lat="
					+ camLat +
					"&lon=" + camLon +
					"&southWestLat=" + (camLat - range) + "&southWestLon=" + (camLon - range * 4) +
					"&northEastLat=" + (camLat + range) + "&northEastLon=" + (camLon + range * 4);
		}
		else
		{
			url = "http://itract.cs.kau.se:8081/proxy/api/transit/stopsInArea?lat="
					+ camLat +
					"&lon=" + camLon +
					"&southWestLat=" + (camLat - range * 4) + "&southWestLon=" + (camLon - range * 3) +
					"&northEastLat=" + (camLat + range * 4) + "&northEastLon=" + (camLon + range * 3);
		}

		new ItractBusStops(this, weakContext).execute(url);
	}
	
	public void showBusStops(CheckedTextView item, int id0, int id1)
	{
		String url;
		double range;
		WeakReference<Activity> weakContext = new WeakReference<Activity>(mf.getActivity());
		
		if (!mf.getIsChecked()[id0]) 
    	{
			mf.setIsChecked(false, id1);
			mf.ddlBusStops.setItemChecked(id1, false);
    		
			// Remove previous markers.
			for (int i = 0; i < stopMarkers.size(); i++)
			{
				stopMarkers.get(i).remove();
				
			}
			stopMarkers.clear();
			
    		item.setChecked(true);
    		mf.setIsChecked(true, id0);
    		
    		// If 'Show stops':
    		if (id0 == 0) showBusStopsAux(false);

    		// If 'Show stops in area':
    		else
    		{
    			range = 0.01;
    			if (mf.isWidescreen())
    			{
	    			url = "http://itract.cs.kau.se:8081/proxy/api/transit/stopsInArea?lat="
	    					+ mf.getLastLocation().latitude +
	    					"&lon=" + mf.getLastLocation().longitude +
	    					"&southWestLat=" + (mf.getLastLocation().latitude - range) + "&southWestLon=" + (mf.getLastLocation().longitude - range * 4) +
	    					"&northEastLat=" + (mf.getLastLocation().latitude + range) + "&northEastLon=" + (mf.getLastLocation().longitude + range * 4);
    			}
    			else
    			{
	    			url = "http://itract.cs.kau.se:8081/proxy/api/transit/stopsInArea?lat="
	    					+ mf.getLastLocation().latitude +
	    					"&lon=" + mf.getLastLocation().longitude +
	    					"&southWestLat=" + (mf.getLastLocation().latitude - range * 4) + "&southWestLon=" + (mf.getLastLocation().longitude - range * 3) +
	    					"&northEastLat=" + (mf.getLastLocation().latitude + range * 4) + "&northEastLon=" + (mf.getLastLocation().longitude + range * 3);
    			}
    			
    			new ItractBusStops(this, weakContext).execute(url);
    		}
    	}	
	}
	
	public void onItractDeparturesComplete(ItractDepartures deps)
	{
		Calendar cal = Calendar.getInstance();
    	String str = "";

		for (int i = 0; i < deps.getDepartures().size(); i++)
    	{	
    		cal.setTimeInMillis(Long.parseLong(deps.getDepartures().get(i).time));
    		str += "Bus " + deps.getDepartures().get(i).routeShortName + " "
    				+ deps.getDepartures().get(i).tripHeadsign + "\n"
    				+ new SimpleDateFormat("dd MMM yyyy HH:mm").format(cal.getTime())
    				+ "\n_______________________\n\n";                       		
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(mf.getActivity());
    	builder.setMessage(str)
			.setTitle("Routes");
    	AlertDialog dialog = builder.create();
    	dialog.setCanceledOnTouchOutside(true);
    	dialog.show();
	}
	
	public void onStopMarkerClick(Marker marker) 
    {
		WeakReference<Activity> weakContext = new WeakReference<Activity>(mf.getActivity());
    	
    	// Look for stop of this marker.
    	int j = 0;
    	while (!stops.getStops().get(j).marker.equals(marker.getId()) && j < stops.getStops().size()) j++;
    	
    	new ItractDepartures(this, weakContext).execute(stops.getStops().get(j));
    }
	
	public Thread t;
	Handler handler = new Handler(new Handler.Callback() {

	    @Override
	    public boolean handleMessage(Message msg) {
	    	onItractBusPosNotify((ArrayList<BusPosition>) msg.obj);
	        return false;
	    }
	});
	
	public void onItractBusPosNotify(ArrayList<BusPosition> positions)
	{			
		if (positions == null) return;
		
		for (int i = 0; i < posMarkers.size(); i++)
		{
			posMarkers.get(i).remove();		
		}
	
		// Set position markers.
		for (int i = 0; i < positions.size(); i++)
		{
			Log.d("loop", "" + i);
			//Log.d("test", "set marker");
			//Log.d("test", "pos: " + sub.getPositions().get(i).lat + "," + sub.getPositions().get(i).lon);
			//Log.d("test", "agencyID: " + sub.getPositions().get(i).agencyID);
			//Log.d("test", "routeID: " + sub.getPositions().get(i).routeID);
			//Log.d("test", "vehicleID: " + sub.getPositions().get(i).vehicleID);
			if (positions.get(i).lat == null || positions.get(i).lon == null) continue;
			posMarkers.add(mf.mMap.addMarker(new MarkerOptions()
		     .position(new LatLng(positions.get(i).lat, positions.get(i).lon))
		     .title("Agency ID: " + positions.get(i).agencyID
		    		 + " | Route ID: " + positions.get(i).routeID
		    		 + " | Vehicle ID: " + positions.get(i).vehicleID)
		     .icon(BitmapDescriptorFactory.fromAsset("bus.png"))));
			//.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
		}
		positions.clear();
	}
	
	public void onItractBusPosComplete(ItractBusPos position)
	{
		if (subscriber != null && subscriber.isSubscribed()) 
		{
			subscriber.unsubscribe();
			
			try { t.join();} catch (InterruptedException e1) { }
			
			for (int i = 0; i < posMarkers.size(); i++)
			{
    			posMarkers.get(i).remove();
			}
		}
		
		// We can set the positions here immediately, or do a Redis get query in the subscriber
		// (the former is faster, since the positions are already at hand)
		for (int i = 0; i < position.getPositions().size(); i++)
		{
			if (position.getPositions().get(i).lat == null || position.getPositions().get(i).lon == null) continue;
			posMarkers.add(mf.mMap.addMarker(new MarkerOptions()
		     .position(new LatLng(position.getPositions().get(i).lat, position.getPositions().get(i).lon))
		     .title("Agency ID: " + position.getPositions().get(i).agencyID
		    		 + " | Route ID: " + position.getPositions().get(i).routeID
		    		 + " | Vehicle ID: " + position.getPositions().get(i).vehicleID)
		     .icon(BitmapDescriptorFactory.fromAsset("bus.png"))));
			//.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
		}
		
		// Make subscription topics.
		vehicleID = new String[position.getPositions().size()];
		for (int i = 0; i < position.getPositions().size(); i++)
		{
			vehicleID[i] = position.getPositions().get(i).vehicleID;
		}
		
		// Start subscriber.
		mainSub = new PosSubscriber(vehicleID, this);
		t = new Thread(mainSub); t.start();
	}
	
	public void showBusPos(boolean update)
	{
		WeakReference<Activity> weakContext = new WeakReference<Activity>(mf.getActivity());
		
		CameraPosition cp = mf.mMap.getCameraPosition();
		double oldCamLat, oldCamLon;
		double camLat = cp.target.latitude;
		double camLon = cp.target.longitude;
		double range;
		
		cp = mf.mMap.getCameraPosition();
		camLat = cp.target.latitude;
		camLon = cp.target.longitude;
		// Size of area for displaying positions.
		range = 0.09;
		// Area size varies with camera zoom. (Need some better way to determine this value.)
		if (cp.zoom > 13) range = 0.03;
		else if (cp.zoom > 12.75) range = 0.035;
		else if(cp.zoom > 12.5) range = 0.04;
		else if(cp.zoom > 12.25) range = 0.045;
		else if(cp.zoom > 12) range = 0.05;
		else if(cp.zoom > 11.75) range = 0.06;
		else if(cp.zoom > 11.5) range = 0.07;
		else if(cp.zoom > 11.25) range = 0.08;
		else if (cp.zoom > 11) range = 0.09;
		
		// Has camera moved?
		if (update)
		{
			oldCamLat = lastCamPos1.target.latitude;
			oldCamLon = lastCamPos1.target.longitude;
			if (mf.isWidescreen())
			{
				if (!(oldCamLat - camLat > range || oldCamLat - camLat < range * -1
						|| oldCamLon - camLon > range * 4 || oldCamLon - camLon < range * -4))
							return; // Not far enough for update.
			}
			else
			{
				if (!(oldCamLat - camLat > range * 4|| oldCamLat - camLat < range * -4
						|| oldCamLon - camLon > range * 3 || oldCamLon - camLon < range * -3))
							return; // Not far enough for update.
			}
		}
		lastCamPos1 = cp;

		// Make request.
		String url = "";
		if (mf.isWidescreen())
		{
			url = "http://itract.cs.kau.se:8081/proxy/api/transit/vehicleLocations?lat=" + camLat + "&lon=" + camLon
						+ "&southWestLon=" + (camLon - range * 4) + "&southWestLat=" + (camLat - range)
						+ "&northEastLon=" + (camLon + range * 4) + "&northEastLat=" + (camLat + range);
		}
		else
		{
			url = "http://itract.cs.kau.se:8081/proxy/api/transit/vehicleLocations?lat=" + camLat + "&lon=" + camLon
						+ "&southWestLon=" + (camLon - range * 3) + "&southWestLat=" + (camLat - range * 4)
						+ "&northEastLon=" + (camLon + range * 3) + "&northEastLat=" + (camLat + range * 4);
		}
		new ItractBusPos(this, weakContext).execute(url);
	}
	
	// The buttons in the left-hand menu.
	public void buttonPressed(long id, CheckedTextView item)
	{
		if (id == 0) 
    	{
			// Show bus stops.
    		if (!mf.getIsChecked()[0])
    			showBusStops(item, 0, 1);
    		// Remove stop markers.
    		else
    		{
    			item.setChecked(false);
    			mf.setIsChecked(false, 0);
        		
        		// Remove markers.
        		for (int i = 0; i < stopMarkers.size(); i++)
        			stopMarkers.get(i).remove();
        		stopMarkers.clear();
    		}
    	}
    	else if (id == 1)
    	{
    		// Show stops in area.
    		if (!mf.getIsChecked()[1]) showBusStops(item, 1, 0);
    		// Remove stops.
    		else
    		{
    			item.setChecked(false);
    			mf.setIsChecked(false, 1);
        		
        		// Remove markers.
        		for (int i = 0; i < stopMarkers.size(); i++)
        			stopMarkers.get(i).remove();
        		stopMarkers.clear();
    		}
    	}
    	else if (id == 2)
    	{
    		// Show positions.
    		if (!mf.getIsChecked()[2]) 
    		{
    			mf.setIsChecked(true, 2); 
    			showBusPos(false); 
    		}
    		// Stop subscribing and remove position markers.
    		else
    		{
    			mf.setIsChecked(false, 2);
    			if (subscriber != null && subscriber.isSubscribed()) 
    			{
    				subscriber.unsubscribe();
    				
    				try { t.join(); } catch (InterruptedException e1) { e1.printStackTrace(); }
  
        			for (int i = 0; i < posMarkers.size(); i++)
            		{
        				posMarkers.get(i).remove();
            			
            		}
    			}
    		}
    	}
	}
	
	public void updatePosMarkers()
	{
		CameraPosition cp;
		double oldCamLat, oldCamLon;
		double camLat, camLon;
		double range;
		
		cp = mf.mMap.getCameraPosition();
		camLat = cp.target.latitude;
		camLon = cp.target.longitude;
		range = 0.09;
		if (cp.zoom > 13) range = 0.03;
		else if (cp.zoom > 12.75) range = 0.035;
		else if(cp.zoom > 12.5) range = 0.04;
		else if(cp.zoom > 12.25) range = 0.045;
		else if(cp.zoom > 12) range = 0.05;
		else if(cp.zoom > 11.75) range = 0.06;
		else if(cp.zoom > 11.5) range = 0.07;
		else if(cp.zoom > 11.25) range = 0.08;
		else if (cp.zoom > 11) range = 0.09;
		
		oldCamLat = lastCamPos1.target.latitude;
		oldCamLon = lastCamPos1.target.longitude;
		if (mf.isWidescreen())
		{
			if (!(oldCamLat - camLat > range || oldCamLat - camLat < range * -1
					|| oldCamLon - camLon > range * 4 || oldCamLon - camLon < range * -4))
						return;
		}
		else
		{
			if (!(oldCamLat - camLat > range * 4|| oldCamLat - camLat < range * -4
					|| oldCamLon - camLon > range * 3 || oldCamLon - camLon < range * -3))
						return;
		}
		lastCamPos1 = cp;
		cameraMoved = true;
		
		if (subscriber != null && subscriber.isSubscribed()) 
		{
			subscriber.unsubscribe();
			
			try { t.join();	} catch (InterruptedException e1) {	}
			
			for (int i = 0; i < posMarkers.size(); i++)
    		{
    			posMarkers.get(i).remove();
    		}
		}
		showBusPos(false);
	}
}
