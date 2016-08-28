package ulanmo.main.bean;

public class Barangay {
	private String zip, brgy, city;
	private double lng, lat;

	public Barangay() {
	}

	public Barangay(String zip, String brgy, String city, double lng, double lat) {
		this.zip = zip;
		this.brgy = brgy;
		this.city = city;
		this.lng = lng;
		this.lat = lat;
	}

	public String toString() {
		return brgy + ", " + city;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getBrgy() {
		return brgy;
	}

	public void setBrgy(String brgy) {
		this.brgy = brgy;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}
}
