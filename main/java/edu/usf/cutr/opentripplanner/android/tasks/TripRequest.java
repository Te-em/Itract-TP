/*
 * Copyright 2011 Marcy Gordon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.usf.cutr.opentripplanner.android.tasks;

import java.io.IOException;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.opentripplanner.api.ws.Message;
import org.opentripplanner.api.ws.Request;
import org.opentripplanner.v092snapshot.api.model.Itinerary;
import org.opentripplanner.v092snapshot.api.model.TripPlan;
import org.opentripplanner.v092snapshot.api.ws.Response;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.listeners.TripRequestCompleteListener;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.util.JacksonConfig;

/**
 * AsyncTask that invokes a trip planning request to the OTP Server
 * 
 * @author Khoa Tran
 * @author Sean Barbeau (conversion to Jackson)
 */

public class TripRequest extends AsyncTask<Request, Integer, Long> {
	private Response response;
	private TripPlan tp;
	private static final String TAG = "OTP";
	private ProgressDialog progressDialog;
	private WeakReference<Activity> activity;
	private Context context;
	private String currentRequestString = "";
	private Server selectedServer;
	private TripRequestCompleteListener callback;

	public TripRequest(WeakReference<Activity> activity, Context context, Server selectedServer, TripRequestCompleteListener callback) {
		this.activity = activity;
		this.context = context;
		this.selectedServer = selectedServer;
		this.callback = callback;
		if (activity.get() != null){
			progressDialog = new ProgressDialog(activity.get());
		}
	}

	protected void onPreExecute() {
		if (activity.get() != null){
			progressDialog.setIndeterminate(true);
	        progressDialog.setCancelable(true);
			progressDialog = ProgressDialog.show(activity.get(), "",
					context.getText(R.string.tripplanner_progress), true);
		}
	}

	protected Long doInBackground(Request... reqs) {
		int count = reqs.length;
		long totalSize = 0;
		for (int i = 0; i < count; i++) tp = requestPlan(reqs[i]);
		return totalSize;
	}
	
	protected void onCancelled(Long result){
		try{		
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}catch(Exception e){
			Log.e(TAG, "Error in TripRequest Cancelled dismissing dialog: " + e);
		}
		
		if (activity.get() != null){
			AlertDialog.Builder geocoderAlert = new AlertDialog.Builder(activity.get());
			Log.d("test", "cancelled trip");
			geocoderAlert.setTitle(R.string.tripplanner_no_results_message)//tripplanner_results_title)
					.setMessage(R.string.tripplanner_error_dialog_title)//tripplanner_error_request_timeout)
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
			AlertDialog alert = geocoderAlert.create();
			alert.show();
		}
				
		Log.e(TAG, "No route to display!");
	}

	protected void onPostExecute(Long result) {
		if (activity.get() != null){
			try{		
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}catch(Exception e){
				Log.e(TAG, "Error in TripRequest PostExecute dismissing dialog: " + e);
			}
		}

		Log.d("test", "is tp null?");
		if (tp == null) Log.d("test", "null");
		if (tp.itinerary.get(0) == null); Log.d("test", "itinerary is null");
		if (tp != null && tp.itinerary.get(0) != null){//(response != null && response.getPlan() != null && response.getPlan().itinerary.get(0) != null) {

			Log.d("test", "get itineraries");
			List<Itinerary> itineraries = tp.itinerary;//response.getPlan().itinerary;
			Log.d("test", "got itineraries");
			
			callback.onTripRequestComplete(itineraries, currentRequestString);
		} else {
			// TODO - handle errors here?
			int errorCode =  response.getError().getId();

			if(response != null && response.getError() != null && errorCode != Message.PLAN_OK.getId()) {

				String msg = getErrorMessage(response.getError().getId());
				if (msg == null){
					msg = response.getError().getMsg();
				}
				if (activity.get() != null){
					AlertDialog.Builder feedback = new AlertDialog.Builder(activity.get());
					feedback.setTitle(context.getResources().getString(R.string.tripplanner_error_dialog_title));
					feedback.setMessage(msg);
					feedback.setNeutralButton(context.getResources().getString(android.R.string.ok), null);
					feedback.create().show();
				}
			}
			Log.e(TAG, "No route to display!");
		}
	}
	
	private String getErrorMessage(int errorCode){
		if (errorCode == Message.SYSTEM_ERROR.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_system));
		}
		else if (errorCode == Message.OUTSIDE_BOUNDS.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_outside_bounds));
		}
		else if (errorCode == Message.PATH_NOT_FOUND.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_path_not_found));
		}
		else if (errorCode == Message.NO_TRANSIT_TIMES.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_no_transit_times));
		}
		else if (errorCode == Message.REQUEST_TIMEOUT.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_request_timeout));
		}
		else if (errorCode == Message.BOGUS_PARAMETER.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_bogus_parameter));
		}
		else if (errorCode == Message.GEOCODE_FROM_NOT_FOUND.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_geocode_from_not_found));
		}
		else if (errorCode == Message.GEOCODE_TO_NOT_FOUND.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_geocode_to_not_found));
		}
		else if (errorCode == Message.GEOCODE_FROM_TO_NOT_FOUND.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_geocode_from_to_not_found));
		}
		else if (errorCode == Message.TOO_CLOSE.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_too_close));
		}
		else if (errorCode == Message.LOCATION_NOT_ACCESSIBLE.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_location_not_accessible));
		}
		else if (errorCode == Message.GEOCODE_FROM_AMBIGUOUS.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_geocode_from_ambiguous));
		}
		else if (errorCode == Message.GEOCODE_TO_AMBIGUOUS.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_geocode_to_ambiguous));
		}
		else if (errorCode == Message.GEOCODE_FROM_TO_AMBIGUOUS.getId()){
			return (context.getResources().getString(R.string.tripplanner_error_geocode_from_to_ambiguous));
		}
		else if (errorCode == Message.UNDERSPECIFIED_TRIANGLE.getId()
				 || errorCode == Message.TRIANGLE_NOT_AFFINE.getId()
				 || errorCode == Message.TRIANGLE_OPTIMIZE_TYPE_NOT_SET.getId()
				 || errorCode == Message.TRIANGLE_VALUES_NOT_SET.getId()){
			return(context.getResources().getString(R.string.tripplanner_error_triangle));
		}
		else {
			return null;
		}
	}
	
	private TripPlan requestPlan(Request requestParams) 
	{
		ItractTrip it = new ItractTrip();
		try {
			it.parse(requestParams);
		} catch (IOException e1) { cancel(true); } catch (JSONException e1) { cancel(true); }
		if (!it.isSuccessful()) cancel(true);
		TripPlan tp = it.getTripPlan();
		return tp;
	}
	
	/**
	 * Disable HTTP connection reuse which was buggy pre-froyo
	 */
	private void disableConnectionReuseIfNecessary() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {		
			System.setProperty("http.keepAlive", "false");
		}
	}
}
