package ulanmo.main.views;

import ulanmo.main.R;

public class RainLevelHandlers {
	private RainLevelHandlers() {}
	private static double[][] references = new double[][] {
			{0.1, 7.5, 15, 30},
			{50, 70, 100, 200},
			{100, 150, 300, 500}
	};
	private static int[][] icons = new int[][] {
		{R.drawable.g_lrain00, R.drawable.g_lrain01, R.drawable.g_lrain02, R.drawable.g_lrain03, R.drawable.g_lrain04, R.drawable.g_lrain05, R.drawable.g_lrain06},
		{R.drawable.g_srain00, R.drawable.g_srain01, R.drawable.g_srain02, R.drawable.g_srain03, R.drawable.g_srain04, R.drawable.g_srain05, R.drawable.g_srain06},
		{R.drawable.g_lrain00, R.drawable.g_lrain01, R.drawable.g_lflood02, R.drawable.g_lflood03, R.drawable.g_lflood04, R.drawable.g_lflood05, R.drawable.g_lflood06},
		{R.drawable.g_srain00, R.drawable.g_srain01, R.drawable.g_sflood02, R.drawable.g_sflood03, R.drawable.g_sflood04, R.drawable.g_sflood05, R.drawable.g_sflood06}
	};
	private static String[][] labels = new String[][] {
		{"Error", "Loading", "No Rainfall", "Light Rainfall", "Heavy Rainfall", "Intense Rainfall", "Torrential Rainfall"},
		{"Error", "Loading", "No Flood Risk", "Slight Flood Risk", "Some Flood Risk", "High Flood Risk", "Extreme Flood Risk"},
		{"Error: please check your internet connection", "...", "It's not raining", "It's drizzling", "It's raining hard, expect flooding in low lying areas", "It's raining extremely hard, expect flooding", "The rain's too heavy, you might want to evacuate soon"},
		{"Error: please check your internet connection", "...", "The roads are dry", "The roads are wet, be careful not to slip", "Some flood might be expected", "It might be hard taking your car with this much flood risk", "You might want to evacuate"}
	};
	
	private static int[] markers = new int[]{
		R.drawable.g_marker00,
		R.drawable.g_marker01,
		R.drawable.g_marker02,
		R.drawable.g_marker03,
		R.drawable.g_marker04,
		R.drawable.g_marker05,
		R.drawable.g_marker06
	};
	
	private static int[] lmarkers = new int[]{
		R.drawable.g_lmarker00,
		R.drawable.g_lmarker01,
		R.drawable.g_lmarker02,
		R.drawable.g_lmarker03,
		R.drawable.g_lmarker04,
		R.drawable.g_lmarker05,
		R.drawable.g_lmarker06
	};
	
	public static int getMarker(int rainLevel) {
		return markers[rainLevel];
	}
	
	public static int getLightMarker(int rainLevel) {
		return lmarkers[rainLevel];
	}
	
	public static int getRainLevel(int index, double rainfall) {
		if(rainfall == -1) return 1;
		if(rainfall == -2) return 0;
		if(rainfall == -3) return 0;
		if(rainfall < references[index][0]) return 2;
		if(rainfall < references[index][1]) return 3;
		if(rainfall < references[index][2]) return 4;
		if(rainfall < references[index][3]) return 5;
		return 6;
	}
	
	public static int getFloodLevel(double[] rainfall) {
		return getFloodLevelViaMax(rainfall);
	}
	
	public static int getFloodLevelViaSum(double[] rainfall) {
		int sumFloodLevel = 0;
		for(int i = 0; i < rainfall.length; i++) {
			if(rainfall[i] == -1) return 1;
			else if(rainfall[i] <= -2) return 0;
			else if(rainfall[i] < references[i][0]) sumFloodLevel += 0;
			else if(rainfall[i] < references[i][1]) sumFloodLevel += 1;
			else if(rainfall[i] < references[i][2]) sumFloodLevel += 2;
			else if(rainfall[i] < references[i][3]) sumFloodLevel += 3;
			else sumFloodLevel += 4;
		}
		return Math.max(getRainLevel(0, rainfall[0]) + 2, (sumFloodLevel / 4) + 2);
	}
	
	public static int getFloodLevelViaMax(double[] rainfall) {
		int maxFloodLevel = 0;
		for(int i = 0; i < rainfall.length; i++) {
			int level = -2;
			if(rainfall[i] == -1) return 1;
			else if(rainfall[i] <= -2) return 0;
			else if(rainfall[i] < references[i][0]) level = 2;
			else if(rainfall[i] < references[i][1]) level = 3;
			else if(rainfall[i] < references[i][2]) level = 4;
			else if(rainfall[i] < references[i][3]) level = 5;
			else level = 6;
			if(level > maxFloodLevel) {
				maxFloodLevel = level;
			}
		}
		return maxFloodLevel;
	}
	
	public static int getLargeRainIcon(int rainLevel) {
		return icons[0][rainLevel];
	}
	
	public static int getLargeFloodIcon(int floodLevel) {
		return icons[2][floodLevel];
	}
	
	public static int getSmallRainIcon(int rainLevel) {
		return icons[1][rainLevel];
	}
	
	public static int getSmallFloodIcon(int rainLevel) {
		return icons[3][rainLevel];
	}
	
	public static String getRainLabel(int rainLevel) {
		return labels[0][rainLevel];
	}
	
	public static String getFloodLabel(int floodLevel) {
		return labels[1][floodLevel];
	}
	
	public static String getRainDescription(int rainLevel) {
		return labels[2][rainLevel];
	}
	
	public static String getFloodDescription(int floodLevel) {
		return labels[3][floodLevel];
	}
}
