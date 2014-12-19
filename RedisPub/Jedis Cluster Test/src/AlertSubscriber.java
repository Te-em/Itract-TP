

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





import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;


public class AlertSubscriber extends JedisPubSub implements Runnable
{
	private Subscriber sub;
	private  String[] channels;
	
	private  SubTestDriver listener;
	private List<SubTestDriver.Itinerary> itineraries;
	
	public class Alert
	{
		String cause, effect, text, ID;
	}
	
	ArrayList<Alert> alerts;
	public ArrayList<Alert> getAlerts() { return alerts; }
	
	public  void subTest (List<JedisPool> pools) 
	{
		JedisPool jp = sub.getRandomNode(pools);
	    
	    while (true)
		    {
			Jedis jedis = null;
			
				try 
				{
				    jedis = jp.getResource();
				    System.out.println("AlertSub will subscribe on " + jedis.getClient().getHost() + ":" + 
					    jedis.getClient().getPort());
				    System.out.println("On no. of channels:" + channels.length);
				    for (int i = 0; i < channels.length; i++)
				    	System.out.println(channels[i]);
	
				    //jedis.subscribe(new Sub(), "hello");
				    	jedis.psubscribe(new AlertSubscriber(alerts, itineraries, listener), channels);
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
		//for (int i = 0; i < routeID.length; i++)
		//{
			//System.out.println("get from ch " + vehicleID[i]);
			//System.out.println("example " + jc.hget(vehicleID[i], "currentLocationLat"));
			/*while (jc.lindex(routeID[i], 0) != null)
			{
				System.out.println("effect: " + jc.lindex(routeID[i], 1));
			}*/
			/*is.callback(channels[i] + ".pubStatus", "New");
			is.callback(channels[i] + ".lat", jc.hget(tripID[i], "currentLocationLat"));
			is.callback(channels[i] + ".lon", jc.hget(tripID[i], "currentLocationLon"));
			is.callback(channels[i] + ".agencyId", jc.hget(tripID[i], "agencyId"));
			is.callback(channels[i] + ".routeId", jc.hget(tripID[i], "routeId"));
			is.callback(channels[i] + ".vehicleId", jc.hget(tripID[i], "vehicleId"));*/
		//}
		//System.out.println("waiting set to false in sub");
		//is.waiting = false;
		//Thread.sleep(10);
	}
	
	public AlertSubscriber()
	{}
	public AlertSubscriber(SubTestDriver mf)
	{
		listener = mf;
	}
	public AlertSubscriber(List<SubTestDriver.Itinerary> itineraries, SubTestDriver mf)
	{
		listener = mf;
		this.itineraries = itineraries;
	}
	
	public AlertSubscriber(ArrayList<Alert> alerts, List<SubTestDriver.Itinerary> itineraries, SubTestDriver mf)
	{
		this.alerts = alerts;
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
		channels = new String[itineraries.get(0).legs.size() + 2];
		for (int i = 0; i < itineraries.get(0).legs.size(); i++)
			channels[i] = itineraries.get(0).legs.get(0).routeId + ".*";
		/*for (int i = 0; i < itineraries.get(0).legs.size(); i++)
			channels[i] = itineraries.get(0).legs.get(0).agencyId + ".*";*/
		channels[channels.length - 2] = itineraries.get(0).legs.get(0).from.stopId + ".*";
		channels[channels.length - 1] = "broadcastAlert.*";
		
		if (!sub.testConnection()) return;
		
		List<JedisPool> nodePoolList = sub.redisConnect();
		
		System.out.println("connected");
		alerts = new ArrayList<Alert>();
		//getKeysTest(jc);
		subTest(nodePoolList);

		System.out.println("Done...");
	 }
	
	/*public void notify(String ch, String msg)
	{
		Log.d("test", ch + ": " + msg);
		// Get the trip ID.
		//String trip[] = ch.split("\\.");
		Message subMsg;
		// Find the leg for the current trip.
		if (ch.endsWith("pubStatus") && 
				msg.equals("New")) 
			alerts.add(new Alert());
		else if (ch.endsWith("pubStatus") && msg.equals("Complete"))
		{
			subMsg = new Message();
		    subMsg.obj = alerts;
		    listener.handler.sendMessage(subMsg);
		    //alerts.clear();
			System.out.println("COMPLETE! ready to notify");
		}
		else if (!alerts.isEmpty())
		{
			if (ch.endsWith("cause")) alerts.get(alerts.size() - 1).cause = msg; 
			else if (ch.endsWith("effect")) alerts.get(alerts.size() - 1).effect = msg;
			else if (ch.endsWith("text")) alerts.get(alerts.size() - 1).text = msg;
			else if (ch.endsWith("ID")) alerts.get(alerts.size() - 1).ID = msg;
		}		
	}*/
	
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
	    //notify(channel, message);
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
		//listener.setAlertSub(this);
    }
}

