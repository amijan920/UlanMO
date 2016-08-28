package ulanmo.main.handlers;

import java.util.ArrayList;
import java.util.Iterator;

import ulanmo.main.DisplayFragmentController;
import ulanmo.main.R;
import ulanmo.main.bean.RainfallMeasurement;
import ulanmo.main.util.DateUtility;
import ulanmo.main.views.MeasurementObserver;
import ulanmo.main.views.RainLevelHandlers;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class BookmarkAdapter extends ArrayAdapter<RainfallMeasurement>
		implements MeasurementObserver {
	private final DisplayFragmentController parentController;
	private final int[] colors = { 0xFFAAAAAA, 0xFFAAAAAA, 0xFF4AA6A6,
			0xFF176678, 0xFFF8B81D, 0xFFD56C27, 0xFFC8342C };
	private TextView lastUpdateText;
	private ArrayList<RainfallMeasurement> ref;

	public BookmarkAdapter(DisplayFragmentController parentController,
			ArrayList<RainfallMeasurement> bookmarks, TextView lastUpdateText) {
		super(parentController, R.layout.bookmark_entry, R.id.book_name,
				bookmarks);
		Iterator<RainfallMeasurement> iterator = bookmarks.iterator();
		while (iterator.hasNext()) {
			iterator.next().registerObserver(this);
		}
		this.ref = bookmarks;
		this.parentController = parentController;
		this.lastUpdateText = lastUpdateText;
	}

	@Override
	public void add(RainfallMeasurement object) {
//		Log.d("Misc", "adding measurement");
		super.add(object);
		object.registerObserver(this);
	}

	public void updateMeasurements() {
//		Log.d("Misc", "updating adapter");
		super.notifyDataSetChanged();

		Iterator<RainfallMeasurement> iterator = ref.iterator();
		while (iterator.hasNext()) {
			RainfallMeasurement item = iterator.next();
			parentController.addMapMarker(
					new LatLng(item.getLat(), item.getLng()), item.getName(),
					BitmapDescriptorFactory.fromResource(RainLevelHandlers
							.getLightMarker(item.getRainLevel(0))), false,
					false, !item.isActive());
		}

		if (getItem(0).getLastUpdate() != null
				&& !getItem(0).getLastUpdate().equals("No update"))
			lastUpdateText.setText("Data as of " + DateUtility.getHour(getItem(0).getLastUpdate()) + ". Click to refresh estimates.");
		else
			lastUpdateText.setText("Click to refresh estimates.");
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = parentController.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.bookmark_entry, null, true);
		RainfallMeasurement item = getItem(position);

		TextView bookName = (TextView) rowView.findViewById(R.id.book_name);
		TextView bookDesc = (TextView) rowView.findViewById(R.id.book_desc);
		ImageView bookIcon = (ImageView) rowView.findViewById(R.id.book_icon);
		ImageView bookFloodIcon = (ImageView) rowView
				.findViewById(R.id.book_flood_icon);
		LinearLayout marker = (LinearLayout) rowView
				.findViewById(R.id.book_marker);
		
		bookName.setText(item.getName());
		bookDesc.setText(RainLevelHandlers.getRainLabel(item.getRainLevel(0)));
		bookIcon.setImageResource(RainLevelHandlers.getSmallRainIcon(item
				.getRainLevel(0)));
		bookFloodIcon.setImageResource(RainLevelHandlers.getSmallFloodIcon(item
				.getFloodLevel()));
		marker.setBackgroundColor(colors[item.getRainLevel(0)]);

		return rowView;
	};

}
