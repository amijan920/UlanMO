package ulanmo.main.bean;

import java.util.ArrayList;
import java.util.Iterator;

import ulanmo.main.handlers.Communications;
import ulanmo.main.handlers.RainfallHandler;
import ulanmo.main.views.MeasurementObserver;
import ulanmo.main.views.RainLevelHandlers;
import android.util.Log;

public class RainfallMeasurement implements Comparable<RainfallMeasurement> {
	private final double EPSILON = 0.001;
	private final String DEBUG_TAG = "RainfallMeasurement";
	private String name, lastUpdate;
	private double lng;
	private double lat;
	private double[] measurements;
	private int[] warnings;
	private int floodLevel;
	private boolean active;
	private ArrayList<MeasurementObserver> observers;
	private Communications comm;

	public RainfallMeasurement(Communications comm, double lat, double lng) {
		this.comm = comm;
		this.lat = lat;
		this.lng = lng;
		this.name = String.format("%.2f, %.2f", lat, lng);

		lastUpdate = "No updates";
		observers = new ArrayList<MeasurementObserver>();
		measurements = new double[] { -1, -1, -1 };
		warnings = new int[] { 1, 1, 1 };
		floodLevel = RainLevelHandlers.getFloodLevel(measurements);
		active = false;
	}

	public double getLng() {
		return lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	public void setIsActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void requestMeasurements() {
		setMeasurement(-1, -1, -1);
		if (!comm.requestPast5Days(new RainfallHandler(this), lat, lng))
			setMeasurement(-2, -2, -2);
	}

	public void setMeasurement(double v0, double v1, double v2) {
		Log.d(DEBUG_TAG, "Measurement set to: [" + v0 + ", " + v1 + ", " + v2
				+ "]");

		measurements[0] = v0;
		measurements[1] = v1;
		measurements[2] = v2;
		refreshWarnings();
		updateObservers();
	}

	public void refreshWarnings() {
		warnings[0] = RainLevelHandlers.getRainLevel(0, measurements[0]);
		warnings[1] = RainLevelHandlers.getRainLevel(1, measurements[1]);
		warnings[2] = RainLevelHandlers.getRainLevel(2, measurements[2]);
		floodLevel = RainLevelHandlers.getFloodLevel(measurements);
	}

	public double getMeasurement(int index) {
		return measurements[index];
	}

	public int getRainLevel(int index) {
		return warnings[index];
	}

	public int getFloodLevel() {
		return floodLevel;
	}

	public double[] getMeasurements() {
		return measurements;
	}

	public void registerObserver(MeasurementObserver o) {
		if (!observers.contains(o))
			observers.add(o);
	}

	public void unregisterObserver(MeasurementObserver o) {
		if (observers.contains(o))
			observers.add(o);
	}

	private void updateObservers() {
		Iterator<MeasurementObserver> iterator = observers.iterator();
		while (iterator.hasNext()) {
			iterator.next().updateMeasurements();
		}
	}

	public int compareTo(RainfallMeasurement r) {
		return compareTo(r.lat, r.lng);
	}

	public int compareTo(double lat, double lng) {
		if (Math.abs(lat - this.lat) < EPSILON
				&& Math.abs(lng - this.lng) < EPSILON) {
			return 0;
		}
		return -1;
	}

	@Override
	public boolean equals(Object o) {
		try {
			if (compareTo((RainfallMeasurement) o) == 0)
				return true;
			return false;
		} catch (ClassCastException e) {
			return false;
		}
	}
}
