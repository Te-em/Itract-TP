package edu.usf.cutr.opentripplanner.android.tasks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.*;

import edu.usf.cutr.opentripplanner.android.R;

import android.util.Log;
import android.widget.Toast;

public class ItractGraph {
	
	private String graphID;
	private String polygonBounds = "";
	private ArrayList<ItractGraph> graphs;
	private double[] latlon;
	private boolean success;
	
	public String getGraphID() { return graphID; }
	public String getPolygonBounds() { return polygonBounds; }
	public double[] getlatlon() { return latlon; }
	public ArrayList<ItractGraph> getGraphs() { return graphs; }
	public boolean isSuccessful() { return success; }
	
	public void setlatlon(double lllat, double lllon, double urlat, double urlon)
	{
		latlon = new double[4];
		latlon[0] = lllat;
		latlon[1] = lllon;
		latlon[2] = urlat;
		latlon[3] = urlon;
	}
	
	void parse(String serverURL) throws JSONException, IOException
	{
		JSONObject obj;
		JSONArray arr, bounds;
		success = false;
		String request = "http://itract.cs.kau.se:8081/proxy/api/secure/proxyInfo?bounds=true";
		URL url = new URL(request);
		Scanner scan = new Scanner(url.openStream());
		if (scan == null) return;
		String response = new String();
		while (scan.hasNext())
			response += scan.nextLine();
		scan.close();
		if (response.equals("[,]") || response.equals("[]") || response.equals("")) return;
		obj = new JSONObject(response);
		arr = obj.getJSONArray("graphs");
		graphID = arr.getJSONObject(0).getString("graphId");
		// Read all graph bounds.
		int g = 0;
		graphs = new ArrayList<ItractGraph>();
		while(!arr.isNull(g) || g > 10)
		{
			Log.d("test", "in graph parse loop " + g);
			ItractGraph ig = new ItractGraph();
			graphID = arr.getJSONObject(g).getString("graphId");
			if (arr.getJSONObject(g).isNull("polygonBounds")) { g++; continue; }
			bounds = arr.getJSONObject(g).getJSONArray("polygonBounds");
			for (int i = 0; i < bounds.length(); i++)
			{
				ig.polygonBounds += bounds.getJSONObject(i).getDouble("lat");
				ig.polygonBounds += ",";
				ig.polygonBounds += bounds.getJSONObject(i).getDouble("lon");
				ig.polygonBounds += ",";
			}
			graphs.add(ig);
			g++;
		}
		success = true;
	}
}
