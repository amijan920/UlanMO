package ulanmo.main.handlers;

import org.json.JSONArray;

//import android.util.Log;
import android.widget.TextView;

public class StationsHandler extends AbstractHandler {
	private TextView textbox;

	public StationsHandler (TextView textbox) {
		this.textbox = textbox;
	}

	@Override
	public void handle(String result) {
		try {
			JSONArray jsonResult = new JSONArray(result);
			textbox.setText(jsonResult.getJSONObject(0).getString("address"));
//			Log.d("Handler", "Station: rec - " + result.substring(0, Math.min(40, result.length())));
		} catch(Exception e) {
//			Log.d("Handler", "Station: Error parsing.", e);
		}
	}
}
