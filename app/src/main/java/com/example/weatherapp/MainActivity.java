package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private String unitType;
    private boolean switchState;
    private LocationManager locationManager;
    private String cityName = "";
    private double latitude = 0;
    private double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getWeatherData(View view) {
        // Haetaan haluttu kaupunki cityEditText kentästä
        EditText cityEditText = findViewById(R.id.cityEditText);

        cityName = cityEditText.getText().toString();
        Log.d("LENGHT", "lenght" + cityName.length());
        // Jos kaupunkia ei asetettu, haetaan paikkatiedoilla
        if(cityName.length() == 0) {
            startGPS();
        }
        // Jos haettava kaupunki asetettu
        else {
            fetchWeatherDataByCity(cityName);
        }
    }

    private void fetchWeatherDataByCity(String city) {
        // Haetaan mittauksikön tyyppi ja kytkimen tila sharedPreferencestä
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        unitType = sharedPreferences.getString("UNIT_TYPE", "metric");
        switchState = sharedPreferences.getBoolean("SWITCH_STATE", false);
        String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=2f5fbf5c80f0727b778de804be6d4fa8&units=" + unitType;
        makeWeatherAPIRequest(WEATHER_URL);
    }

    private void fetchWeatherDataByCoordinates(double latitude, double longitude) {
        // Haetaan mittauksikön tyyppi ja kytkimen tila sharedPreferencestä
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        unitType = sharedPreferences.getString("UNIT_TYPE", "metric");
        switchState = sharedPreferences.getBoolean("SWITCH_STATE", false);
        String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=2f5fbf5c80f0727b778de804be6d4fa8&units=" + unitType;
        makeWeatherAPIRequest(WEATHER_URL);
    }

    private void makeWeatherAPIRequest(String weatherUrl) {
        StringRequest request = new StringRequest(Request.Method.GET, weatherUrl,
                response -> {
                    // Säätiedot haettu onnistuneesti
                    parseWeatherJsonAndUpdateUi(response);
                },
                error -> {
                    // Verkkovirhe yms
                });
        // Lähetetään request Volleyllä == lisätään request requestqueueen
        Volley.newRequestQueue(this).add(request);
    }

    private void parseWeatherJsonAndUpdateUi(String response) {
        // Parsetaan JSON ja päivitetään näytölle lämpötila, säätila ja tuulen nopeus
        // Muunnetaan merkkijono JSON objektiksi
        try {
            JSONObject weatherJSON = new JSONObject(response);
            String city = weatherJSON.getString("name");
            String weather = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
            double temperature = weatherJSON.getJSONObject("main").getDouble("temp");
            double wind = weatherJSON.getJSONObject("wind").getDouble("speed");

            TextView cityNameTextView = findViewById(R.id.cityNameTextView);
            cityNameTextView.setText(city);
            TextView weatherTextView = findViewById(R.id.weatherTextView);
            weatherTextView.setText(weather);
            TextView temperatureTextView = findViewById(R.id.temperatureTextView);
            TextView windTextView = findViewById(R.id.windTextView);
            if(switchState == false) {
                temperatureTextView.setText("" + temperature + "°C");
                windTextView.setText("" + wind + " m/s");
            } else {
                temperatureTextView.setText("" + temperature + "°F");
                windTextView.setText("" + wind + " mph");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void openForeca(View view) {
        String foreca = "http://www.foreca.fi/" + cityName;
        Uri forecaUri = Uri.parse(foreca);
        Intent intent = new Intent(Intent.ACTION_VIEW, forecaUri);
        // Tarkastetaan/varmistetaan onko laitteella tämän intentin toteuttava palvelu
        if (intent.resolveActivity(getPackageManager()) != null) {
            // webbiselain löytyy
            startActivity(intent);
        }
        else {
            // ei webbiselainta, ei voida näyttää
        }
    }

    public void openSettings(View view) {
        // Asetukset näkymään
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("UNIT_TYPE", unitType);
        startActivity(intent); // Tämä komento käynnistää aktiviteetin
    }

    public void startGPS() {
        // Tsekataan, onko oikeudet paikkatietoon, jos ei ole, pyyde-tään oikeudet dialogilla
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Ei oikeuksia, joten pyydetään oikeudet
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Jos on oikeudet, luetaan gps ja/tai rekisteröidytään kuun-telemaan LAT LNG -parametrejä
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                fetchWeatherDataByCoordinates(latitude, longitude);
                locationManager.removeUpdates(this);
            }
        });
    }

}