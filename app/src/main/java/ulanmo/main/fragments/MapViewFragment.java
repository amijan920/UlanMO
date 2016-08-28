package ulanmo.main.fragments;

import java.util.ArrayList;
import java.util.Iterator;

import ulanmo.main.DisplayFragmentController;
import ulanmo.main.views.RainLevelHandlers;
import android.app.Activity;
import android.os.Bundle;
//import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapViewFragment extends SupportMapFragment {
	private final double EPSILON = 0.0001;
	//private SupportMapFragment mapFragment;
	private ArrayList<Marker> mapMarkers;
	private Marker active;
	private GoogleMap map;
	private DisplayFragmentController parentController;
	private Circle positionCircle;
	private Polygon overlay[];
	private final int[] colors = { 0x22AAAAAA, 0x22AAAAAA, 0x224AA6A6,
			0x22176678, 0x22F8B81D, 0x22D56C27, 0x22C8342C };

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mapMarkers = new ArrayList<Marker>();
		setUpMapIfNeeded();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			parentController = (DisplayFragmentController) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	public boolean setUpMapIfNeeded() {
//		Log.d("PhoneSupport", "setting up map");
		if (map == null) {
			map = getMap();
//			Log.d("PhoneSupport", "map is null: " + (map == null));
			if (map != null) {
//				Log.d("PhoneSupport", "adding listener on map");
				map.setOnMapClickListener(new OnMapClickListener() {
					@Override
					public void onMapClick(LatLng point) {
						parentController.mapClicked(point);
					}
				});
				return true;
			}
			return false;
		}
		return true;
	}

	public void addMapMarker(LatLng position, String label,
			BitmapDescriptor bitmap, boolean animate, boolean clear,
			boolean visible) {
//		Log.d("MapMarker", "adding map marker at " + position.toString() + " " + label + " " + bitmap.toString());
		if (clear)
			removeMarkers();
		else
			removeMarkers(position);
		try {
			Marker m = map.addMarker(new MarkerOptions().position(position)
					.title(label).icon(bitmap));
			mapMarkers.add(m);
			m.setVisible(visible);
		} catch (Exception e) {
//			Log.d("MapMarker", "error", e);
		}
		if (positionCircle != null) {
			positionCircle.setCenter(position);
		}
		if (animate)
			setMapCenter(position);

//		Log.d("MapMarker", "added marker at " + mapMarkers.get(0).getPosition());
	}

	public void changeActiveMarker(LatLng position, String label,
			BitmapDescriptor bitmap, boolean animate) {
//		Log.d("MapMarker", "changing active marker to " + position.toString());

		if (active != null) {
			setVisibilityMarkers(active.getPosition(), true);
			active.remove();
		}
		active = map.addMarker(new MarkerOptions().position(position)
				.title(label).icon(bitmap));

		setVisibilityMarkers(active.getPosition(), false);
		if (animate)
			setMapCenter(position);
	}

	public void removeMarkers() {
//		Log.d("MapMarker", "removing all map markers");

		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			iterator.next().remove();
			iterator.remove();
		}
	}

	public void removeMarkers(LatLng pos) {

		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker m = iterator.next();
			if ((Math.abs(m.getPosition().latitude - pos.latitude) < EPSILON && Math
					.abs(m.getPosition().longitude - pos.longitude) < EPSILON)) {

//				Log.d("MapMarker", "removing one marker: " + m.toString());
				m.remove();
				iterator.remove();
			}
		}
	}

	public void setVisibilityMarkers(LatLng pos, boolean isVisible) {
		Iterator<Marker> iterator = mapMarkers.iterator();
		while (iterator.hasNext()) {
			Marker m = iterator.next();
			if ((Math.abs(m.getPosition().latitude - pos.latitude) < EPSILON && Math
					.abs(m.getPosition().longitude - pos.longitude) < EPSILON)) {
				m.setVisible(isVisible);
			}
		}
	}

	public void setMapCenter(LatLng position) {
		if (map.getCameraPosition().zoom > 13)
			map.animateCamera(CameraUpdateFactory.newLatLng(position));
		else
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13));
	}
	
	public void initializeOverlayInterpolations(int rows, int cols, LatLng upleftcoor, float interval, float[] values, String date) {
		overlay = new Polygon[rows*cols];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				PolygonOptions rectOptions = new PolygonOptions()
	              .add(new LatLng(upleftcoor.latitude + interval * (i - .5), upleftcoor.longitude + interval * (j - .5)),
	                   new LatLng(upleftcoor.latitude + interval * (i - .5), upleftcoor.longitude + interval * (j + .5)),
	                   new LatLng(upleftcoor.latitude + interval * (i + .5), upleftcoor.longitude + interval * (j + .5)),
	                   new LatLng(upleftcoor.latitude + interval * (i + .5), upleftcoor.longitude + interval * (j - .5)),
	                   new LatLng(upleftcoor.latitude + interval * (i - .5), upleftcoor.longitude + interval * (j - .5)))
	              .fillColor(colors[RainLevelHandlers.getRainLevel(0, values[cols*i + j])])
	              .strokeWidth(0);
				overlay[cols*i + j] = map.addPolygon(rectOptions);
			}
		}
	}
	
	public void updateOverlayInterpolations(int rows, int cols, LatLng upleftcoor, float interval,  float[] values, String date) {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				overlay[cols*i + j].setFillColor(colors[RainLevelHandlers.getRainLevel(0, values[cols*i + j])]);;
			}
		}
	}
}
