package ulanmo.main.fragments;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import ulanmo.main.DisplayFragmentController;
import ulanmo.main.R;
import ulanmo.main.bean.Barangay;
import ulanmo.main.bean.Place;
import ulanmo.main.bean.RainfallMeasurement;
import ulanmo.main.handlers.BarangaysHandler;
import ulanmo.main.handlers.BookmarkAdapter;
import ulanmo.main.handlers.GeocodingHandler;
import ulanmo.main.handlers.PlaceHandler;
import ulanmo.main.handlers.PlacesAutoCompleteAdapter;
import ulanmo.main.util.Utility;
import ulanmo.main.views.MeasurementObserver;
import ulanmo.main.views.RainLevelHandlers;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.InputType;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class RainMeasurementFragment extends Fragment implements
		MeasurementObserver {

	private TextView testText;
	private TextView rainfallText;
	private TextView rainfallDesc;
	private TextView accumulationText;
	private TextView accumulationDesc;
	private TextView lastUpdateText;
	private TextView addBookmarkText;
	private ImageView pastTexts[];
	private ImageView searchIcon;
	private ImageView rainfallImage;
	private ImageView accumulationImage;
	private LinearLayout subLayout;
	private LinearLayout currLayout;
	private RelativeLayout bookmarksLabel;
	private AutoCompleteTextView barangayText;
	private DisplayFragmentController parentController;
	private RainfallMeasurement currentMeasurement;
	private ArrayList<RainfallMeasurement> bookmarkedMeasurements;
	private ArrayList<RainfallMeasurement> pastMeasurements;
	private int activeRainLevel, activeFloodLevel;
	private ListView bookmarks;
	private BookmarkAdapter bookmarksAdapter;

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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			requestAllBarangays();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.measurement_view, container,
				false);

		bookmarkedMeasurements = new ArrayList<RainfallMeasurement>();
		pastMeasurements = new ArrayList<RainfallMeasurement>();

		testText = (TextView) view.findViewById(R.id.test_text);
		barangayText = (AutoCompleteTextView) view
				.findViewById(R.id.barangay_text);
		rainfallText = (TextView) view.findViewById(R.id.rainfall);
		rainfallDesc = (TextView) view.findViewById(R.id.rainfall_text);
		rainfallImage = (ImageView) view.findViewById(R.id.rainfall_image);
		accumulationText = (TextView) view.findViewById(R.id.accumulation);
		accumulationDesc = (TextView) view.findViewById(R.id.accumulation_text);
		accumulationImage = (ImageView) view
				.findViewById(R.id.accumulation_image);
		lastUpdateText = (TextView) view.findViewById(R.id.last_update);
		addBookmarkText = (TextView) view.findViewById(R.id.book_add);
		searchIcon = (ImageView) view.findViewById(R.id.search_icon);
		pastTexts = new ImageView[3];
		pastTexts[0] = (ImageView) view.findViewById(R.id.subrainI1);
		pastTexts[1] = (ImageView) view.findViewById(R.id.subrainI2);
		pastTexts[2] = (ImageView) view.findViewById(R.id.subrainI3);
		activeFloodLevel = 1;
		activeRainLevel = 1;

		bookmarks = (ListView) view.findViewById(R.id.bookmarks_list);
		bookmarksAdapter = new BookmarkAdapter(parentController,
				bookmarkedMeasurements, lastUpdateText);
		bookmarks.setAdapter(bookmarksAdapter);

		subLayout = (LinearLayout) view.findViewById(R.id.subgroup);
		currLayout = (LinearLayout) view.findViewById(R.id.current_view);
		bookmarksLabel = (RelativeLayout) view.findViewById(R.id.bookmarks_label);

		initializeListeners();
		registerForContextMenu(bookmarks);

