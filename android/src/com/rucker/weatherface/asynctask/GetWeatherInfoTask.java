package com.rucker.weatherface.asynctask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class GetWeatherInfoTask extends AsyncTask<Location, Void, Void> {
	
    // the tuple key corresponding to the weather icon displayed on the watch
    private static final int ICON_KEY = 0;
    // the tuple key corresponding to the temperature displayed on the watch
    private static final int TEMP_KEY = 1;
    // This UUID identifies the weather app
    private static final UUID WEATHER_UUID = UUID.fromString("E23101B0-F418-4F72-8709-FC90799FEE98");
	private static final String TAG = "GetWeatherInfoTask";
	private Context ctx;
	
	public GetWeatherInfoTask(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	protected Void doInBackground(Location... params) {
		   // A very sketchy, rough way of getting the local weather forecast from the phone's approximate location
        // using the OpenWeatherMap webservice: http://openweathermap.org/wiki/API/JSON_API
        double latitude = params[0].getLatitude();
        double longitude = params[0].getLongitude();
        
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
                //Convert Kelvin to Fahrenheit
               int temp = (int) (temperature * 9/5 - 459.67);

                Log.d("WeatherActivity", "Got temperature of " + temp + 
                		" for location: " +  String.format("%f, %f", latitude, longitude));
                sendWeatherDataToWatch(weatherIcon, temp);
            } finally {
                urlConnection.disconnect();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		return null;
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

    public void sendWeatherDataToWatch(int weatherIconId, int temp) {
        // Build up a Pebble dictionary containing the weather icon and the current temperature in degrees celsius
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(ICON_KEY, (byte) weatherIconId);
        data.addString(TEMP_KEY, String.format("%d\u00B0F", temp));

        // Send the assembled dictionary to the weather watch-app; this is a no-op if the app isn't running or is not
        // installed
        PebbleKit.sendDataToPebble(ctx, WEATHER_UUID, data);
    }
}
