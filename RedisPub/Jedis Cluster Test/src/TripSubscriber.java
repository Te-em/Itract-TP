import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

//import JedisPubSubTest.TestPubSub;




import org.json.JSONException;
import org.json.JSONObject;

//import SubTestDriver;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;


public class TripSubscriber extends JedisPubSub implements Runnable
{
	private Subscriber sub;
	private  String[] channels;
	private  String[] tripID;
	
	private  SubTestDriver listener;
	private List<SubTestDriver.Itinerary> itineraries;
	private SubTestDriver.Leg lastLeg;

	public  void subTest (List<JedisPool> pools) 
	{
		 JedisPool jp = sub.getRandomNode(pools);
	    
	    while (true)
		    {
			Jedis jedis = null;
			
				try 
				{
				    jedis = jp.getResource();
				    System.out.println("TripSub will subscribe on " + jedis.getClient().getHost() + ":" + 
					    jedis.getClient().getPort());
				    System.out.println("On no. of channels:" + channels.length);
				    for (int i = 0; i < channels.length; i++)
				    	System.out.println(channels[i]);
	
				    //jedis.subscribe(new Sub(), "hello");
				    	jedis.psubscribe(new TripSubscriber(itineraries, tripID, listener), channels);
				    	System.out.println("break");
				    break;
				} catch (JedisConnectionException e) 
				{
					System.out.println("JedisConnectionException occurred in SubTest");
			    
					if (jedis != null) 
					{
						jp.returnBrokenResource(jedis);
						jedis = null;
				    }
			    
			    // it seems that this node is broken, assign new node
					jp = sub.getRandomNode(pools);
				} finally { if (jedis != null) { jp.returnResource(jedis); }
			}
		}
	}
	
	public  void getKeysTest(JedisCluster jc) throws InterruptedException
	{
		for (int i = 0; i < tripID.length; i++)
		{
			//System.out.println("get from ch " + vehicleID[i]);
			//System.out.println("example " + jc.hget(vehicleID[i], "currentLocationLat"));
			int j = 0;
			System.out.println(tripID[i] + "-" + j);
			while (jc.lindex(tripID[i] + "-" + j, 0) != null)
			{
				System.out.println("trip ID: " + jc.lindex(tripID[i] + "-" + j, 0));
				j++;
			}
			/*is.callback(channels[i] + ".pubStatus", "New");
			is.callback(channels[i] + ".lat", jc.hget(tripID[i], "currentLocationLat"));
			is.callback(channels[i] + ".lon", jc.hget(tripID[i], "currentLocationLon"));
			is.callback(channels[i] + ".agencyId", jc.hget(tripID[i], "agencyId"));
			is.callback(channels[i] + ".routeId", jc.hget(tripID[i], "routeId"));
			is.callback(channels[i] + ".vehicleId", jc.hget(tripID[i], "vehicleId"));*/
		}
		//System.out.println("waiting set to false in sub");
		//is.waiting = false;
		//Thread.sleep(10);
	}
	
	public TripSubscriber()
	{}
	public TripSubscriber(String[] trips, SubTestDriver mf)
	{
		tripID = trips;
		listener = mf;
	}
	public TripSubscriber(List<SubTestDriver.Itinerary> itineraries, String[] trips, SubTestDriver mf)
	{
		tripID = trips;
		listener = mf;
		this.itineraries = itineraries;
	}
	