//		Log.d("PhoneSupport", "is null: "
//				+ (view.findViewById(R.id.map_text) != null));
		if (view.findViewById(R.id.map_text) != null) {
//			Log.d("PhoneSupport", "Adding listener");
			view.findViewById(R.id.map_text).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							parentController.switchToMap();
						}
					});
		}

		return view;
	}

	private void initializeListeners() {
		bookmarks.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int pos,
					long id) {
				RainfallMeasurement selected = (RainfallMeasurement) parent
						.getAdapter().getItem(pos);
				changeActiveMeasurement(selected);
				parentController.changeActiveMarker(
						new LatLng(selected.getLat(), selected.getLng()),
						selected.getName(), BitmapDescriptorFactory
								.fromResource(RainLevelHandlers.getMarker(1)),
						true);
				barangayText.setText(selected.getName());
				updateMeasurements();
			}
		});

		bookmarks.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View arg1,
					int pos, long id) {
				final RainfallMeasurement selected = (RainfallMeasurement) parent
						.getAdapter().getItem(pos);
				AlertDialog.Builder alert = new AlertDialog.Builder(
						parentController);
				alert.setMessage("Are you sure you want to delete: "
						+ selected.getName() + "?");

				alert.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								bookmarksAdapter.remove(selected);
								selected.unregisterObserver(bookmarksAdapter);
								saveBookmarks();
							}
						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						});

				alert.show();
				return false;
			}

		});

		accumulationText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (subLayout.getVisibility() == View.GONE) {
					subLayout.setVisibility(View.VISIBLE);
				} else {
					subLayout.setVisibility(View.GONE);
				}
			}
		});
		
		bookmarksLabel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (currLayout.getVisibility() == View.GONE) {
					currLayout.setVisibility(View.VISIBLE);
				} else {
					currLayout.setVisibility(View.GONE);
					subLayout.setVisibility(View.GONE);
				}
			}
		});
		
		accumulationImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (subLayout.getVisibility() == View.GONE) {
					subLayout.setVisibility(View.VISIBLE);
				} else {
					subLayout.setVisibility(View.GONE);
				}
			}
		});

		lastUpdateText.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				refreshFavorites();
			}
		});

		addBookmarkText.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				addNewBookmark();
			}
		});

		searchIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (barangayText.requestFocus()) {
					InputMethodManager imm = (InputMethodManager) parentController
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(barangayText,
							InputMethodManager.SHOW_IMPLICIT);
				}
			}
		});
	}

	private void requestAllBarangays() {
		if (!parentController.requestAllBarangays(new BarangaysHandler(
				parentController, this)))
			testText.setText("Error fetching barangay list.");
	}

	public void setBarangayAdapter(ArrayAdapter<Barangay> adapter) {
		setToPlaceAdapter();
//        setToBarangayAdapter(adapter);
	}

    private void setToBarangayAdapter(ArrayAdapter<Barangay> adapter) {
        barangayText.setAdapter(adapter);
        final RainMeasurementFragment self = this;
        barangayText.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                if (currLayout.getVisibility() == View.GONE) {
                    currLayout.setVisibility(View.VISIBLE);
                }
                Barangay selected = (Barangay) parent.getAdapter().getItem(pos);
                barangayText.clearFocus();
//                Log.d("Auto", "hide keyboard");
                InputMethodManager in = (InputMethodManager) self.getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(barangayText.getWindowToken(), 0);
                parentController.requestMeasurements(
                        new LatLng(selected.getLat(), selected.getLng()), false);
            }
        });
    }
	
	private void setToPlaceAdapter() {
		barangayText.setAdapter(new PlacesAutoCompleteAdapter(parentController, android.R.layout.simple_list_item_1, parentController.getCommunications()));
		final RainMeasurementFragment self = this;
		barangayText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int pos,
					long id) {
				if (currLayout.getVisibility() == View.GONE) {
					currLayout.setVisibility(View.VISIBLE);
				}
				Place selected = (Place) parent.getAdapter().getItem(pos);
				barangayText.clearFocus();
//				Log.d("Auto", "hide keyboard");
				parentController.getCommunications().getPlaceInfo(new PlaceHandler(parentController, self), selected.getId());
				
				InputMethodManager in = (InputMethodManager) self.getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				in.hideSoftInputFromWindow(barangayText.getWindowToken(), 0);
			}
		});
	}

	public void requestInterpolation(double lat, double lng, boolean geocode) {
		if (!Utility.isInBound(new LatLng(lat, lng)))
			return;
		if (currLayout.getVisibility() == View.GONE) {
			currLayout.setVisibility(View.VISIBLE);
		}
		RainfallMeasurement toMeasure = checkRecent(lat, lng);
		if (toMeasure == null) {
			toMeasure = new RainfallMeasurement(
					parentController.getCommunications(), lat, lng);
//			Log.d("RainfallMeasurement", "Creating new");
			pastMeasurements.add(toMeasure);
		}

		changeActiveMeasurement(toMeasure);
		currentMeasurement.requestMeasurements();

		if (geocode) {
			barangayText.setText("Fetching location data");
			if (parentController.getCommunications().requestLocation(
					new GeocodingHandler(this, ""), lat, lng)) {
				barangayText.setEnabled(false);

			}
			if (parentController.getCommunications().isDemo()) {
				barangayText.setText("Location not fetched");
				barangayText.setEnabled(true);
				parentController.changeActiveMarker(new LatLng(lat, lng),
						"Location cannot be fetched", BitmapDescriptorFactory
								.fromResource(RainLevelHandlers
										.getMarker(activeRainLevel)), true);
			} else {
				parentController.changeActiveMarker(new LatLng(lat, lng),
						"Location being fetched", BitmapDescriptorFactory
								.fromResource(RainLevelHandlers
										.getMarker(activeRainLevel)), true);
			}
		} else {
			parentController
					.changeActiveMarker(new LatLng(lat, lng), "Interpolated",
							BitmapDescriptorFactory
									.fromResource(RainLevelHandlers
											.getMarker(1)), true);
		}

		return;
	}

	public void requestBookmarks() {
		bookmarksAdapter.clear();
		try {
			String result = getBookmarks();
			JSONArray jsonResult = new JSONArray(result);
//			Log.d("File", result);

			for (int i = 0; i < jsonResult.length(); i++) {
				JSONObject json = jsonResult.getJSONObject(i);
				RainfallMeasurement entry = new RainfallMeasurement(
						parentController.getCommunications(),
						json.getDouble("lat"), json.getDouble("lng"));
				entry.setName(json.getString("name"));

				JSONArray pastMeasurements = json.getJSONArray("lastMeasure");
				entry.setMeasurement(pastMeasurements.getDouble(0),
						pastMeasurements.getDouble(1),
						pastMeasurements.getDouble(2));

				entry.setLastUpdate(json.getString("lastUpdate"));

				addBookmark(entry);
			}
		} catch (Exception e) {
//			Log.d("File", "Error parsing.", e);
		}

		// if (!parentController.requestFavorites(new BookmarksHandler(
		// parentController, this)))
		// testText.setText("Error favorites barangay list.");
	}

	public void addBookmark(RainfallMeasurement fav) {
		fav.requestMeasurements();
		bookmarksAdapter.add(fav);

		addMapMarker(fav);
	}

	public void addNewBookmark() {
		Iterator<RainfallMeasurement> iterator = bookmarkedMeasurements
				.iterator();
		boolean unique = true;
		while (iterator.hasNext()) {
			if (iterator.next().equals(currentMeasurement)) {
				unique = false;
				break;
			}
		}
		if (!unique) {
			AlertDialog.Builder alert = new AlertDialog.Builder(
					parentController);
			alert.setMessage("You've already bookmarked that area.");
			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});
			alert.show();
			return;
		}

		if (currentMeasurement == null) {
			AlertDialog.Builder alert = new AlertDialog.Builder(
					parentController);
			alert.setMessage("There's currently no location information.");
			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});
			alert.show();
			return;
		}

		AlertDialog.Builder alert = new AlertDialog.Builder(parentController);
		alert.setMessage("Name your bookmark:");

		final EditText input = new EditText(parentController);
		input.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		input.setText(barangayText.getText());
		input.setSelectAllOnFocus(true);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				currentMeasurement.setName(value);
				bookmarksAdapter.add(currentMeasurement);
				bookmarksAdapter.updateMeasurements();
				saveBookmarks();
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		alert.show();
	}

	public void addMapMarker(RainfallMeasurement rainfall) {
		parentController.addMapMarker(
				new LatLng(rainfall.getLat(), rainfall.getLng()), rainfall
						.getName(), BitmapDescriptorFactory
						.fromResource(RainLevelHandlers.getLightMarker(rainfall
								.getRainLevel(0))), false, false, true);
	}

	public void refreshFavorites() {
		Toast.makeText(parentController, "Refreshing bookmarks",
				Toast.LENGTH_SHORT).show();
		Iterator<RainfallMeasurement> iterator = bookmarkedMeasurements
				.iterator();
		boolean isUpdated = false;
		while (iterator.hasNext()) {
			RainfallMeasurement curr = iterator.next();
			if(currentMeasurement != null)
				isUpdated = curr.equals(currentMeasurement);
			curr.requestMeasurements();
		}
		if(!isUpdated && currentMeasurement != null)
			currentMeasurement.requestMeasurements();
	}

	public void geocodingResult(String address) {
		if (address == null) {
//			Log.d("Geocoding", "received null");
			barangayText.setText("Unknown Address");
		} else {
//			Log.d("Geocoding", address);
			barangayText.setText(address);
		}
		barangayText.setEnabled(true);
	}

	public void updateMeasurements() {
//		Log.d("RainfallMeasurement", "received measurement update + "
//				+ currentMeasurement.getRainLevel(0));

		int currRainLevel = currentMeasurement.getRainLevel(0);
		int currRainLevelDay = currentMeasurement.getRainLevel(1);
		int currRainLevel5 = currentMeasurement.getRainLevel(2);

		parentController.changeActiveMarker(
				new LatLng(currentMeasurement.getLat(), currentMeasurement
						.getLng()), "Interpolated", BitmapDescriptorFactory
						.fromResource(RainLevelHandlers
								.getMarker(currRainLevel)), false);

		if (currRainLevel != activeRainLevel) {
			activeRainLevel = currRainLevel;
			rainfallText.setText(RainLevelHandlers
					.getRainLabel(activeRainLevel));
			rainfallImage.setImageResource(RainLevelHandlers
					.getLargeRainIcon(activeRainLevel));
			Utility.changeText(rainfallDesc,
					RainLevelHandlers.getRainDescription(activeRainLevel));
		}

		int currFloodLevel = currentMeasurement.getFloodLevel();
		pastTexts[2].setImageResource(RainLevelHandlers.getSmallRainIcon(currRainLevel));
		pastTexts[1].setImageResource(RainLevelHandlers.getSmallRainIcon(currRainLevelDay));
		pastTexts[0].setImageResource(RainLevelHandlers.getSmallRainIcon(currRainLevel5));

		if (currFloodLevel != activeFloodLevel) {
			activeFloodLevel = currFloodLevel;
			accumulationText.setText(RainLevelHandlers
					.getFloodLabel(activeFloodLevel));
			accumulationImage.setImageResource(RainLevelHandlers
					.getLargeFloodIcon(activeFloodLevel));
			Utility.changeText(accumulationDesc,
					RainLevelHandlers.getFloodDescription(activeFloodLevel));
		}
	}

	public RainfallMeasurement checkRecent(double lat, double lng) {
		Iterator<RainfallMeasurement> iterator = pastMeasurements.iterator();
		while (iterator.hasNext()) {
			RainfallMeasurement curr = iterator.next();
			if (curr.compareTo(lat, lng) == 0)
				return curr;
		}

		iterator = bookmarkedMeasurements.iterator();
		while (iterator.hasNext()) {
			RainfallMeasurement curr = iterator.next();
			if (curr.compareTo(lat, lng) == 0)
				return curr;
		}

		return null;
	}

	public void changeActiveMeasurement(RainfallMeasurement newActive) {
		if (currentMeasurement != null) {
			currentMeasurement.unregisterObserver(this);
			currentMeasurement.setIsActive(false);
//			Log.d("RainfallMeasurement", "changed active");
		}
		currentMeasurement = newActive;
		currentMeasurement.registerObserver(this);
		currentMeasurement.setIsActive(true);
		updateMeasurements();
	}

	public String getBookmarks() {
		SharedPreferences sharedPref = parentController.getSharedPreferences(
				getString(R.string.bookmark_key), Context.MODE_PRIVATE);
		return sharedPref.getString("bookmarks", "[]");
	}

	public boolean saveBookmarks() {
		SharedPreferences sharedPref = parentController.getSharedPreferences(
				getString(R.string.bookmark_key), Context.MODE_PRIVATE);
		JSONArray jsonArray = new JSONArray();
		Iterator<RainfallMeasurement> iterator = bookmarkedMeasurements
				.iterator();
		try {
			while (iterator.hasNext()) {
				JSONObject obj = new JSONObject();
				RainfallMeasurement curr = iterator.next();
				obj.put("lat", curr.getLat());
				obj.put("lng", curr.getLng());
				obj.put("name", curr.getName());
				obj.put("lastUpdate", curr.getLastUpdate());
				JSONArray arr = new JSONArray();
				arr.put(curr.getMeasurement(0));
				arr.put(curr.getMeasurement(1));
				arr.put(curr.getMeasurement(2));
				obj.put("lastMeasure", arr);
				jsonArray.put(obj);
			}
		} catch (Exception e) {
//			Log.d("File", "error with json");
			return false;
		}

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.clear();
		editor.putString("bookmarks", jsonArray.toString());
		editor.commit();
		return true;
	}

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}
}
