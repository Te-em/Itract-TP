

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;


public class PosSubscriber extends JedisPubSub implements Runnable
{
	private Subscriber sub;
	private  String[] channels;
	private  String[] vehicleID;
	private int thread;
	private long  ms_sum, s_sum;
	private int times;
	
	public class BusPosition
	{
		public Double lat;
		public Double lon;
		public String agencyID;
		public String routeID;
		public String vehicleID;
	}
	
	 ArrayList<BusPosition> positions;
	public ArrayList<BusPosition> getPositions() { return positions; }
	
	public  void subTest (List<JedisPool> pools) 
	{
	    JedisPool jp = sub.getRandomNode(pools);
	    
	   while (true) 
	   {
			Jedis jedis = null;
			
				try 
				{
				    jedis = jp.getResource();
				    System.out.println("PosSub will subscribe on " + jedis.getClient().getHost() + ":" + 
					    jedis.getClient().getPort());
				    System.out.println("On no. of channels:" + channels.length);
				    /*for (int i = 0; i < channels.length; i++)
				    	System.out.println(channels[i]);*/
	
				    //jedis.subscribe(new Sub(), "hello");
				    	jedis.subscribe(new PosSubscriber(positions, vehicleID, listener), channels);
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
	
	public PosSubscriber()
	{ }
	
	SubTestDriver listener;
	public PosSubscriber(String[] vehicles, SubTestDriver listener, int i)
	{
		thread = i;
		vehicleID = vehicles;
		this.listener = listener;
	}
	
	public PosSubscriber(ArrayList<BusPosition> positions, String[] vehicles, SubTestDriver listener)
	{
		vehicleID = vehicles;
		this.listener = listener;
		this.positions = positions;
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
		times = 0;
		s_sum = 0; ms_sum = 0;

		channels = new String[vehicleID.length + 1];
		
		// Create the channels to listen to.
		for (int i = 0; i < vehicleID.length; i++)
			channels[i] = vehicleID[i];
		channels[channels.length-1] = "broadcastPosTest";
		
		if (!sub.testConnection()) return;
		
		List<JedisPool> nodePoolList = sub.redisConnect();
		
		// Object to store the notification data.
		positions = new ArrayList<BusPosition>();
		
		// Start subscribing.
		subTest(nodePoolList);

		System.out.println("Done...");
	 }
	
	public void notify(String ch, String msg)
	{
		if (msg == null) return;
		JSONObject vehicle;
		BusPosition position = new BusPosition();
		long startTime, endTime, ms, s;
		
		/* When publisher has extracted positions from GTFS and published them,
		 * publisher will send a "Complete" message, telling the subscriber
		 * to notify the main thread (Android).
		 */
		if (ch.equals("broadcastPos") && msg.equals("Complete"))
		{
			if (!positions.isEmpty())
	    	{
		    	// To the Android app.
		    	/*Message subMsg = new Message();
			    subMsg.obj = positions;
			    listener.handler.sendMessage(subMsg);*/
				/*for (int i = 0; i < positions.size(); i++)
				{
					System.out.println("Thread " + thread + ": Lat/lon: " + positions.get(i).lat + "," + positions.get(i).lon);
				}*/
	    	}
			return;
		}
		
		try 
		{
			vehicle = new JSONObject(msg);
			position.lat = vehicle.getDouble("lat");
			position.lon = vehicle.getDouble("lon");
			position.agencyID = vehicle.getString("agencyId");
			position.routeID = vehicle.getString("routeId");
			position.vehicleID = vehicle.getString("vehicleId");
			startTime = vehicle.getLong("timestamp");
			positions.add(position);
		} catch (JSONException e) {	e.printStackTrace(); }
		
		startTime = 0;
		endTime = new Date().getTime();
		
		// Compute the RTT.
		ms = endTime - startTime;
		s = (endTime - startTime) / 1000;
		
		ms_sum += ms;
		s_sum += s;
		times++;
		
		System.out.println(ms + " ms");
    	System.out.println(s + " s");
    	
    	System.out.println(ms_sum / times + " ms mean");
    	System.out.println(s_sum / times + " s mean");
	}
	
		@Override
	public void onMessage(String channel, String message) {
	    System.out.println("Message arrived / channel : " + channel);
	    notify(channel, message);
	}

	@Override
	public void onPMessage(String pattern, String channel,
		String message) {
	    System.out.println("Message arrived / channel : " + channel + " : message : " + message);
	    notify(channel, message);
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) 
	{
		System.out.println("Unsubscribe!");
	}

	@Override // Called for each pattern unsubscription
	public void onPUnsubscribe(String pattern, int subscribedChannels) 
	{
		
	}

	@Override // Called for each pattern subscription
	public void onPSubscribe(String pattern, int subscribedChannels)
	{
		//listener.setSub(this);
    }
}
