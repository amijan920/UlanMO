package ulanmo.main.handlers;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import ulanmo.main.fragments.RainMeasurementFragment;
import android.location.Address;
//import android.util.Log;

public class GeocodingHandler extends AbstractHandler {
	private RainMeasurementFragment rainMeasurement;
	private String id;
	private static final String STATUS_OK = "OK";
	private static final String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";

	public GeocodingHandler(RainMeasurementFragment rainMeasurement, String id) {
		this.rainMeasurement = rainMeasurement;
		this.id = id;
	}

	@Override
	public void handle(String result) {
		try {
//			Log.d("Handler",
//					"Geocoding: rec - "
//							+ result.substring(0,
//									Math.min(100, result.length())));
			
			final JSONObject json = new JSONObject(result);
			final String status = json.getString("status");
			Address address = null;
			if (status.equals(STATUS_OK)) {
				final JSONArray a = json.getJSONArray("results");
				if (a.length() > 0) {
					Address current = new Address(Locale.getDefault());
					final JSONObject item = a.getJSONObject(0);
					JSONArray components = item
							.getJSONArray("address_components");
					String formattedAddress = "";
					for (int i = 0; i < components.length(); i++) {
						JSONObject component = components.getJSONObject(i);
						JSONArray types = component.getJSONArray("types");
						for (int j = 0; j < types.length(); j++) {
							if (types.getString(j).equals("route")) {
								formattedAddress = component
										.getString("short_name");
							} else if (types.getString(j)
									.equals("neighborhood")) {
								formattedAddress = component
										.getString("short_name");
							} else if (types.getString(j).equals("locality")) {
								formattedAddress += ", "
										+ component.getString("long_name");
							}
						}
					}
					current.setFeatureName(formattedAddress);
					address = current;
				}

			} else if (status.equals(STATUS_OVER_QUERY_LIMIT)) {

			}
			if (address != null)
				rainMeasurement.geocodingResult(address.getFeatureName());
			else
				rainMeasurement.geocodingResult(null);
		} catch (Exception e) {
//			Log.d("Handler", "Geocoding: Error parsing.", e);
		}
	}

}
