package ulanmo.main.handlers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ulanmo.main.DisplayFragmentController;
import ulanmo.main.bean.Barangay;
import ulanmo.main.fragments.RainMeasurementFragment;
//import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.maps.model.LatLng;

public class PlaceHandler extends AbstractHandler {
	RainMeasurementFragment rainMeasurement;
	AutoCompleteTextView autoText;
	DisplayFragmentController parentController;

	public PlaceHandler(DisplayFragmentController parentActivity,
			RainMeasurementFragment rainfMeasurement) {
		this.rainMeasurement = rainMeasurement;
		this.parentController = parentActivity;
	}

	@Override
	public void handle(String result) {
		try {

//			Log.d("Handler",
//					"Place: rec - "
//							+ result.substring(0, Math.min(40, result.length())));
			
			JSONObject jsonObj = new JSONObject(result);
			JSONObject details = jsonObj.getJSONObject("result");
			JSONObject location = details.getJSONObject("geometry")
					.getJSONObject("location");

			parentController.requestMeasurements(new LatLng(location.getDouble("lat"), location.getDouble("lng")), false);
		} catch (Exception e) {
//			Log.d("Handler", "Place: Error parsing.", e);
		}

		try {

			JSONArray jsonResult = new JSONArray(result);
			Barangay arr[] = new Barangay[jsonResult.length()];
			for (int i = 0; i < arr.length; i++) {
				JSONObject json = jsonResult.getJSONObject(i);
				Barangay barangay = new Barangay();
				barangay.setBrgy(json.getString("brgy"));
				barangay.setCity(json.getString("city"));
				barangay.setZip(json.getString("zip"));
				barangay.setLat(json.getDouble("lat"));
				barangay.setLng(json.getDouble("lng"));
				arr[i] = barangay;
			}
			ArrayAdapter<Barangay> adapter = new ArrayAdapter<Barangay>(
					parentController, android.R.layout.simple_list_item_1, arr);

			rainMeasurement.setBarangayAdapter(adapter);
		} catch (Exception e) {
		}
	}
}
