import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class Publisher 
{
	private static int expire = 60;
	
	public static void printPositions(List<VehiclePositionTest> positions)
	{	
		for (int i = 0; i < positions.size(); i++)
		{
			System.out.println("********* VEHICLE " + i + " *********");
				System.out.println("StopId: " + positions.get(i).getStopId());
				System.out.println("TripId: " + positions.get(i).getTripId());
				System.out.println("RouteId: " + positions.get(i).getRouteId());
				System.out.println("VehicleId: " + positions.get(i).getVehicleId());
				System.out.println("AgencyId: " + positions.get(i).getAgencyId());
				System.out.println("TimeOfRecord: " + positions.get(i).getTimeOfRecord());
				System.out.println("TimeOfLocationUpdate: " + positions.get(i).getTimeOfLocationUpdate());
				System.out.println("Speed: " + positions.get(i).getSpeed());
				System.out.println("CurrentLocationLat: " + positions.get(i).getCurrentLocationLat());
				System.out.println("CurrentLocationLon: " + positions.get(i).getCurrentLocationLon());
				System.out.println("Odometer: " + positions.get(i).getOdometer());
				System.out.println("CurrentOrientation: " + positions.get(i).getCurrentOrientation());
				System.out.println("CongestionLevel: " + positions.get(i).getCongestionLevel());
				System.out.println("Status: " + positions.get(i).getStatus());
				System.out.println("Label: " + positions.get(i).getLabel());
				System.out.println("LicensePlate: " + positions.get(i).getLicensePlate());
			System.out.println("**********************************\n");
		}
	}
	
	public static void correctNullValues(List<VehiclePositionTest> positions)
	{
		if (positions == null) return;
		for (int i = 0; i < positions.size(); i++)
		{
				if (positions.get(i).getStopId() == null) positions.get(i).setStopId("");
				if (positions.get(i).getTripId() == null) positions.get(i).setTripId("");
				if (positions.get(i).getRouteId() == null) positions.get(i).setRouteId("");
				if (positions.get(i).getAgencyId() == null) positions.get(i).setAgencyId("");
				if (positions.get(i).getTimeOfRecord() == null) positions.get(i).setTimeOfRecord(0L);
				if (positions.get(i).getTimeOfLocationUpdate() == null) positions.get(i).setTimeOfLocationUpdate(0L);
				if (positions.get(i).getCongestionLevel() == null) positions.get(i).setCongestionLevel("");
				if (positions.get(i).getStatus() == null) positions.get(i).setStatus("");
				if (positions.get(i).getLabel() == null) positions.get(i).setLabel("");
				if (positions.get(i).getLicensePlate() == null) positions.get(i).setLicensePlate("");
		}
	}
	
	public static void mongoInsertTest3(DB db, List<AlertTest> alerts)
	{
		DBCollection coll = db.getCollection("testCollection");
		BasicDBObject doc = new BasicDBObject();
		for (int i = 0; i < alerts.size(); i++)
		{
			doc.append("_id", alerts.get(i).ID + ": " + new Date().getTime()).
			append("Cause", alerts.get(i).cause).
			append("Effect", alerts.get(i).effect).
			append("Text", alerts.get(i).text);
			for (int j = 0; j < alerts.get(i).agencyID.size(); i++)
				doc.append("agencyID" + j, alerts.get(i).agencyID);
			for (int j = 0; j < alerts.get(i).routeID.size(); i++)
				doc.append("routeID" + j, alerts.get(i).routeID);
			for (int j = 0; j < alerts.get(i).stopID.size(); i++)
				doc.append("stopID" + j, alerts.get(i).stopID);
			coll.save(doc);
		}	
	}
	
	public static void mongoInsertTest2(DB db, List<TripUpdate> tripUpdates)
	{
		DBCollection coll = db.getCollection("testCollection");
		BasicDBObject doc = new BasicDBObject();
		// Now, copy and update current agency data to mongo.
		for (int i = 0; i < tripUpdates.size(); i++)
		{
			doc.append("_id", tripUpdates.get(i).tripId + ": " + new Date().getTime());
			coll.save(doc);
			for (int j = 0; j < tripUpdates.get(i).stopUpdates.size(); j++)
			{
				doc = new BasicDBObject();
				doc.append("_id", tripUpdates.get(i).tripId + "-" + j + ": " + new Date().getTime()).
		    	append("arrivalDelay", tripUpdates.get(i).stopUpdates.get(j).arrivalDelay).
		    	append("arrivalTime", tripUpdates.get(i).stopUpdates.get(j).arrivalTime).
		    	append("departureDelay", tripUpdates.get(i).stopUpdates.get(j).departureDelay).
		    	append("departureTime", tripUpdates.get(i).stopUpdates.get(j).departureTime).
		    	append("stopSeq", tripUpdates.get(i).stopUpdates.get(j).stopSeq).
		    	append("trip", tripUpdates.get(i).tripId);
				coll.save(doc);
			}
		}	
	}

	public static void mongoInsertTest(DB db, List<VehiclePositionTest> positions)
	{
		DBCollection coll = db.getCollection("testCollection");
		BasicDBObject doc = new BasicDBObject();
		// Now, copy and update current agency data to mongo.
		for (int i = 0; i < positions.size(); i++)
		{
			doc.append("_id", positions.get(i).getVehicleId() + ": " + new Date().getTime()).
	    	append("stopId", positions.get(i).getStopId()).
	    	append("tripId", positions.get(i).getTripId()).
	    	append("agencyId", positions.get(i).getAgencyId()).
	    	append("timeOfRecord", positions.get(i).getTimeOfRecord()).
			append("timeOfLocationUpdate", positions.get(i).getTimeOfLocationUpdate()).
	    	append("speed", positions.get(i).getSpeed()).
			append("currentLocationLat", positions.get(i).getCurrentLocationLat()).
	    	append("currentLocationLon", positions.get(i).getCurrentLocationLon()).
	    	append("odometer", positions.get(i).getOdometer()).
	    	append("currentOrientation", positions.get(i).getCurrentOrientation()).
	    	append("congestionLevel", positions.get(i).getCongestionLevel()).
	    	append("status", positions.get(i).getStatus()).
	    	append("label", positions.get(i).getLabel()).
	    	append("licensePlate", positions.get(i).getLicensePlate());
	    	coll.save(doc);
		}
	}
	
	public static void mongoQueryTest2(DB db, List<TripUpdate> tripUpdates)
	{
		DBCollection coll = db.getCollection("testCollection");
		DBObject d = coll.findOne(new BasicDBObject("_id", tripUpdates.get(0).tripId));
		DBObject a = coll.findOne(new BasicDBObject("_id", tripUpdates.get(0).tripId + "-0"));
		System.out.println("********* MONGO OUTPUT *********");
		System.out.println("Trip: " + d.get("_id").toString());
		System.out.println("Stop delay: " + a.get("arrivalDelay").toString());
		System.out.println("**********************************\n");
	}
	
	public static void mongoQueryTest(DB db, List<VehiclePositionTest> positions)
	{
		DBCollection coll = db.getCollection("testCollection");
		DBObject d = coll.findOne(new BasicDBObject("_id", positions.get(0).getVehicleId()));
		System.out.println("********* MONGO OUTPUT *********");
		System.out.println("Vehicle: " + d.get("_id").toString());
		System.out.println("Stop: " + d.get("stopId").toString());
		System.out.println("Trip: " + d.get("tripId").toString());
		System.out.println("Agency: " + d.get("agencyId").toString());
		System.out.println("Time of record: " + d.get("timeOfRecord").toString());
		System.out.println("Time of update: " + d.get("timeOfLocationUpdate").toString());
		System.out.println("Speed: " + d.get("speed").toString());
		System.out.println("Latitude: " + d.get("currentLocationLat").toString());
		System.out.println("Longitude: " + d.get("currentLocationLon").toString());
		System.out.println("Odometer: " + d.get("odometer").toString());
		System.out.println("Orientation: " + d.get("currentOrientation").toString());
		System.out.println("Congestion level: " + d.get("congestionLevel").toString());
		System.out.println("Status: " + d.get("status").toString());
		System.out.println("Label: " + d.get("label").toString());
		System.out.println("License plate: " + d.get("licensePlate").toString());
		System.out.println("**********************************\n");
	}
	
	public static void redisInsertTest3(JedisCluster jc, List<AlertTest> alerts)
	{
		for (int i = 0; i < alerts.size(); i++)
		{
			for (int j = 0; j < alerts.get(i).getRouteID().size(); j++)
			{
				String ID = alerts.get(i).getRouteID().get(j);
				jc.rpush(ID, "" + alerts.get(i).cause);
				jc.rpush(ID, "" + alerts.get(i).effect);
				jc.expire(ID, 60);
			}
		}
	}
	
	public static void redisInsertTest2(JedisCluster jc, List<TripUpdate> tripUpdates)
	{
		for (int i = 0; i < tripUpdates.size(); i++)
		{
			for (int j = 0; j < tripUpdates.get(i).stopUpdates.size(); j++)
			{
				String ID = tripUpdates.get(i).tripId + "-" + j;
				jc.rpush(ID, "" + tripUpdates.get(i).tripId);
				jc.rpush(ID, "" + tripUpdates.get(i).stopUpdates.get(j).arrivalDelay);
				jc.rpush(ID, "" + tripUpdates.get(i).stopUpdates.get(j).arrivalTime);
				jc.rpush(ID, "" + tripUpdates.get(i).stopUpdates.get(j).departureDelay);
				jc.rpush(ID, "" + tripUpdates.get(i).stopUpdates.get(j).departureTime);
				jc.rpush(ID, "" + tripUpdates.get(i).stopUpdates.get(j).stopSeq);
				jc.expire(ID, 60);
			}
		}
	}
	
	public static void redisInsertTest(JedisCluster jc, List<VehiclePositionTest> positions)
	{
		for (int i = 0; i < positions.size(); i++)
		{
			Map<String, String> hash = new HashMap<String, String>();
			hash.put("vehicleId", positions.get(i).getVehicleId());
			hash.put("stopId", positions.get(i).getStopId());
			hash.put("tripId", positions.get(i).getTripId());
			hash.put("agencyId", positions.get(i).getAgencyId());
			hash.put("timeOfRecord", "" + positions.get(i).getTimeOfRecord());
			hash.put("timeOfLocationUpdate", "" + positions.get(i).getTimeOfLocationUpdate());
			hash.put("speed", "" + positions.get(i).getSpeed());
			hash.put("currentLocationLat", "" + positions.get(i).getCurrentLocationLat());
			hash.put("currentLocationLon", "" + positions.get(i).getCurrentLocationLon());
			hash.put("odometer", "" + positions.get(i).getOdometer());
			hash.put("currentOrientation", "" + positions.get(i).getCurrentOrientation());
			hash.put("congestionLevel", positions.get(i).getCongestionLevel());
			hash.put("status", positions.get(i).getStatus());
			hash.put("label", positions.get(i).getLabel());
			hash.put("licensePlate", positions.get(i).getLicensePlate());
			jc.hmset(positions.get(i).getVehicleId(), hash);
			jc.expire(positions.get(i).getVehicleId(), expire);
		}
	}
	
	public static void redisQueryTest2(JedisCluster jc, List<TripUpdate> tripUpdates)
	{
		System.out.println("********* REDIS OUTPUT *********");
		for (int i = 0; i < tripUpdates.size(); i++)
		{
			for (int j = 0; j < tripUpdates.get(i).stopUpdates.size(); j++)
			{
				String ID = tripUpdates.get(i).tripId + "-" + j;
				System.out.println(ID);
				System.out.println("trip ID: " + jc.lindex(ID, 0));
				System.out.println("delay: " + jc.lindex(ID, 1));
				System.out.println("time: " + jc.lindex(ID, 2));
				System.out.println("stopseq: " + jc.lindex(ID, 3));
			}
		}
		System.out.println("**********************************\n");
	}
	
	public static void redisQueryTest(JedisCluster jc, List<VehiclePositionTest> positions)
	{
		System.out.println("********* REDIS OUTPUT *********");
		for (int i = 0; i < positions.size(); i++)
		{
			System.out.println(i + "Lat: " + jc.hget(positions.get(i).getVehicleId(), "currentLocationLat"));
			System.out.println(i + "Lon: " + jc.hget(positions.get(i).getVehicleId(), "currentLocationLon"));
		}
		System.out.println("**********************************\n");
	}
	
	private static JedisPool getRandomNode(List<JedisPool> pools) 
	{
	    return pools.get(new Random().nextInt(pools.size()));
	}
	
	
	
	public static void pubTest3(List<JedisPool> pools, List<AlertTest> alerts) throws InterruptedException
	{
		JedisPool jp = getRandomNode(pools);
		Jedis jedis = null;
		
		try 
		{
		    jedis = jp.getResource();

		    for (int i = 0; i < alerts.size(); i++)
			{
		    	for (int j = 0; j < alerts.get(i).getRouteID().size(); j++)
				{
		    		if (alerts.get(i).getRouteID().get(j) == null) continue;
					String ID = alerts.get(i).getRouteID().get(j);
			    	System.out.println("I will publish message on channel " + ID + 
			    			", " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
			    	jedis.publish(ID,
				    	"\"" + ID + "\" " +
		    			"{ " +
		    				"\"ID\": \"" + alerts.get(i).ID + "\", " +
		    				"\"cause\": \"" + alerts.get(i).cause + "\", " +
		    				"\"effect\": \"" + alerts.get(i).effect + "\", " +
		    				"\"text\": \"" + alerts.get(i).text + "\", " +
		    				"\"timestamp\": " + new Date().getTime() + " " +
		    			"}");
			    	/*jedis.publish(ID + ".pubStatus", "New");
			    	jedis.publish(ID + ".ID" , "" + alerts.get(i).ID);
		    		jedis.publish(ID + ".cause" , "" + alerts.get(i).cause);
		    		jedis.publish(ID + ".effect" , "" + alerts.get(i).effect);
		    		jedis.publish(ID + ".text" , "" + alerts.get(i).text);*/
				}
		    	for (int j = 0; j < alerts.get(i).getStopID().size(); j++)
				{
		    		if (alerts.get(i).getStopID().get(j) == null) continue;
					String ID = alerts.get(i).getStopID().get(j);
			    	System.out.println("I will publish message on channel " + ID + 
			    			", " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
			    	jedis.publish(ID,
			    			"{ " +
			    				"\"ID\": \"" + alerts.get(i).ID + "\", " +
			    				"\"cause\": \"" + alerts.get(i).cause + "\", " +
			    				"\"effect\": \"" + alerts.get(i).effect + "\", " +
			    				"\"text\": \"" + alerts.get(i).text + "\", " +
			    				"\"timestamp\": " + new Date().getTime() + " " +
			    			"}");
			    	/*jedis.publish(ID + ".pubStatus", "New");
			    	jedis.publish(ID + ".ID" , "" + alerts.get(i).ID);
		    		jedis.publish(ID + ".cause" , "" + alerts.get(i).cause);
		    		jedis.publish(ID + ".effect" , "" + alerts.get(i).effect);
		    		jedis.publish(ID + ".text" , "" + alerts.get(i).text);*/
				}
		    	for (int j = 0; j < alerts.get(i).getAgencyID().size(); j++)
				{
		    		if (alerts.get(i).getAgencyID().get(j) == null) continue;
					String ID = alerts.get(i).getStopID().get(j);
			    	System.out.println("I will publish message on channel " + ID + 
			    			", " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
			    	jedis.publish(ID,
			    			"{ " +
			    				"\"ID\": \"" + alerts.get(i).ID + "\", " +
			    				"\"cause\": \"" + alerts.get(i).cause + "\", " +
			    				"\"effect\": \"" + alerts.get(i).effect + "\", " +
			    				"\"text\": \"" + alerts.get(i).text + "\", " +
			    				"\"timestamp\": " + new Date().getTime() + " " +
			    			"}");
			    	/*jedis.publish(ID + ".pubStatus", "New");
			    	jedis.publish(ID + ".ID" , "" + alerts.get(i).ID);
		    		jedis.publish(ID + ".cause" , "" + alerts.get(i).cause);
		    		jedis.publish(ID + ".effect" , "" + alerts.get(i).effect);
		    		jedis.publish(ID + ".text" , "" + alerts.get(i).text);*/
				}
			}
		    //Thread.sleep(1000);
		    jedis.publish("broadcastAlert", "Complete");
		    //jedis.publish("broadcastAlert" + ".time", ""+System.nanoTime());
		 
		} catch (JedisConnectionException e) 
		{ 
			System.out.println("JedisConnectionException occurred in PubTest");
		    if (jedis != null) 
		    {
				jp.returnBrokenResource(jedis);
				jedis = null;
		    }
		    
		    // it seems that this node is broken, assign new node
		    jp = getRandomNode(pools);
		} 
		finally 
		{
		    if (jedis != null) { jp.returnResource(jedis); }
		}		   
	}
	
	
	
	public static void pubTest2(List<JedisPool> pools, List<TripUpdate> tripUpdates) throws InterruptedException
	{
		JedisPool jp = getRandomNode(pools);
		Jedis jedis = null;
		
		try 
		{
		    jedis = jp.getResource();
		    //System.out.println("I will publish message on channel " + positions.get(0).getVehicleId() + ", " + jedis.getClient().getHost() + ":" + 
			   // jedis.getClient().getPort());

		    for (int i = 0; i < tripUpdates.size(); i++)
			{
		    	for (int j = 0; j < tripUpdates.get(i).stopUpdates.size(); j++)
				{
		    		String ID = tripUpdates.get(i).tripId + "." + j;
		    		System.out.println("I will publish message on channel " + ID + 
		    		", " + jedis.getClient().getHost() + ":" + jedis.getClient().getPort());
		    		jedis.publish(ID,
			    			"{ " +
			    				"\"arr_delay\": " + tripUpdates.get(i).stopUpdates.get(j).arrivalDelay + ", " +
			    				"\"arr_time\": " + tripUpdates.get(i).stopUpdates.get(j).arrivalTime + ", " +
			    				"\"dep_delay\": " + tripUpdates.get(i).stopUpdates.get(j).departureDelay + ", " +
			    				"\"dep_time\": " + tripUpdates.get(i).stopUpdates.get(j).departureTime + ", " +
			    				"\"stopseq\": " + tripUpdates.get(i).stopUpdates.get(j).stopSeq + ", " +
			    				"\"timestamp\": " + new Date().getTime() + " " +
			    			"}");
		    		/*jedis.publish(ID + ".arr_delay" , "" + tripUpdates.get(i).stopUpdates.get(j).arrivalDelay);
		    		jedis.publish(ID + ".arr_time" , "" + tripUpdates.get(i).stopUpdates.get(j).arrivalTime);
		    		jedis.publish(ID + ".dep_delay" , "" + tripUpdates.get(i).stopUpdates.get(j).departureDelay);
		    		jedis.publish(ID + ".dep_time" , "" + tripUpdates.get(i).stopUpdates.get(j).departureTime);
		    		jedis.publish(ID + ".stopseq" , "" + tripUpdates.get(i).stopUpdates.get(j).stopSeq);*/
				}
		    	//jedis.publish(tripUpdates.get(i).tripId + ".pubStatus", "New");
			}
		    jedis.publish("broadcastTrip", "Complete");
		    //jedis.publish("broadcastTrip" + ".time", ""+System.nanoTime());
		 
		} catch (JedisConnectionException e) 
		{ 
			System.out.println("JedisConnectionException occurred in PubTest");
		    if (jedis != null) 
		    {
				jp.returnBrokenResource(jedis);
				jedis = null;
		    }
		    
		    // it seems that this node is broken, assign new node
		    jp = getRandomNode(pools);
		} 
		finally 
		{
		    if (jedis != null) { jp.returnResource(jedis); }
		}		   
	}
	
	
	public static void pubTest(List<JedisPool> pools, List<VehiclePositionTest> positions) throws InterruptedException
	{
		JedisPool jp = getRandomNode(pools);
		Jedis jedis = null;
		
		try 
		{
		    jedis = jp.getResource();
		    //System.out.println("I will publish message on channel " + positions.get(0).getVehicleId() + ", " + jedis.getClient().getHost() + ":" + 
			   // jedis.getClient().getPort());
		    //long time = new Date().getTime();
		    for (int i = 0; i < positions.size(); i++)
			{
		    	System.out.println("I will publish message on channel " + positions.get(i).getVehicleId() + ", " + jedis.getClient().getHost() + ":" + 
					    jedis.getClient().getPort());
		    	jedis.publish(positions.get(i).getVehicleId(),
		    			"{ " +
		    				"\"lat\": " + positions.get(i).getCurrentLocationLat() + ", " +
		    				"\"lon\": " + positions.get(i).getCurrentLocationLon() + ", " +
		    				"\"agencyId\": \"" + positions.get(i).getAgencyId() + "\", " +
		    				"\"routeId\": \"" + positions.get(i).getRouteId() + "\", " +
		    				"\"vehicleId\": \"" + positions.get(i).getVehicleId() + "\", " +
		    				"\"timestamp\": " + new Date().getTime() + " " +
		    			"}");
		    	/*jedis.publish(positions.get(i).getVehicleId() + ".pubStatus", "New"+ ";" + new Date().getTime());
			    jedis.publish(positions.get(i).getVehicleId() + ".lat" , "" + positions.get(i).getCurrentLocationLat()+ ";" + new Date().getTime());
			    jedis.publish(positions.get(i).getVehicleId() + ".lon" , "" + positions.get(i).getCurrentLocationLon()+ ";" + new Date().getTime());
			    jedis.publish(positions.get(i).getVehicleId() + ".agencyId" , "" + positions.get(i).getAgencyId()+ ";" + new Date().getTime());
			    jedis.publish(positions.get(i).getVehicleId() + ".routeId" , "" + positions.get(i).getRouteId()+ ";" + new Date().getTime());
			    jedis.publish(positions.get(i).getVehicleId() + ".vehicleId" , "" + positions.get(i).getVehicleId()+ ";" + new Date().getTime());*/
			}
		    jedis.publish("broadcastPos", "Complete");
		    //jedis.publish("broadcastPos" + ".time", ""+ time);
		 
		} catch (JedisConnectionException e) 
		{ 
			System.out.println("JedisConnectionException occurred in PubTest");
		    if (jedis != null) 
		    {
				jp.returnBrokenResource(jedis);
				jedis = null;
		    }
		    
		    // it seems that this node is broken, assign new node
		    jp = getRandomNode(pools);
		} 
		finally 
		{
		    if (jedis != null) { jp.returnResource(jedis); }
		}		   
	}
	
	public static void main(String[] args) throws UnknownHostException, InterruptedException
	{
		System.out.println("testing publisher");
		
		//System.out.println(tripUpdates.get(0).tripId + " - " + 
		//		tripUpdates.get(0).stopUpdates.get(0).stopSeq + ": " + 
			//	tripUpdates.get(0).stopUpdates.get(0).arrivalDelay);
		
		GtfsRealtimeHttpVehicleUpdateSource gtfs = new GtfsRealtimeHttpVehicleUpdateSource();
		GtfsRealtimeHttpTripUpdateSource tripgtfs = new GtfsRealtimeHttpTripUpdateSource();
		//GtfsRealtimeHttpAlertsUpdateSource alertgtfs = new GtfsRealtimeHttpAlertsUpdateSource();
		
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		jedisClusterNodes.add(new HostAndPort("193.10.227.200", 7005));
		JedisCluster jc = new JedisCluster(jedisClusterNodes);
		System.out.println("Currently " + jc.getClusterNodes().size() + " nodes in cluster");
		Map<String, JedisPool> nodeMap = jc.getClusterNodes();
		List<JedisPool> nodePoolList = new ArrayList<JedisPool>(nodeMap.values());
		Collections.shuffle(nodePoolList);
		
		System.out.println("connect to mongo");;
		MongoClient mongoClient = new MongoClient( "192.168.93.73" );
		DB db = mongoClient.getDB( "mydb" );
		System.out.println("connected!");
		DBCollection coll = db.getCollection("testCollection");
		
/*String ID = "test";
		BasicDBObject doc = new BasicDBObject();
		doc.append("_id", ID).append("seq", "0");
		System.out.println("find");
		DBCursor cursor = coll.find();
		System.out.println("print");
		 while(cursor.hasNext()) {
		       System.out.println(cursor.next());
		   }*/
		while (true)
		{
			List<TripUpdate> tripUpdates;
			List<AlertTest> alerts = null;
			TripUpdateTest tpt = new TripUpdateTest();
			if (args.length > 0 && args[0].equals("testArrivalDelay"))
				tripUpdates = tpt.testArrivalDelay("testTrip");
			else if (args.length > 0 && args[0].equals("testDepartureDelay"))
				tripUpdates = tpt.testDepartureDelay("testTrip");
			else if (args.length > 0 && args[0].equals("tesTripUpdate"))
				tripUpdates = tpt.testTripUpdate("testTrip");
			else if (args.length > 0 && args[0].equals("testDepArrDelay"))
				tripUpdates = tpt.testDepArrDelay("testTrip", "testTrip2");
			else if (args.length > 0 && args[0].equals("testRandomDelay"))
				tripUpdates = tpt.testRandomDelay("testTrip", "testTrip2");
			else if (args.length > 0 && args[0].equals("testFileDelay"))
				tripUpdates = tpt.testFileDelay("testTrip");
			else tripUpdates = tripgtfs.getTripUpdates();
			
			if (args.length > 0 && (args[0].equals("testAlert") || args.length > 1 && args[1].equals("testAlert")))
				alerts = tpt.testAlert("testRoute");
			
			//List<Alert> alerts = alertgtfs.getAlerts();
			List<VehiclePositionTest> positions = gtfs.getVehiclePositionUpdates();
			
			
			correctNullValues(positions);
			//printPositions(positions);
			
			if (args.length > 0 && args[0].equals("mongo"))
			{
				mongoInsertTest(db, positions);
				mongoQueryTest(db, positions);
				mongoInsertTest2(db, tripUpdates);
				mongoQueryTest2(db, tripUpdates);
			}

			//redisInsertTest(jc, positions);
			//redisQueryTest(jc, positions);
			//redisInsertTest2(jc, tripUpdates);
			//redisInsertTest3(jc, alerts);
			//redisQueryTest2(jc, tripUpdates);
			pubTest(nodePoolList, positions);
			pubTest2(nodePoolList, tripUpdates);
			if (alerts != null) pubTest3(nodePoolList, alerts);
			Thread.sleep(10000);
		}
	}
}
