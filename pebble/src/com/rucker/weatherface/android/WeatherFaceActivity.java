package com.rucker.weatherface.android;

import android.app.Activity;
import android.os.Bundle;
import com.rucker.weatherface.android.R;

public class WeatherFaceActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
