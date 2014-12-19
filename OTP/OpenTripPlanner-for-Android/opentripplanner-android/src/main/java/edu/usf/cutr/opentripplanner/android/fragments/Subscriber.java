package edu.usf.cutr.opentripplanner.android.fragments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import android.util.Log;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class Subscriber 
{
	public JedisPool getRandomNode(List<JedisPool> pools) 
	{
		ArrayList<JedisPool> slaves = new ArrayList<JedisPool>();
		for (int i = 0; i < pools.size(); i++)
		{
			JedisPool jp = pools.get(i);
			try 
			{
				Jedis j = jp.getResource();
				System.out.println(j.clusterNodes());
				if (j.clusterNodes().contains("slave"))
					slaves.add(jp);
			}
			catch (JedisConnectionException e) 
			{
				System.out.println("JedisConnectionException in test");
			}
		}
		
	    return slaves.get(new Random().nextInt(slaves.size()));
	    
	    /* all this code should be replaced with
	     * return pools.get(new Random().nextInt(pools.size()));
	     * but right now it looks for slave nodes since the masters are
	     * not accessible by the Android client.
	     * When masters get a public IP, the code should be replaced.
	     * Redis Cluster works that way, that a client is not accessing
	     * a specific node, e.g. a certain slave, but the cluster directs
	     *  the client to an appropriate node, regardless of IP. 
	     */
	}
	
	public List<JedisPool> redisConnect()
	{
		Properties prop = new Properties();
		String ip = "193.10.227.200"; // This is the default IP.
		String port = "7005"; // Default port.
		
		try {
			prop.load(Subscriber.class.getResourceAsStream("config.properties"));
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
		
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		jedisClusterNodes.add(new HostAndPort(ip, Integer.parseInt(port)));
		JedisCluster jc = new JedisCluster(jedisClusterNodes);
		System.out.println("Currently " + jc.getClusterNodes().size() + " nodes in cluster");
		Map<String, JedisPool> nodeMap = jc.getClusterNodes();
		List<JedisPool> nodePoolList = new ArrayList<JedisPool>(nodeMap.values());
		Collections.shuffle(nodePoolList);
		return nodePoolList;
	}
	
	public boolean testConnection() throws InterruptedException
	{
		Properties prop = new Properties();
		String ip = "193.10.227.200"; // This is the default IP.
		
		try {
			prop.load(Subscriber.class.getResourceAsStream("config.properties"));
			ip = prop.getProperty("server_ip");
		} catch (FileNotFoundException e1) {
			System.out.println("No config file!");
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Process p1 = null;
		try {
			p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 173.194.35.133");
		} catch (IOException e) {
			Log.d("test", "io error");
			e.printStackTrace();
		}
		int returnVal = p1.waitFor();
		boolean reachable = (returnVal==0);
		if (reachable) Log.d("test", "We have Internet connection.");
		else { Log.d("test", "Might be that we have no Internet connection."); return false; }
		try {
			p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 " + ip);
		} catch (IOException e) {
			Log.d("test", "io error");
			e.printStackTrace();
		}
		returnVal = p1.waitFor();
		reachable = (returnVal==0);
		if (reachable) { Log.d("test", "We have server connection"); return true; }
		else { Log.d("test", "No server connection"); return false; }
	}

}
