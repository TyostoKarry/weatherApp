package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ActivityNotFoundException;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

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

    public void clearWeatherInfo() {
        // Tyhjennetään säädata tekstikentät
        TextView cityNameTextView = findViewById(R.id.cityNameTextView);
        cityNameTextView.setText("");

        TextView temperatureTextView = findViewById(R.id.temperatureTextView);
        temperatureTextView.setText("");
        TextView temperatureInfoTextView = findViewById(R.id.temperatureInfoTextView);
        temperatureInfoTextView.setText("");

        TextView windTextView = findViewById(R.id.windTextView);
        windTextView.setText("");
        TextView windSpeedInfoTextView = findViewById(R.id.windSpeedInfoTextView);
        windSpeedInfoTextView.setText("");

        // Tyhjennetään sään tilan kuva
        ImageView weatherStateImageView = findViewById(R.id.weatherStateImageView);
        weatherStateImageView.setImageDrawable(null);
    }

    public void setLoadingState() {
        // Asetetaan ruudulle lataus kuvake
        ImageView weatherStateImageView = findViewById(R.id.weatherStateImageView);
        weatherStateImageView.setImageResource(R.drawable.loading);
    }

    public void getWeatherData(View view) {
        // Haetaan haluttu kaupunki cityEditText kentästä
        EditText cityEditText = findViewById(R.id.cityEditText);
        cityName = cityEditText.getText().toString();
        Log.d("LENGHT", "lenght" + cityName.length());

        // Alustetaan ruutu
        clearWeatherInfo();

        // Jos kaupunkia ei asetettu, ilmoitetaan siitä
        if(cityName.length() == 0) {
            // Ilmoitetaan ettei haettavaa kaupunkia ole asetettu
            TextView cityNameTextView = findViewById(R.id.cityNameTextView);
            String cityNotSetText = getResources().getString(R.string.city_not_set);
            cityNameTextView.setText(cityNotSetText);
        }
        // Jos haettava kaupunki asetettu
        else {
            // Asetetaan lataus kuvake
            setLoadingState();

            fetchWeatherDataByCity(cityName);
        }
    }

    private void fetchWeatherDataByCity(String city) {
        // Haetaan mittauksikön tyyppi ja kytkimen tila sharedPreferencestä
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        unitType = sharedPreferences.getString("UNIT_TYPE", "metric");
        switchState = sharedPreferences.getBoolean("SWITCH_STATE", false);

        // Luodaan URL
        String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=2f5fbf5c80f0727b778de804be6d4fa8&units=" + unitType;
        makeWeatherAPIRequest(WEATHER_URL);
    }

    private void fetchWeatherDataByCoordinates(double latitude, double longitude) {
        // Haetaan mittauksikön tyyppi ja kytkimen tila sharedPreferencestä
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        unitType = sharedPreferences.getString("UNIT_TYPE", "metric");
        switchState = sharedPreferences.getBoolean("SWITCH_STATE", false);

        // Luodaan URL
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
                    // Ilmoitetaan ongelmasta yhdistää API:in
                    TextView cityNameTextView = findViewById(R.id.cityNameTextView);
                    String apiError = getResources().getString(R.string.api_error);
                    cityNameTextView.setText(apiError);
                    // Asetetaan ruudulle virhe kuvake
                    ImageView weatherStateImageView = findViewById(R.id.weatherStateImageView);
                    weatherStateImageView.setImageResource(R.drawable.error_image);
                });
        // Lähetetään request Volleyllä == lisätään request requestqueueen
        Volley.newRequestQueue(this).add(request);
    }

    private void parseWeatherJsonAndUpdateUi(String response) {
        // Parsetaan JSON ja päivitetään näytölle lämpötila, säätila ja tuulen nopeus
        // Muunnetaan merkkijono JSON objektiksi
        try {
            // Haetaan säädata JSONObjektista
            JSONObject weatherJSON = new JSONObject(response);
            String city = weatherJSON.getString("name");
            double temperature = weatherJSON.getJSONObject("main").getDouble("temp");
            double wind = weatherJSON.getJSONObject("wind").getDouble("speed");
            String icon = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("icon");

            // Haetaan sää kuvake
            ImageView weatherStateImageView = findViewById(R.id.weatherStateImageView);
            String weatherStateImageUrl = "https://openweathermap.org/img/wn/" + icon + "@4x.png";

            // Ladataan sää kuvake näytölle
            Picasso.get()
                    .load(weatherStateImageUrl)
                    .error(R.drawable.error_image)
                    .into(weatherStateImageView);

            // Päivitetään säätiedot näytölle
            TextView cityNameTextView = findViewById(R.id.cityNameTextView);
            cityNameTextView.setText(city);
            cityName = city;

            TextView temperatureTextView = findViewById(R.id.temperatureTextView);
            TextView temperatureInfoTextView = findViewById(R.id.temperatureInfoTextView);
            String temperatureInfoText = getResources().getString(R.string.temperature);
            temperatureTextView.setText("" + temperature + (switchState ? "°F" : "°C"));
            temperatureInfoTextView.setText(temperatureInfoText);

            TextView windTextView = findViewById(R.id.windTextView);
            TextView windSpeedInfoTextView = findViewById(R.id.windSpeedInfoTextView);
            String windSpeedInfoText = getResources().getString(R.string.wind_speed);
            windTextView.setText("" + wind + (switchState ? " mph" : " m/s"));
            windSpeedInfoTextView.setText(windSpeedInfoText);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void openForeca(View view) {
        String foreca = "http://www.foreca.fi/" + cityName;
        Uri forecaUri = Uri.parse(foreca);
        Intent intent = new Intent(Intent.ACTION_VIEW, forecaUri);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            clearWeatherInfo();
            TextView cityNameTextView = findViewById(R.id.cityNameTextView);
            String noInternet = getResources().getString(R.string.no_internet_access);
            cityNameTextView.setText(noInternet);
        }
    }

    public void openSettings(View view) {
        // Asetukset näkymään
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent); // Tämä komento käynnistää aktiviteetin
    }

    public void startGPS(View view) {
        // Alustetaan ruutu
        clearWeatherInfo();
        setLoadingState();
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