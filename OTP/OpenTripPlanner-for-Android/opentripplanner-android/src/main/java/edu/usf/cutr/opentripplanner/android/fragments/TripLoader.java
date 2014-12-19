package edu.usf.cutr.opentripplanner.android.fragments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Context;
import android.os.Message;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class TripLoader  implements Runnable
{
	MainFragment listener;
	Context context;
	
	public TripLoader(MainFragment mf, Context c)
	{
		listener = mf;
		context = c;
	}
	
	public void run()
	{
		try { loadTrips(); } catch (UnknownHostException e) { e.printStackTrace(); }
	}
	
	public void loadTrips() throws UnknownHostException
	{
		// The result is to be a JSON document.
		String result = "[";
		String ID;
		Message msg;
		DBCursor cursor; 
		DBCollection counters, trips;
		BasicDBObject doc;
		Properties prop = new Properties();
		String ip = "192.168.93.73"; // This is the default IP (this IP is not public though :( ).
		String port = "27017"; // Mongo's default port.
		
		// If there is another IP, it's read from a file.
		try {
			prop.load(Subscriber.class.getResourceAsStream("configMongo.properties"));
			if (prop.getProperty("server_ip") != null)
				ip = prop.getProperty("server_ip");
			if (prop.getProperty("port") != null)
				port = prop.getProperty("port");
		} catch (FileNotFoundException e1) {
			System.out.println("No config file!");
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("connect to mongo");
		MongoClient mongoClient = new MongoClient(ip + ":" + Integer.parseInt(port));
		DB db = mongoClient.getDB( "mydb" );
		System.out.println("connected!");
		trips = db.getCollection("tripCollection");
		
		ID = new Installation().id(context);
		Pattern j = Pattern.compile(ID, Pattern.CASE_INSENSITIVE);
		
		cursor = trips.find(new BasicDBObject("_id", j));
		while (cursor.hasNext())
		{
			result += cursor.next().toString();
			if (cursor.hasNext()) result += ",";
		}
		
		msg = new Message();
		msg.arg2 = 1;
		result += "]";
	    msg.obj = result;
	    listener.handler.sendMessage(msg);
	}
}
