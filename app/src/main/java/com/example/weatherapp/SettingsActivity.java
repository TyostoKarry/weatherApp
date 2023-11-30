package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.CompoundButton;

public class SettingsActivity extends AppCompatActivity {
    private String unitType;
    private Switch unitSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Alustetaan kytkin
        unitSwitch = findViewById(R.id.unitSwitch);

        // Haetaan kytkimen tila SharedPreferencestä ja asetetaan se kytkimelle
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        unitType = sharedPreferences.getString("UNIT_TYPE", "metric");
        boolean switchState = sharedPreferences.getBoolean("SWITCH_STATE", false);
        unitSwitch.setChecked(switchState);

        // Asetetaan tilan muutokselle kuuntelija
        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    unitType = "imperial";
                } else {
                    unitType = "metric";
                }
                // Tallennetaan tyyppi ja kytkimen tila sharedpreferenceen
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("UNIT_TYPE", unitType);
                editor.putBoolean("SWITCH_STATE", unitSwitch.isChecked());
                editor.apply();
            }
        });
    }

    public void backToMain(View view) {
        // Takaisin päänäkymään
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}