# Copyright (c) 2013 Rick Ucker
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.

package com.rucker.weatherface.asynctask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.rucker.weatherface.android.MainActivity;

/**
 * @see https://github.com/pebble/pebblekit/blob/master/Android/PebbleKitExample/src/com/example/PebbleKitExample/ExampleWeatherActivity.java
 * @author zulak@getpebble.com, Rick Ucker
 *
 */
public class GetWeatherInfoTask extends AsyncTask<Location, Void, HashMap<String, String>> {
	
    // the tuple key corresponding to the weather icon displayed on the watch
    private static final int ICON_KEY = 0;
    // the tuple key corresponding to the temperature displayed on the watch
    private static final int TEMP_KEY = 1;
    // This UUID identifies the weather app
    private static final UUID WEATHER_UUID = UUID.fromString("E23101B0-F418-4F72-8709-FC90799FEE98");
	private static final String TAG = "GetWeatherInfoTask";
	private Activity caller;
	private int callerViewId;
	
	/**
	 * 
	 * @param caller the Activity calling this task.
	 * @param callerViewId the ID of the TextView to be updated post-execute.
	 */
	public GetWeatherInfoTask(Activity caller, int callerViewId) {
		this.caller = caller;
		this.callerViewId = callerViewId;
	}
	
	@Override
	protected HashMap<String, String> doInBackground(Location... params) {
	   // A very sketchy, rough way of getting the local weather forecast from the phone's approximate location
       // using the OpenWeatherMap webservice: http://openweathermap.org/wiki/API/JSON_API
        double latitude = params[0].getLatitude();
        double longitude = params[0].getLongitude();
        HashMap<String, String> results = new HashMap<String, String>();
        
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
                String location = l.getString("name");
                int wtype = l.getJSONArray("weather").getJSONObject(0).getInt("id");

                int weatherIcon = getIconFromWeatherId(wtype);
                //Convert Kelvin to Fahrenheit
                String temp = String.format("%d\u00B0F", (int) (temperature * 9/5 - 459.67));
                
                results.put(MainActivity.KEY_LOCATION_NAME, location);
                results.put(MainActivity.KEY_TEMP, temp);

                Log.d("WeatherActivity", "Got temperature of " + temp + 
                		" for location: " +  String.format("%f, %f", latitude, longitude));
                sendWeatherDataToWatch(weatherIcon, temp);
            } finally {
                urlConnection.disconnect();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		return results;
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

    public void sendWeatherDataToWatch(int weatherIconId, String temp) {
        // Build up a Pebble dictionary containing the weather icon and the current temperature in degrees fahrenheit
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(ICON_KEY, (byte) weatherIconId);
        data.addString(TEMP_KEY, temp);

        // Send the assembled dictionary to the weather watch-app; this is a no-op if the app isn't running or is not
        // installed
        PebbleKit.sendDataToPebble(caller.getApplicationContext(), WEATHER_UUID, data);
    }
    
    protected void onPostExecute(HashMap<String, String> result) {
    	TextView tv = (TextView)caller.findViewById(callerViewId);
    	//FIXME: Use of constants below produces coupling between this task and the main activity.
    	tv.setText("It looks like you are near " + result.get(MainActivity.KEY_LOCATION_NAME) + ".\n" +
    			"The current temperature is " + result.get(MainActivity.KEY_TEMP) + ".");
    }
}
