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
import java.util.List;
import java.util.prefs.Preferences;
import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.util.ArrayList;

//public class GtfsRealtimeHttpVehicleUpdateSource implements TripUpdateSource, PreferencesConfigurable {
public class GtfsRealtimeHttpVehicleUpdateSource {
    //private static final Logger LOG = LoggerFactory.getLogger(GtfsRealtimeHttpVehicleUpdateSource.class);
    
    private String agencyId;
    private String url;

    public GtfsRealtimeHttpVehicleUpdateSource() {
        String url = "http://itract.cs.kau.se:13600/vehicle-positions";//preferences.get("url", null);
        if (url == null) {
            throw new IllegalArgumentException("Missing mandatory 'url' parameter");
        }
        this.url = url;
        this.agencyId = "601";//preferences.get("defaultAgencyId", null);
    }

    public List<VehiclePositionTest> getVehiclePositionUpdates() {
        FeedMessage feed = null;
        List<VehiclePositionTest> vehiclePositions = null;
        try {
            InputStream is = HttpUtils.getData(url);
            if (is != null) {
                feed = GtfsRealtime.FeedMessage.parseFrom(is);//.PARSER.parseFrom(is);
                vehiclePositions = getVehiclePositionsFromGtfsRealtime(feed);
                System.out.println("Got vehicle positions.");
            }
        }
        catch (IOException e) {
            System.out.println("Failed to parse gtfs-rt feed from " + url + ":");
        }
        return vehiclePositions;
    }

    private ArrayList<VehiclePositionTest> getVehiclePositionsFromGtfsRealtime(GtfsRealtime.FeedMessage feed) {
        if (feed == null) {
            return null;
        }
        GtfsRealtime.FeedHeader header = feed.getHeader();
        long timestamp = header.getTimestamp();
        ArrayList<VehiclePositionTest> vehicleList = new ArrayList<VehiclePositionTest>();
    
        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasVehicle() && entity.getVehicle().hasPosition() 
                    && entity.getVehicle().hasTrip() && (entity.getVehicle().getTrip().hasTripId() || entity.getVehicle().getTrip().hasRouteId())) {
                vehicleList.add(new VehiclePositionTest(entity.getVehicle(), timestamp));
            }
        }
        return vehicleList;
    }

    public String toString() {
        return "GtfsRealtimeHttpVehicleUpdateStreamer(" + url + ")";
    }
}