package edu.usf.cutr.opentripplanner.android.fragments;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.Place;

import edu.usf.cutr.opentripplanner.android.fragments.PosSubscriber.BusPosition;
import edu.usf.cutr.opentripplanner.android.tasks.ItractTripSubscriber;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;

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
	
	private  MainFragment listener;
	private List<Itinerary> itineraries;
	
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
				    System.out.println("I will subscribe on " + jedis.getClient().getHost() + ":" + 
					    jedis.getClient().getPort());
				    System.out.println("On no. of channels:" + channels.length);
				    //for (int i = 0; i < channels.length; i++)
				    	//System.out.println(channels[i]);
	
				    	jedis.subscribe(new AlertSubscriber(alerts, itineraries, listener), channels);
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
	
	public AlertSubscriber()
	{}
	public AlertSubscriber(MainFragment mf)
	{
		listener = mf;
	}
	public AlertSubscriber(List<Itinerary> itineraries, MainFragment mf)
	{
		listener = mf;
		this.itineraries = itineraries;
	}
	
	public AlertSubscriber(ArrayList<Alert> alerts, List<Itinerary> itineraries, MainFragment mf)
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
		channels = new String[itineraries.get(0).legs.size() + 3];
		
		for (int i = 0; i < itineraries.get(0).legs.size(); i++)
			channels[i] = itineraries.get(0).legs.get(0).routeId;
		// The first stop of the itinerary.
		channels[channels.length - 3] = itineraries.get(0).legs.get(0).from.stopId.getId();
		// Broadcast channel for alert subscribers.
		channels[channels.length - 2] = "broadcastAlert";
		// A test channel.
		channels[channels.length - 1] = "testRoute";
		
		if (!sub.testConnection()) return;
		
		List<JedisPool> nodePoolList = sub.redisConnect();
		
		// Object to store incoming alerts.
		alerts = new ArrayList<Alert>();
		
		subTest(nodePoolList);

		System.out.println("Done...");
	 }
	
	public void notify(String ch, String msg)
	{
		if (msg == null) return;
		JSONObject al;
		Alert alert;
		long startTime, endTime;
		Message subMsg;
		
		// All alerts available at the moment have arrived.
		if (ch.equals("broadcastPos") && msg.equals("Complete"))
		{
			if (!alerts.isEmpty())
	    	{
				// To the Android app.	
		    	subMsg = new Message();
			    subMsg.obj = alerts;
			    listener.handler.sendMessage(subMsg);
	    	}
			return;
		}
		
		alert = new Alert();
		startTime = 0;
		endTime = new Date().getTime();
		
		try 
		{
			al = new JSONObject(msg);
			alert.cause = al.getString("cause");
			alert.effect = al.getString("effect");
			alert.text = al.getString("text");
			alert.ID = al.getString("ID");
			startTime = al.getLong("timestamp");
			alerts.add(alert);
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
		listener.setAlertSub(this);
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
		listener.setAlertSub(this);
    }
}

