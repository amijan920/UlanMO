package ulanmo.main.handlers;

import org.json.JSONArray;
import org.json.JSONObject;

import ulanmo.main.bean.RainfallMeasurement;
//import android.util.Log;

public class RainfallHandler extends AbstractHandler {
	private RainfallMeasurement rainMeasurement;

	public RainfallHandler(RainfallMeasurement rainView) {
		this.rainMeasurement = rainView;
	}

	@Override
	public void handle(String result) {
		try {
//			Log.d("Handler",
//					"Rainfall: rec - "
//							+ result.substring(0,
//									Math.min(100, result.length())));

			JSONArray jsonResult = new JSONArray(result);
			double past5 = 0;
			double pastHour = 0;
			double pastDay = 0;
			for (int i = 0; i < jsonResult.length(); i++) {
				JSONObject json = jsonResult.getJSONObject(i);
				if (i == 0) {
					rainMeasurement.setLastUpdate(json.getString("date"));
					pastHour += json.getDouble("rainfall");
				}
				if (i < 24)
					pastDay += json.getDouble("rainfall");
				past5 += json.getDouble("rainfall");
			}
			rainMeasurement.setMeasurement(pastHour, pastDay, past5);
		} catch (Exception e) {
			rainMeasurement.setMeasurement(-2, -2, -2);
//			Log.d("Handler", "Rainfall: Error parsing.", e);
		}
	}

}
