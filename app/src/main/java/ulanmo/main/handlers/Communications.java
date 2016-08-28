package ulanmo.main.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ulanmo.main.bean.Place;
import ulanmo.main.util.DateUtility;
import ulanmo.main.util.Utility;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
//import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Communications {
	private final String DEBUG_TAG = "Communications";

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String TYPE_DETAILS = "/details";
	private static final String OUT_JSON = "/json";

	private static final double BIAS_LAT = 14.57775;
	private static final double BIAS_LNG = 121.05;
	private static final double BIAS_RAD = 31000;

	private static final String API_KEY = "AIzaSyA1igOxyRnQVQpO7mOdAZoxYTS17GcZg6c";

	private Activity parentActivity;
	private String token;
	private HashMap<String, String> serverURLs;
	private boolean offlineDemo, onlineDemo;

	public Communications(Activity parentActivity) {
		this.parentActivity = parentActivity;
		offlineDemo = false;
		onlineDemo = false;
		token = "THETOKEN";
		serverURLs = new HashMap<String, String>();
		serverURLs.put("getAllStations",
				"http://penoy.admu.edu.ph:8180/ulanmo/getAllStations");
		serverURLs.put("getAllBarangays",
				"http://penoy.admu.edu.ph:8180/ulanmo/getAllBarangays");
		serverURLs.put("getPastHour",
				"http://penoy.admu.edu.ph:8180/ulanmo/getPastHour");
		serverURLs.put("getPast24Hours",
				"http://penoy.admu.edu.ph:8180/ulanmo/getPast24Hours");
		serverURLs.put("getPast5Days",
				"http://penoy.admu.edu.ph:8180/ulanmo/getPast5Days");
		serverURLs.put("geocoding",
				"http://maps.googleapis.com/maps/api/geocode/json");
	}

	public boolean isDemo() {
		return offlineDemo;
	}

	public boolean isOnlineDemo() {
		return onlineDemo;
	}

	public void setDemo(boolean demo) {
		this.offlineDemo = demo;
	}

	public void setOnlineDemo(boolean onlineDemo) {
		this.onlineDemo = onlineDemo;
	}

	public boolean requestAllBarangays(AbstractHandler handler) {
//		Log.d(DEBUG_TAG, "Request all barangays");

		return readBarangaysFromFile(handler);
		// Log.d(DEBUG_TAG, "RequestingAllBarangays");
		//
		// HashMap<String, String> params = new HashMap<String, String>();
		// params.put("token", "THETOKEN");
		//
		// if (checkConnection()) {
		// new
		// WebDataRequest(handler).execute(Utility.getURL(serverURLs.get("getAllBarangays"),
		// params));
		// return true;
		// } else {
		// return false;
		// }
	}

	private boolean readBarangaysFromFile(AbstractHandler handler) {
//		Log.d(DEBUG_TAG, "Reading all barangays");
		String json = null;
		try {
			InputStream is = parentActivity.getAssets().open("barangays.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
		handler.handle(json);
		return true;
	}

	public boolean requestAllStations(AbstractHandler handler) {
//		Log.d(DEBUG_TAG, "Requesting all stations");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("token", token);

		if (checkConnection()) {
			new WebDataRequest(handler).execute(Utility.getURL(
					serverURLs.get("getAllStations"), params));
			return true;
		} else {
			return false;
		}
	}

	public boolean requestLocation(AbstractHandler handler, double latitude,
			double longitude) {
//		Log.d(DEBUG_TAG, String.format("Geocoding: [lat, %f] [long,  %f]",
//				latitude, longitude));

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("sensor", "true");
		params.put("latlng", latitude + "," + longitude);
		params.put("locale", Locale.getDefault().getLanguage());

		if (checkConnection()) {
			new WebDataRequest(handler).execute(Utility.getURL(
					serverURLs.get("geocoding"), params));
			return true;
		} else
			return false;
	}

	public boolean requestPast(AbstractHandler handler, double latitude,
			double longitude, String label) {
//		Log.d(DEBUG_TAG,
//				String.format("Requesting: " + label
//						+ ": [lat, %f] [long,  %f]", latitude, longitude));

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("token", token);
		params.put("lat", Double.toString(latitude));
		params.put("lng", Double.toString(longitude));
		if (onlineDemo) {
			params.put("date", DateUtility.getDate());
			params.put("hour", DateUtility.getHour());
		}
//		Log.d("Communications", "Connectivity: " + checkConnection());
//		Log.d("Communications",
//				"Url: " + Utility.getURL(serverURLs.get(label), params));

		if (offlineDemo) {
			handler.handle(String
					.format("[{\"lat\":14.02,\"lng\":127.2,\"rainfall\":%f,\"date\":\"2013-08-18 09:00:00\"}]",
							Math.random() * 40));
			return true;
		}

		if (checkConnection()) {
			new WebDataRequest(handler).execute(Utility.getURL(
					serverURLs.get(label), params));
			return true;
		} else
			return false;
	}

	public boolean getPlaceInfo(AbstractHandler handler, String reference) {
		StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS
				+ OUT_JSON);
		sb.append("?key=" + API_KEY);
		sb.append("&placeid=" + reference);

		if (checkConnection()) {
			new WebDataRequest(handler).execute(sb.toString());
			return true;
		} else
			return false;
	}

	public boolean requestPastHour(AbstractHandler handler, double latitude,
			double longitude) {
		return requestPast(handler, latitude, longitude, "getPastHour");
	}

	public boolean requestPast24Hours(AbstractHandler handler, double latitude,
			double longitude) {
		return requestPast(handler, latitude, longitude, "getPast24Hours");
	}

	public boolean requestPast5Days(AbstractHandler handler, double latitude,
			double longitude) {
		return requestPast(handler, latitude, longitude, "getPast5Days");
	}

	private boolean checkConnection() {
		ConnectivityManager connMgr = (ConnectivityManager) parentActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//		Log.d(DEBUG_TAG, "Checking Connection [NetworkInfo!NULL, "
//				+ (networkInfo != null) + "]");
		return (networkInfo != null && networkInfo.isConnected());
	}

	public ArrayList<Place> autocomplete(String input) {
		ArrayList<Place> resultList = null;

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			StringBuilder sb = new StringBuilder(PLACES_API_BASE
					+ TYPE_AUTOCOMPLETE + OUT_JSON);
			sb.append("?key=" + API_KEY);
			sb.append("&components=country:ph");
			sb.append("&input=" + URLEncoder.encode(input, "utf8"));
			sb.append("&location=" + BIAS_LAT + "," + BIAS_LNG);
			sb.append("&radius=" + BIAS_RAD);

//			Log.d(DEBUG_TAG, "url" + sb.toString());

			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
//			Log.e(DEBUG_TAG, "Error processing Places API URL", e);
			return resultList;
		} catch (IOException e) {
//			Log.e(DEBUG_TAG, "Error connecting to Places API", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			// Create a JSON object hierarchy from the results
			// Log.d(DEBUG_TAG, "Received: " + jsonResults.toString());
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			// Extract the Place descriptions from the results
			resultList = new ArrayList<Place>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				resultList.add(new Place(predsJsonArray.getJSONObject(i)
						.getString("description"), predsJsonArray
						.getJSONObject(i).getString("place_id")));
			}
		} catch (JSONException e) {
//			Log.e(DEBUG_TAG, "Cannot process JSON results", e);
		}

		return resultList;
	}

	// private String downloadUrl(String myurl) throws IOException {
	// InputStream is = null;
	// int len = 10000;
	//
	// try {
	// Log.d(DEBUG_TAG, "download URL: " + myurl);
	// URL url = new URL(myurl);
	// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	// conn.setReadTimeout(10000);
	// conn.setConnectTimeout(15000);
	// conn.setRequestMethod("GET");
	// conn.setDoInput(true);
	//
	// conn.connect();
	// int response = conn.getResponseCode();
	// Log.d(DEBUG_TAG, "The response is: " + response);
	// is = conn.getInputStream();
	//
	// String contentAsString = readIt(is, len);
	// Log.d(DEBUG_TAG, "Decoded: " + contentAsString.substring(0, Math.min(45,
	// contentAsString.length())));
	// return contentAsString;
	//
	// }
	// catch(FileNotFoundException e) {
	// return "[{\"rainfall\":-2}]";
	// }
	// catch(Exception e) {
	// Log.d(DEBUG_TAG, "Exception: ", e);
	// return "";
	// }
	// finally {
	// if (is != null) {
	// is.close();
	// }
	// }
	// }
	//
	// private String readIt(InputStream stream, int len) throws IOException,
	// UnsupportedEncodingException {
	// Reader reader = null;
	// reader = new InputStreamReader(stream, "UTF-8");
	// char[] buffer = new char[len];
	// int length = 0;
	// while (true) {
	// int ret = reader.read(buffer, length, buffer.length - length);
	// if (ret == -1 || length == buffer.length) break;
	// length += ret;
	// }
	// return new String(buffer);
	// }

	private class WebDataRequest extends AsyncTask<String, Void, String> {
		private AbstractHandler handler;

		public WebDataRequest(AbstractHandler handler) {
			this.handler = handler;
		}

		@Override
		protected String doInBackground(String... urls) {
			int responseCode = -1;
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(urls[0]);

			try {
				HttpResponse response = client.execute(httpget);
				StatusLine statusLine = response.getStatusLine();
				responseCode = statusLine.getStatusCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
					return builder.toString();
				} else {
//					Log.d(DEBUG_TAG, String
//							.format("Unsuccessful HTTP response code: %d",
//									responseCode));
				}
			} catch (Exception e) {
//				Log.d(DEBUG_TAG, "HTTP error: ", e);
			}

			return "Unable to retrieve web page. URL may be invalid.";
		}

		@Override
		protected void onPostExecute(String result) {
			// testText.setText(result);
			handler.handle(result);
		}
	}
}
