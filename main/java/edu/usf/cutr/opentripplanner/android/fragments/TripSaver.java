package edu.usf.cutr.opentripplanner.android.fragments;

import java.net.UnknownHostException;
import java.util.List;

import org.opentripplanner.v092snapshot.api.model.Itinerary;

import android.content.Context;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class TripSaver implements Runnable
{
	private Context context;
	List<Itinerary> itineraries;
	
	public TripSaver(Context c, List<Itinerary> it)
	{
		context = c; 
		itineraries = it;
	}
	
	public void run()
	{
		try { saveTrip(); } catch (UnknownHostException e) { e.printStackTrace(); }
	}
	
	/*public static void mongoQueryTest(DB db, List<Itinerary> positions, String ID)
	{
		DBCollection coll = db.getCollection("tripCollection");
		DBObject d = coll.findOne(new BasicDBObject("_id", ID + ":0"));
		System.out.println("********* MONGO OUTPUT *********");
		System.out.println("Route: " + d.get("routeId").toString());
		System.out.println("**********************************\n");
	}*/

	private Integer getNextSequence(String name, DBCollection counters) 
	{
		  counters.update(new BasicDBObject("_id", name), new BasicDBObject("$inc", new BasicDBObject("seq", 1)));
		  DBCursor ret = counters.find(new BasicDBObject("_id", name));
		  return (Integer) ret.next().get("seq");
	}


	public void saveTrip() throws UnknownHostException
	{
		String ID;
		DBCollection counters, trips;
		BasicDBObject doc;
		System.out.println("connect to mongo");
		MongoClient mongoClient = new MongoClient("192.168.43.105:27017");
		DB db = mongoClient.getDB( "mydb" );
		System.out.println("connected!");
		
		ID = new Installation().id(context);
		
		counters = db.getCollection("counterCollection");
		doc = new BasicDBObject();
		
		DBCursor cursor = counters.find(new BasicDBObject("_id", ID));
		//System.out.println(cursor.hasNext());
		
		if (!cursor.hasNext())
		{
			System.out.println("add seq");
			doc.append("_id", ID).append("seq", 0);
			counters.save(doc);
		}
		else 
		{
			System.out.println("find");
			cursor = counters.find();
			System.out.println("print");
			 while(cursor.hasNext()) {
			       System.out.println(cursor.next());
			   }
		}
		trips = db.getCollection("tripCollection");
		doc = new BasicDBObject();
		doc.append("_id", ID + ":" + getNextSequence(ID, counters));
		doc.append("from", itineraries.get(0).legs.get(0).getFrom().name).
		append("startTime", itineraries.get(0).legs.get(0).getStartTime());
		
		/*for (int i = 0; i < itineraries.get(0).legs.size(); i++)
		{
			doc.append("leg" + i,
					new BasicDBObject().append("routeId", itineraries.get(0).legs.get(i).routeId).
					append("tripId", itineraries.get(0).legs.get(i).tripId).
					append("agencyId", itineraries.get(0).legs.get(i).agencyId).
					append("agencyName", itineraries.get(0).legs.get(i).agencyName).
					append("headsign", itineraries.get(0).legs.get(i).getHeadsign()).
					append("routeShortName", itineraries.get(0).legs.get(i).getRouteShortName()).
					append("tripShortName", itineraries.get(0).legs.get(i).tripShortName).
					append("from", itineraries.get(0).legs.get(i).getFrom().name).
					append("to", itineraries.get(0).legs.get(i).getTo().name).
					append("mode", itineraries.get(0).legs.get(i).mode).
					append("startTime", itineraries.get(0).legs.get(i).getStartTime()).
					append("endTime", itineraries.get(0).legs.get(i).getEndTime())
			);
			}*/
		doc.append("to", itineraries.get(0).legs.get(itineraries.get(0).legs.size() - 1).getTo().name).
		append("endTime", itineraries.get(0).legs.get(0).getEndTime());
		trips.save(doc);
		
		
		//mongoQueryTest(db, itineraries, ID);
	}
}
