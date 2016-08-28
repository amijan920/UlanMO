package ulanmo.main.util;

import java.util.HashMap;
import java.util.Map.Entry;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class Utility {
	static private LatLng southWest = new LatLng(14.3555, 120.9);
	static private LatLng northEast = new LatLng(14.8, 121.2);
	
	private Utility() {}
	
	public static boolean isInBound(LatLng position) {
		if(position.latitude < southWest.latitude)
			return false;
		if(position.latitude > northEast.latitude)
			return false;
		if(position.longitude < southWest.longitude)
			return false;
		if(position.longitude > northEast.longitude)
			return false;
		return true;
	}
	
	public static boolean writeToFile(String string, String filename) {
		return true;
	}
	
	public static String getURL(String baseURL, HashMap<String, String> params) {
		String parameters = "";
		for(Entry<String, String> e : params.entrySet()) {
			parameters += e.getKey() + "=" + e.getValue() + "&";
		}
		return baseURL + "?" + parameters;
	}
	
	static class FadeOutAnimation implements AnimationListener {
		private TextView view;
		private String textTo;
		private Animation in;
		public FadeOutAnimation(TextView view, String textTo, Animation in) {
			this.view = view;
			this.textTo = textTo;
			this.in = in;
		}
		@Override
        public void onAnimationEnd(Animation animation) {
            view.setText(textTo);
            view.startAnimation(in);
        }
		
		@Override
        public void onAnimationStart(Animation animation) {}
		@Override
        public void onAnimationRepeat(Animation animation) {}
	}
	
	public static void changeText(TextView view, String textTo) {
		Animation in = new AlphaAnimation(0.0f, 1.0f);
	    in.setDuration(200);
	    Animation out = new AlphaAnimation(1.0f, 0.0f);
	    out.setDuration(200);
	    out.setAnimationListener(new FadeOutAnimation(view, textTo, in));
	    
	    view.startAnimation(out);
	}
}
