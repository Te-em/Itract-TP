/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

//import opentripplanner.updater.PreferencesConfigurable;
//import opentripplanner.routing.graph.Graph;
//import opentripplanner.routing.trippattern.TripUpdateList;
//import opentripplanner.util.HttpUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class GtfsRealtimeHttpTripUpdateSource //implements opentripplanner.updater.stoptime.TripUpdateSource 
{
    
    //private static final Logger LOG = LoggerFactory.getLogger(GtfsRealtimeHttpTripUpdateSource.class);

    /**
     * Default agency id that is used for the trip id's in the TripUpdateLists
     */
    private String agencyId;

    private String url;

    //@Override
    public GtfsRealtimeHttpTripUpdateSource()
    {
        String url = "http://developer.mbta.com/lib/gtrtfs/Passages.pb";//preferences.get("url", null);
        if (url == null)
            throw new IllegalArgumentException("Missing mandatory 'url' parameter");
        this.url = url;
        //this.agencyId = preferences.get("defaultAgencyId", null);
    }

    //@Override
    public List<TripUpdate> getTripUpdates() {
        FeedMessage feed = null;
        List<TripUpdate> updates = null;
        try {
            InputStream is = HttpUtils.getData(url);
            if (is != null) {
                feed = GtfsRealtime.FeedMessage.parseFrom(is);//parser.parseFrom(is);
                updates = getTripUpdatesFromGtfsRealtime(feed);//, agencyId);
            }
        } catch (Exception e) {
            //LOG.warn("Failed to parse gtfs-rt feed from " + url + ":", e);
            //LOG.warn("Failed to parse gtfs-rt feed from " + url);
        }
        return updates;
    }
    
    private ArrayList<TripUpdate> getTripUpdatesFromGtfsRealtime(GtfsRealtime.FeedMessage feed) {
        if (feed == null) {
            return null;
        }
        GtfsRealtime.FeedHeader header = feed.getHeader();
        long timestamp = header.getTimestamp();
        ArrayList<TripUpdate> tripUpdateList = new ArrayList<TripUpdate>();
    
        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasTripUpdate() && entity.getTripUpdate().hasTrip() 
                    && (entity.getTripUpdate().getTrip().hasTripId() || entity.getTripUpdate().getTrip().hasRouteId())) {
                tripUpdateList.add(new TripUpdate(entity.getTripUpdate(), timestamp));
            }
        }
        return tripUpdateList;
    }

    public String toString() {
        return "GtfsRealtimeHttpUpdateStreamer(" + url + ")";
    }
}