import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class GtfsRealtimeHttpAlertsUpdateSource
{
	private String url;
	
	public GtfsRealtimeHttpAlertsUpdateSource()
	    {
			System.out.println("get url");
	        String url = "http://localhost:8081/alerts";//"http://api.bart.gov/gtfsrt/alerts.aspx";//preferences.get("url", null);
	        if (url == null)
	            throw new IllegalArgumentException("Missing mandatory 'url' parameter");
	        this.url = url;
	        //this.agencyId = preferences.get("defaultAgencyId", null);
	    }
	
	 public List<AlertTest> getAlerts() 
	 {
		 	System.out.println("get alerts");
	        FeedMessage feed = null;
	        List<AlertTest> updates = null;
	        try {
	            InputStream is = HttpUtils.getData(url);
	            if (is != null) {
	                feed = GtfsRealtime.FeedMessage.parseFrom(is);//parser.parseFrom(is);
	                updates = getAlertsFromGtfsRealtime(feed);//, agencyId);
	            }
	        } catch (Exception e) {
	        	System.out.println("Failed to parse gtfs-rt feed from " + url + ":" + e);
	            //LOG.warn("Failed to parse gtfs-rt feed from " + url + ":", e);
	            //LOG.warn("Failed to parse gtfs-rt feed from " + url);
	        }
	        return updates;
	    }
	 
	 private ArrayList<AlertTest> getAlertsFromGtfsRealtime(GtfsRealtime.FeedMessage feed) 
	 {
	        if (feed == null) 
	        {
	            return null;
	        }
	        GtfsRealtime.FeedHeader header = feed.getHeader();
	        long timestamp = header.getTimestamp();
	        ArrayList<AlertTest> AlertList = new ArrayList<AlertTest>();
	    
	        System.out.println(feed.toString());
	        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) 
	        {
	            if (entity.hasAlert())//&& entity.getTripUpdate().hasTrip() 
	                    //&& (entity.getTripUpdate().getTrip().hasTripId() || entity.getTripUpdate().getTrip().hasRouteId())) {
	            {
	                AlertList.add(new AlertTest(entity.getAlert(), timestamp, entity.getId()));
	            }
	        }
	        return AlertList;
	    }
}