import java.util.ArrayList;
import java.util.List;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;


public class AlertTest 
{
	
	public String cause, effect, text, ID;
	public String time;
	ArrayList<String> routeID, stopID, agencyID; 
	
	public ArrayList<String> getRouteID() { return routeID; }
	public ArrayList<String> getAgencyID() { return agencyID; }
	public ArrayList<String> getStopID() { return stopID; }
	
	public AlertTest(GtfsRealtime.Alert alert, long timestamp, String ID)
	{
		List<EntitySelector> entities = alert.getInformedEntityList();
		this.ID = ID;
		routeID = new ArrayList<String>();
		stopID = new ArrayList<String>();
		agencyID = new ArrayList<String>();
		//System.out.println(alert.getEffect().name());
		//System.out.println(alert.getDescriptionText().getTranslation(0));
		cause = alert.getCause().name();
		effect = alert.getEffect().name();
		text = alert.getDescriptionText().getTranslation(0).toString();
		time = "" + timestamp;
		for (int i = 0; i < entities.size(); i++)
		{
			if (entities.get(i).hasAgencyId()) agencyID.add(entities.get(i).getAgencyId());
			if (entities.get(i).hasRouteId()) routeID.add(entities.get(i).getRouteId());
			if (entities.get(i).hasStopId()) stopID.add(entities.get(i).getStopId());
			System.out.println("stop ID is " + entities.get(i).getStopId());
		}
	}
	
	public AlertTest() {}
}
