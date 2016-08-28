package ulanmo.main.util;

public class DateUtility {
	private DateUtility() {}
	
	private static String date = "2013-08-10";
	
	public static String getDate() {
		return date;
	}
	
	public static void setDate(String newDate) {
		date = newDate;
	}
	
	public static void setToRealtime() {
		date = "";
	}
	
	public static String formatDate(int year, int month, int day) {
		return String.format("%4d-%02d-%02d", year, month, day);
	}
	
	public static String getHour(String date) {
		try{
		String[] comp = date.split(" ");
		String[] time = comp[1].split(":");
		int hour = Integer.parseInt(time[0]);
		if(hour == 0)
			return "12am";
		else if(hour <= 11)
			return hour+"am";
		else if(hour == 12)
			return "12pm";
		else
			return hour%12 + "pm";
		}
		catch(Exception e) {
			return "";
		}
	}
	
	public static String getHour() {
		return "9";
	}
}
