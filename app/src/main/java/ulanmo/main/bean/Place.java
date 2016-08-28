package ulanmo.main.bean;

public class Place {
	private String address;
	private String placeId;
	public Place(String address, String placeId) {
		this.address = address;
		this.placeId = placeId;
	}
	
	public String toString() {
		return address;
	}
	public String getId() {
		return placeId;
	}
}