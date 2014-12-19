package edu.usf.cutr.opentripplanner.android.fragments;

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

import org.json.JSONException;
import org.json.JSONObject;

import edu.usf.cutr.opentripplanner.android.tasks.ItractSubscriber;
import edu.usf.cutr.opentripplanner.android.tasks.ItractSubscriber.BusPosition;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

//import JedisPubSubTest.TestPubSub;

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
				    //for (int i = 0; i < channels.length; i++)
				    	//System.out.println(channels[i]);
	
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
	
	/* Get positions from Redis before subscribing, to display them immediately.
	 * Is not needed at the moment, as the positions are fetched from the Itract server.
	 */
	public  void getKeysTest(JedisCluster jc) throws InterruptedException
	{
		// This code must be updated. It should send a JSON document to notify.
		//for (int i = 0; i < vehicleID.length; i++)
		//{
			//System.out.println("get from ch " + vehicleID[i]);
			//System.out.println("example " + jc.hget(vehicleID[i], "currentLocationLat"));
			//notify(channels[i] + ".pubStatus", "New");
			//notify(channels[i] + ".lat", jc.hget(vehicleID[i], "currentLocationLat"));
			//notify(channels[i] + ".lon", jc.hget(vehicleID[i], "currentLocationLon"));
			//notify(channels[i] + ".agencyId", jc.hget(vehicleID[i], "agencyId"));
			//notify(channels[i] + ".routeId", jc.hget(vehicleID[i], "routeId"));
			//notify(channels[i] + ".vehicleId", jc.hget(vehicleID[i], "vehicleId"));
		//}
		//notify("boradcast" + ".pubStatus", "Complete");
	}
	public PosSubscriber()
	{}
	
	 MainFrag listener;
	public PosSubscriber(String[] vehicles, MainFrag listener)
	{
		vehicleID = vehicles;
		this.listener = listener;
	}
	
	public PosSubscriber(ArrayList<BusPosition> positions, String[] vehicles, MainFrag listener)
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
		//channels = new String[] {"karlstad.bus1.*", "karlstad.bus1.pos"};

		channels = new String[vehicleID.length + 1];
		
		for (int i = 0; i < vehicleID.length; i++)
			channels[i] = vehicleID[i] + "";
		channels[channels.length-1] = "broadcastPos";
		
		if (!sub.testConnection()) return;
		
		List<JedisPool> nodePoolList = sub.redisConnect();
	
		// Object to store notification data.
		positions = new ArrayList<BusPosition>();
		//getKeysTest(jc);
		subTest(nodePoolList);

		System.out.println("Done...");
	 }
	
	public void notify(String ch, String msg)
	{
		if (msg == null) return;
		JSONObject vehicle;
		BusPosition position;
		long startTime, endTime;
		
		// All available positions for now have arrived. Send them to the main thread.
		if (ch.equals("broadcastPos") && msg.equals("Complete"))
		{
			if (!positions.isEmpty())
	    	{
		    	// To the Android app.
		    	Message subMsg = new Message();
			    subMsg.obj = positions;
			    listener.handler.sendMessage(subMsg);
	    	}
			return;
		}
		
		position = new BusPosition();
		startTime = 0;
		endTime = new Date().getTime();
		
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
		
		//System.out.println("Time in ms: " + (endTime - startTime));
    	//System.out.println("Time in s: " + ((endTime - startTime) / 1000));
	}
	
		@Override
	public void onMessage(String channel, String message) {
	    System.out.println("Message arrived / channel : " + channel + " : message : " + message);
	    notify(channel, message);
	}

	@Override
	public void onPMessage(String pattern, String channel,
		String message) {
	    System.out.println("Message arrived / channel : " + channel + " : message : " + message);
	    notify(channel, message);
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) 
	{
		listener.setSub(this);
	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) 
	{
		System.out.println("Unsubscribe!");
	}

	@Override // Called for each pattern unsubscription
	public void onPUnsubscribe(String pattern, int subscribedChannels) 
	{
		System.out.println("Unsubscribe!");
	}

	@Override // Called for each pattern subscription
	public void onPSubscribe(String pattern, int subscribedChannels)
	{
		listener.setSub(this);
    }
}
