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

package org.opentripplanner.v092snapshot.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/* import javax.xml.bind.annotation.XmlAttribute; */
/*import javax.xml.bind.annotation.XmlElement;*/
/*import javax.xml.bind.annotation.XmlElementWrapper;*/

import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.patch.Alerts;
import org.opentripplanner.v092snapshot.util.model.EncodedPolylineBean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One leg of a trip -- that is, a temporally continuous piece of the journey that takes place on a
 * particular vehicle (or on foot).
 */

@Data public class Leg implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -7657410568807464781L;

	/**
     * The date and time this leg begins.
     */
	private String startTime = null;
	
	public String getStartTime() { return startTime; }
    
    /**
     * The date and time this leg ends.
     */
    private String endTime = null;

	public String getEndTime() { return endTime; }
    
    public long duration;
    
    /**
     * The distance traveled while traversing the leg in meters.
     */
    private Double distance = null;
    
    public Double getDistance() { return distance; }

    /**
     * The mode (e.g., <code>Walk</code>) used when traversing this leg.
     */
    /*@XmlAttribute*/
    
    public String mode = TraverseMode.WALK.toString();

    /**
     * For transit legs, the route of the bus or train being used. For non-transit legs, the name of
     * the street being traversed.
     */
    /*@XmlAttribute*/
    
    public String route = "";

    /* @XmlAttribute */
    
    public String agencyName;

    /*@XmlAttribute*/
    
    public String agencyUrl;

    /*@XmlAttribute*/
    
    private int agencyTimeZoneOffset;
    
    public int getAgencyTimeZoneOffset() { return agencyTimeZoneOffset; }

    /**
     * For transit leg, the route's (background) color (if one exists). For non-transit legs, null.
     */
    /*@XmlAttribute*/
    
    public String routeColor = null;
    
    /**
     * For transit legs, the ID of the route.
     * For non-transit legs, null.
     */
    /*@XmlAttribute*/
    
    public String routeId = null;

    /**
     * For transit leg, the route's text color (if one exists). For non-transit legs, null.
     */
    /*@XmlAttribute*/
    
    public String routeTextColor = null;

    /**
     * For transit legs, if the rider should stay on the vehicle as it changes route names.
     */
    /*@XmlAttribute*/
    
    public Boolean interlineWithPreviousLeg;

    
    /**
     * For transit leg, the trip's short name (if one exists). For non-transit legs, null.
     */
    /*@XmlAttribute*/
    
    public String tripShortName = null;

    /**
     * For transit legs, the headsign of the bus or train being used. For non-transit legs, null.
     */
    /*@XmlAttribute*/
    
    private String headsign = null;
    
    public String getHeadsign() { return headsign; }

    /**
     * For transit legs, the ID of the transit agency that operates the service used for this leg.
     * For non-transit legs, null.
     */
    /*@XmlAttribute*/
    
    public String agencyId = null;
    
    /**
     * For transit legs, the ID of the trip.
     * For non-transit legs, null.
     */
    /*@XmlAttribute*/
    
    public String tripId = null;
    
    /**
     * The Place where the leg originates.
     */
    public Place from = null;
    
    public Place getFrom() { return from; }
    
    /**
     * The Place where the leg begins.
     */
    private Place to = null;
    
    public Place getTo() { return to; }

    /**
     * For transit legs, intermediate stops between the Place where the leg originates and the Place where the leg ends.
     * For non-transit legs, null.
     * This field is optional i.e. it is always null unless "showIntermediateStops" parameter is set to "true" in the planner request.
     */
    /*@XmlElementWrapper(name = "intermediateStops")*/
    @JsonProperty(value="intermediateStops")
    @Getter @Setter public List<Place> stop;

    /**
     * The leg's geometry.
     */
    public EncodedPolylineBean legGeometry;

    /**
     * A series of turn by turn instructions used for walking, biking and driving. 
     */
    /*@XmlElementWrapper(name = "steps")*/
    @Getter @Setter @JsonProperty(value="steps")
    public List<WalkStep> walkSteps;

    /**
     * Deprecated field formerly used for notes -- will be removed.  See
     * alerts
     */
    /*@XmlElement*/
    
    @Getter @Setter private List<Note> notes;

    /*@XmlElement*/
    
    @Getter @Setter private List<Alerts> alerts;

    /*@XmlAttribute*/
    
    private String routeShortName;
    
    public String getRouteShortName() { return routeShortName; }

    /*@XmlAttribute*/
    
    private String routeLongName;
    
    public String getRouteLongName() { return routeLongName; }

    /*@XmlAttribute*/
    
    public String boardRule;

    /*@XmlAttribute*/
    
    public String alightRule;

    /*@XmlAttribute*/
    
    public Boolean rentedBike;

    /**
     * bogus walk/bike/car legs are those that have 0.0 distance, 
     * and just one instruction
     * 
     * @return boolean true if the leg is bogus 
     */
    public boolean isBogusNonTransitLeg() {
       boolean retVal = false;
        if( (TraverseMode.WALK.toString().equals(this.mode) ||
             TraverseMode.CAR.toString().equals(this.mode) ||
             TraverseMode.BICYCLE.toString().equals(this.mode)) &&
            (this.walkSteps == null || this.walkSteps.size() <= 1) && 
            this.distance == 0) {
            retVal = true;
        }
        return retVal;
    }
    
    @Getter @Setter boolean bogusNonTransitLeg;
    
    /** 
     * The leg's duration in milliseconds
     */
    /*@XmlElement*/
//    public long getDuration() {
//        return endTime.getTimeInMillis() - startTime.getTimeInMillis();
//    }

//    public void addAlert(Alert alert) {
//        if (notes == null) {
//            notes = new ArrayList<Note>();
//        }
//        if (alerts == null) {
//            alerts = new ArrayList<Alert>();
//        }
//        String text = alert.alertHeaderText.getSomeTranslation();
//        if (text == null) {
//            text = alert.alertDescriptionText.getSomeTranslation();
//        }
//        if (text == null) {
//            text = alert.alertUrl.getSomeTranslation();
//        }
//        Note note = new Note(text);
//        if (!notes.contains(note)) {
//            notes.add(note);
//        }
//        if (!alerts.contains(alert)) {
//            alerts.add(alert);
//        }
//    }

//    public void setTimeZone(TimeZone timeZone) {
//        Calendar calendar = Calendar.getInstance(timeZone);
//        calendar.setTime(startTime.getTime());
//        startTime = calendar;
//        calendar = Calendar.getInstance(timeZone);
//        calendar.setTime(endTime.getTime());
//        endTime = calendar;
//        agencyTimeZoneOffset = timeZone.getOffset(startTime.getTimeInMillis());
//    }
}
