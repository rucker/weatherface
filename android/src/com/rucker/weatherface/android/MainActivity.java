package com.rucker.weatherface.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.json.JSONObject;

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

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * @see https://github.com/pebble/pebblekit/blob/master/Android/PebbleKitExample/src/com/example/PebbleKitExample/ExampleWeatherActivity.java
 * @author zulak@getpebble.com, Rick Ucker
 *
 */
public class MainActivity extends Activity {
	
    // the tuple key corresponding to the weather icon displayed on the watch
    private static final int ICON_KEY = 0;
    // the tuple key corresponding to the temperature displayed on the watch
    private static final int TEMP_KEY = 1;
    // This UUID identifies the weather app
    private static final UUID WEATHER_UUID = UUID.fromString("E23101B0-F418-4F72-8709-FC90799FEE98");
    // Tag used for logging
    private static final String TAG = "Weatherface";

    private LocationManager mLocationManager;
    //ID for requests from this activity.
    private static final int activityRequestCode = 1;
    
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
		if (playSvcAvail != ConnectionResult.SUCCESS) { 
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

//        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        for (String provider : mLocationManager.getAllProviders()) {
        	mLocationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        	//TODO shouldn't these be enumerated somewhere in the API?
        	if (provider.equals("gps")) {
//        		doWeatherUpdate(mLocationManager.getLastKnownLocation(provider));
        	}
        }
    }

    public void sendWeatherDataToWatch(int weatherIconId, int temperatureCelsius) {
        // Build up a Pebble dictionary containing the weather icon and the current temperature in degrees celsius
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(ICON_KEY, (byte) weatherIconId);
        data.addString(TEMP_KEY, String.format("%d\u00B0C", temperatureCelsius));

        // Send the assembled dictionary to the weather watch-app; this is a no-op if the app isn't running or is not
        // installed
        PebbleKit.sendDataToPebble(getApplicationContext(), WEATHER_UUID, data);
    }
    
    /**
     * 
     * @param location
     */

    public void doWeatherUpdate(Location location) {
        // A very sketchy, rough way of getting the local weather forecast from the phone's approximate location
        // using the OpenWeatherMap webservice: http://openweathermap.org/wiki/API/JSON_API
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        
        Log.i(TAG, "Found latitude of " + latitude + " and longitude of " + longitude);

        try {
            URL u = new URL(String.format("http://api.openweathermap.org/data/2.1/find/city?lat=%f&lon=%f&cnt=1",
                    latitude,
                    longitude));

            HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
            try {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String json = reader.readLine();

                JSONObject jsonObject = new JSONObject(json);
                JSONObject l = jsonObject.getJSONArray("list").getJSONObject(0);
                JSONObject m = l.getJSONObject("main");
                double temperature = m.getDouble("temp");
                int wtype = l.getJSONArray("weather").getJSONObject(0).getInt("id");

                int weatherIcon = getIconFromWeatherId(wtype);
                int temp = (int) (temperature - 273.15);

                sendWeatherDataToWatch(weatherIcon, temp);
            } finally {
                urlConnection.disconnect();
            }

            Log.d("WeatherActivity", String.format("%f, %f", latitude, longitude));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getIconFromWeatherId(int weatherId) {
        if (weatherId < 600) {
            return 2;
        } else if (weatherId < 700) {
            return 3;
        } else if (weatherId > 800) {
            return 1;
        } else {
            return 0;
        }
    }
    
}
