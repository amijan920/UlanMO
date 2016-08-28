package ulanmo.main;

import java.util.Calendar;

import ulanmo.main.fragments.MapViewFragment;
import ulanmo.main.fragments.RainMeasurementFragment;
import ulanmo.main.util.DateUtility;
import android.app.DatePickerDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;

public class DisplayActivity extends DisplayFragmentController {

	private MapViewFragment mapViewFragment;
	private RainMeasurementFragment rainFragment;
	private Menu menu;
	private boolean isMultipane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Log.d("Display", "Is fragment_container2 null? "
//				+ (findViewById(R.id.fragment_container2) == null));

		int screenSize = getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
//		Log.d("Display", "Screen size: " + screenSize);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;
		float scaleFactor = metrics.density;
		float widthDp = widthPixels / scaleFactor;
		float heightDp = heightPixels / scaleFactor;
		float smallestWidth = Math.min(widthDp, heightDp);
		
//		Log.d("Display", "Screen density: " + getResources().getDisplayMetrics());
//		Log.d("Display", "Smallest width: " + smallestWidth);
		
		if(smallestWidth >= 600) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		
//		switch (screenSize) {
//		case Configuration.SCREENLAYOUT_SIZE_XLARGE:
//			Log.d("Display", "Screen size: xlarge");
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//			break;
//		case Configuration.SCREENLAYOUT_SIZE_LARGE:
//			Log.d("Display", "Screen size: large");
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
//			break;
//		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
//			Log.d("Display", "Screen size: normal");
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
//			break;
//		default:
//			Log.d("Display", "Screen size: def");
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
//		}

		setContentView(R.layout.activity_display);
		MapsInitializer.initialize(getApplicationContext());

		rainFragment = new RainMeasurementFragment();
		mapViewFragment = new MapViewFragment();

		if (findViewById(R.id.fragment_container) != null) {

			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, rainFragment).commit();
			if (findViewById(R.id.fragment_container2) != null) {
				isMultipane = true;
				getSupportFragmentManager().beginTransaction()
						.add(R.id.fragment_container2, mapViewFragment)
						.commit();
			} else {
				getSupportFragmentManager().beginTransaction()
						.add(R.id.fragment_container, mapViewFragment)
						.hide(mapViewFragment).commit();
				isMultipane = false;
			}
		}
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		super.onConnected(dataBundle);
//		int rows = 10;
//		int cols = 10;
//		float[] values = new float[rows*cols];
//		int curr = 2;
//		for(int i = 0; i < rows*cols; i++) {
//			int rand = (int)(Math.random()*3);
//			if(rand == 1) {
//				curr = Math.min(curr+1, 6);
//				values[i] = curr;
//			}
//			if(rand == 2) {
//				curr = Math.max(curr-1, 2);
//				values[i] = curr;
//			}
//		}
//		mapViewFragment.initializeOverlayInterpolations(rows, cols, new LatLng(14.8,
//				120.9), .002f, values, "2014-08-08");
	}

	public void switchToMap() {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right,
						R.anim.slide_out_left).hide(rainFragment)
				.show(mapViewFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void switchToRain() {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_left,
						R.anim.slide_out_right).hide(mapViewFragment)
				.show(rainFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isMultipane)
			switchToRain();
		setUpMapIfNeeded();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.design, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem checkable = menu.findItem(R.id.toggle_demo);
		checkable.setChecked(comm.isDemo());
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.toggle_demo:
			comm.setDemo(!comm.isDemo());
			item.setChecked(comm.isDemo());
			rainFragment.refreshFavorites();
			return true;
		case R.id.real_time:
			comm.setOnlineDemo(!comm.isOnlineDemo());
			item.setChecked(comm.isOnlineDemo());
			if(comm.isOnlineDemo()) {
				Calendar c = Calendar.getInstance();
				int mYear = c.get(Calendar.YEAR);
				int mMonth = c.get(Calendar.MONTH);
				int mDay = c.get(Calendar.DAY_OF_MONTH);
				System.out.println("the selected " + mDay);
				DatePickerDialog dialog = new DatePickerDialog(this,
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								DateUtility.setDate(DateUtility.formatDate(year,
										monthOfYear + 1, dayOfMonth));

								menu.findItem(R.id.real_time).setTitle("Demo: "+ DateUtility.formatDate(year,
										monthOfYear + 1, dayOfMonth));
								rainFragment.refreshFavorites();
							}
						}, mYear, mMonth, mDay);
				dialog.show();
			}
			else {
				menu.findItem(R.id.real_time).setTitle("Online Demo");
				rainFragment.refreshFavorites();
			}
			return true;
		default:
			return false;
		}
	}

	public void requestMeasurements(LatLng position, boolean geocode) {
		rainFragment.requestInterpolation(position.latitude,
				position.longitude, geocode);
	}

	public void mapClicked(LatLng point) {
		if (!isMultipane)
			switchToRain();
		requestMeasurements(point, true);
	}

	public void addMapMarker(LatLng position, String label,
			BitmapDescriptor bitmap, boolean animate, boolean clear,
			boolean visible) {
		mapViewFragment.addMapMarker(position, label, bitmap, animate, clear,
				visible);
	}

	public void changeActiveMarker(LatLng position, String label,
			BitmapDescriptor bitmap, boolean animate) {
		mapViewFragment.changeActiveMarker(position, label, bitmap, animate);
	}

	public void setMapCenter(LatLng position) {
		mapViewFragment.setMapCenter(position);
	}

	private boolean setUpMapIfNeeded() {
		if (!isMultipane)
			return false;
		return mapViewFragment.setUpMapIfNeeded();
	}

	public void requestFavorites() {
		rainFragment.requestBookmarks();
	}
}
