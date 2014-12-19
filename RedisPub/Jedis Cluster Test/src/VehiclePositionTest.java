import com.google.transit.realtime.GtfsRealtime;


public class VehiclePositionTest {
//    private static final long serialVersionUID = 8305126586053907736L;
//    public static final String defaultLanguage = "en";
//    private Long serviceDate;

    private String stopId;
    public String getStopId() { return stopId; }
    public void setStopId(String param) { stopId = param; }
 
    private String tripId;
    public String getTripId() { return tripId; }
    public void setTripId(String param) { tripId = param; }

    private String routeId;
    public String getRouteId() { return routeId; }
    public void setRouteId(String param) { routeId = param; }

    private String vehicleId;
    public String getVehicleId() { return vehicleId; }

    private String agencyId;
    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String param) { agencyId = param; }
 
    private Long timeOfRecord;
    public Long getTimeOfRecord() { return timeOfRecord; }
    public void setTimeOfRecord(Long param) { timeOfRecord = param; }

    private Long timeOfLocationUpdate;
    public Long getTimeOfLocationUpdate() { return timeOfLocationUpdate; }
    public void setTimeOfLocationUpdate(Long param) { timeOfLocationUpdate = param; }

    private float speed;
    public float getSpeed() { return speed; }
    public void setSpeed(float param) { speed = param; }

    private double currentLocationLat;
    public double getCurrentLocationLat() { return currentLocationLat; }
    public void setCurrentLocationLat(double param) { currentLocationLat = param; }
 
    private double currentLocationLon;
    public double getCurrentLocationLon() { return currentLocationLon; }
    public void setCurrentLocationLon(double param) { currentLocationLon = param; }

    private Double odometer;
    public Double getOdometer() { return odometer; }
    public void setOdometer(double param) { odometer = param; }
    
    /**
     * In degrees, 0º is East, 90º is North, 180º is West, and 270º is South
     */

    private float currentOrientation;
    public float getCurrentOrientation() { return currentOrientation; }
    public void setCurrentOrientation(float param) { currentOrientation = param; }

    private String congestionLevel;
    public String getCongestionLevel() { return congestionLevel; }
    public void setCongestionLevel(String param) { congestionLevel = param; }

    private String status;
    public String getStatus() { return status; }
    public void setStatus(String param) { status = param; }

    private String label;
    public String getLabel() { return label; }
    public void setLabel(String param) { label = param; }
 
    private String licensePlate;
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String param) { licensePlate = param; }

    public VehiclePositionTest() { }

    public VehiclePositionTest(GtfsRealtime.VehiclePosition vehiclePosition, long timestamp) {
        GtfsRealtime.VehicleDescriptor vehicleDescriptor = vehiclePosition.getVehicle();
        stopId = vehiclePosition.getStopId();
        tripId = (vehiclePosition.getTrip().hasTripId()) ? vehiclePosition.getTrip().getTripId() : null;
        routeId = (vehiclePosition.getTrip().hasRouteId()) ? vehiclePosition.getTrip().getRouteId(): null;
        vehicleId = vehicleDescriptor.getId();
        timeOfRecord = vehiclePosition.hasTimestamp() ? vehiclePosition.getTimestamp() : timestamp;
        timeOfLocationUpdate = System.currentTimeMillis();
        speed = vehiclePosition.getPosition().getSpeed();
        currentLocationLon = vehiclePosition.getPosition().getLongitude();
        currentLocationLat = vehiclePosition.getPosition().getLatitude();
        odometer = vehiclePosition.getPosition().getOdometer();
        currentOrientation = vehiclePosition.getPosition().getBearing();
        congestionLevel = vehiclePosition.getCongestionLevel().toString();
        status = vehiclePosition.getCurrentStatus().toString();
        label = vehiclePosition.getVehicle().getLabel();
        licensePlate = vehiclePosition.getVehicle().getLicensePlate();
    }
}