	public void run()
	{
		try {
			runSub();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void runSub() throws InterruptedException, UnknownHostException 
	{
		sub = new Subscriber();
		channels = new String[tripID.length + 1];
		
		for (int i = 0; i < tripID.length; i++)
			channels[i] = tripID[i] + ".*";
		channels[channels.length-1] = "broadcastTrip.*";
		
		if (!sub.testConnection()) return;
		
		List<JedisPool> nodePoolList = sub.redisConnect();
		
		System.out.println("connected");
		
		//getKeysTest(jc);
		subTest(nodePoolList);

		System.out.println("Done...");
	 }
	
/*public void notify(String ch, String msg)
{
	System.out.println(ch + ": " + msg);
	if (itineraries == null) return;
	// Get the trip ID.
	String trip[] = ch.split("\\.");
	Message subMsg;
	// Find the leg for the current trip.
	//itineraries.get(0).legs.get(0).tripId = "testTrip";
	//itineraries.get(0).legs.get(1).tripId = "testTrip2";
	for (int i = 0; i < itineraries.get(0).legs.size(); i++)
	{
		System.out.println(itineraries.get(0).legs.get(i).tripId + " =? " + trip[0]);
		if (itineraries.get(0).legs.get(i).tripId.equals(trip[0]))
		{
			System.out.println("found trip");
			// If arrival time, set new arrival time.
			if (ch.endsWith(".arr_time") && !msg.equals("0")) itineraries.get(0).legs.get(i).setEndTime(msg);
			// If arrival delay, set arrival time + delay.
			else if (ch.endsWith(".arr_delay") && !msg.equals("0")) itineraries.get(0).legs.get(i).arrivalDelay = Long.parseLong(msg) * 1000;
			// If departure time, set new departure time.
			else if (ch.endsWith(".dep_time") && !msg.equals("0")) itineraries.get(0).legs.get(i).setStartTime(msg);
			// If departure delay, set departure time + delay.+ 180000));//
			else if (ch.endsWith(".dep_delay") && !msg.equals("0"))
			{
				itineraries.get(0).legs.get(i).departureDelay = Long.parseLong(msg) * 1000;
				itineraries.get(0).legs.get(i).arrivalDelay = Long.parseLong(msg) * 1000;
			}
		}
	}
	if (msg.equals("Complete"))
	{
		subMsg = new Message();
	    subMsg.arg1 = 1;
	    listener.handler.sendMessage(subMsg);
	    if (itineraries.get(0).legs.size() < 2) return;
	    for (int i = 0; i < itineraries.get(0).legs.size() - 1; i++)
	    {
	    	System.out.println("i = " + i);
			// If arrival (end time) of bus x is later than departure (start time) of bus x + 1.
			if (Long.parseLong(itineraries.get(0).legs.get(i).getEndTime()) + itineraries.get(0).legs.get(i).arrivalDelay > Long.parseLong(itineraries.get(0).legs.get(i + 1).getStartTime()) + itineraries.get(0).legs.get(i + 1).departureDelay)
			{
				// Save location where connection failed.
				lastLeg = itineraries.get(0).legs.get(i);
				subMsg = new Message();
			    subMsg.obj = lastLeg;
			    listener.handler.sendMessage(subMsg);
			    break;
			}
	    }
	}
}*/
	
	public void notify(String ch, String msg)
	{
		if (msg == null) return;
		JSONObject update;
		if (itineraries == null) return;
		long startTime, endTime;
		
		startTime = 0;
		endTime = new Date().getTime();
		
		// Get the trip ID.
		String trip[] = ch.split("\\.");
		
		// Find the leg for the current trip.
		for (int i = 0; i < itineraries.get(0).legs.size(); i++)
		{
			if (itineraries.get(0).legs.get(i).tripId.equals(trip[0]))
			{
				try 
				{
					update = new JSONObject(msg);
					itineraries.get(0).legs.get(i).setEndTime(update.getString("arr_time"));
					itineraries.get(0).legs.get(i).arrivalDelay = update.getInt("arr_delay") * 1000;
					itineraries.get(0).legs.get(i).setStartTime(update.getString("dep_time"));
					itineraries.get(0).legs.get(i).departureDelay = update.getInt("dep_delay") * 1000 + update.getInt("arr_delay") * 1000;
					startTime = update.getLong("timestamp");
				} catch (JSONException e) {	e.printStackTrace(); }
				
				System.out.println("Time in ms: " + (endTime - startTime));
		    	System.out.println("Time in s: " + ((endTime - startTime) / 1000));
		    	break;
			}
		}
    	// To the Android app.
    	/*Message subMsg = new Message();
	    subMsg.arg1 = 1;
	    listener.handler.sendMessage(subMsg);
    	
	    if (itineraries.get(0).legs.size() < 2) return;
	    for (int i = 0; i < itineraries.get(0).legs.size() - 1; i++)
	    {
			// If arrival (end time) of bus x is later than departure (start time) of bus x + 1.
			if (Long.parseLong(itineraries.get(0).legs.get(i).getEndTime()) + itineraries.get(0).legs.get(i).arrivalDelay > Long.parseLong(itineraries.get(0).legs.get(i + 1).getStartTime()) + itineraries.get(0).legs.get(i + 1).departureDelay)
			{
				// Save location where connection failed.
				lastLeg = itineraries.get(0).legs.get(i);
				subMsg = new Message();
			    subMsg.obj = lastLeg;
			    listener.handler.sendMessage(subMsg);
			    break;
			}
	    }*/
	}
	
		@Override
	public void onMessage(String channel, String message) {
	    System.out.println("Message arrived / channel : " + channel + " : message : " + message);
	    if (message.endsWith("50")) unsubscribe();
	}

	@Override
	public void onPMessage(String pattern, String channel,
		String message) {
	    System.out.println("Message arrived / channel : " + channel + " : message : " + message);
	    if (channel.endsWith(".time"))
	    {
	    	long end_time = System.nanoTime();
	    	long start_time = Long.parseLong(message);
	    	System.out.println("staTime in ms: " + (start_time / 1000));
	    	System.out.println("endTime in ms: " + (end_time / 1000));
	    	System.out.println("Time in ns: " + (end_time - start_time));
	    	System.out.println("Time in ms: " + ((end_time - start_time) / 1000));
	    	System.out.println("Time in s: " + ((end_time - start_time) / 1000000));
	    }
	    notify(channel, message);
	    //Log.d("test", "test callback");
	    //listener.handler.sendMessage(new Message());
	   // listener.onTripUpdateNotify2();
	    //if (message.equals("Complete")) { System.out.println("Unsubscribing..."); punsubscribe(); }

	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
	}

	@Override // Called for each pattern unsubscription
	public void onPUnsubscribe(String pattern, int subscribedChannels) 
	{
		System.out.println("Unsubscribe!");
	}

	@Override // Called for each pattern subscription
	public void onPSubscribe(String pattern, int subscribedChannels)
	{
		//listener.setTripSub(this);
    }
}
