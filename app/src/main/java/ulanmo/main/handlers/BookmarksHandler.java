package ulanmo.main.handlers;

import org.json.JSONArray;
import org.json.JSONObject;

import ulanmo.main.DisplayFragmentController;
import ulanmo.main.bean.RainfallMeasurement;
import ulanmo.main.fragments.RainMeasurementFragment;
//import android.util.Log;

public class BookmarksHandler extends AbstractHandler {
	RainMeasurementFragment rainMeasurement;
	DisplayFragmentController parentController;

	public BookmarksHandler(DisplayFragmentController parentActivity,
			RainMeasurementFragment rainMeasurement) {
		this.rainMeasurement = rainMeasurement;
		this.parentController = parentActivity;
	}

	@Override
	public void handle(String result) {
		try {
//			Log.d("Handler",
//				"Bookmark: rec - "
//						+ result.substring(0, Math.min(40, result.length())));
			
			JSONArray jsonResult = new JSONArray(result);
			
			for (int i = 0; i < jsonResult.length(); i++) {
				JSONObject json = jsonResult.getJSONObject(i);
				RainfallMeasurement entry = new RainfallMeasurement(parentController.getCommunications(), 
						json.getDouble("lat"), json.getDouble("lng"));
				entry.setName(json.getString("name"));
				
				JSONArray pastMeasurements = json.getJSONArray("lastMeasure");
				entry.setMeasurement(pastMeasurements.getDouble(0), pastMeasurements.getDouble(1), pastMeasurements.getDouble(2));
				
				entry.setLastUpdate(json.getString("lastUpdate"));
				
				rainMeasurement.addBookmark(entry);
			}
			
		} catch (Exception e) {
//			Log.d("Handler", "Bookmark: Error parsing.", e);
		}
	}
}
