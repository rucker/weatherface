package com.rucker.weatherface.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.rucker.weatherface.asynctask.GetWeatherInfoTask;

public class MainActivity extends Activity {
	
    // Tag used for logging
    private static final String TAG = "Weatherface";
    private LocationManager mLocationManager;
    //ID for requests from this activity.
    private static final int activityRequestCode = 1;
    public static final String KEY_LOCATION_NAME = "LOCATION";
    public static final String KEY_TEMP = "TEMP";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (doGooglePlayServicesCheck()) {
	        setContentView(R.layout.activity_main);
	        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	        updateWeather(findViewById(R.layout.activity_main));
        }
    }


    private boolean doGooglePlayServicesCheck() {
    	int playSvcAvail =  GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
    	Log.i(TAG, "Google Play Services status=" + playSvcAvail);
		if (playSvcAvail != ConnectionResult.SUCCESS && playSvcAvail != ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) { 
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playSvcAvail, this, activityRequestCode);
			errorDialog.show();
			return false;
		}
		return true;
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void updateWeather(View view) {
        // when this button is clicked, get the handset's approximate location and request weather data from a
        // third-party web service
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mLocationManager.removeUpdates(this);
                doWeatherUpdate(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        String provider = mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER).getName();
        mLocationManager.requestLocationUpdates(provider, 0, 0, locationListener);
		doWeatherUpdate(mLocationManager.getLastKnownLocation(provider));
    }
    
    /**
     * 
     * @param location
     */

    public void doWeatherUpdate(Location location) {
    	GetWeatherInfoTask gwit = new GetWeatherInfoTask(this, R.id.greeting);
    	gwit.execute(location);
    }
}
