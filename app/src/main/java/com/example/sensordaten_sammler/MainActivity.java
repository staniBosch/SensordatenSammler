package com.example.sensordaten_sammler;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    public static SensorManager sensorManager;
    public static LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Session.getID(getApplicationContext());
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LocationFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_loc);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.loc_title_text);
        }


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_accelerometer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AccelerometerFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.accelerometer_title_text);
                break;
            case R.id.nav_pressure:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PressureFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.air_pressure_title_text);
                break;
            case R.id.nav_humidity:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HumidityFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.humidity_title_text);
                break;
            case R.id.nav_env_temp:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new EnvTempFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.env_temp_title_text);
                break;
            case R.id.nav_proximity:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProximityFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.proximity_title_text);
                break;
            case R.id.nav_battery:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BatteryFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.battery_title_text);
                break;
            case R.id.nav_loc:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LocationFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.loc_title_text);
                break;
            case R.id.nav_wifi:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new WiFiFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.wifi_title_text);
                break;
            case R.id.nav_light:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new LightFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.light_title_text);
                break;
            case R.id.nav_bluetooth:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BluetoothFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.bluetooth_title_text);
                break;
            case R.id.nav_compass:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CompassFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.compass_title_text);
                break;
            case R.id.nav_magnetometer:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MagnetometerFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.magnetometer_title_text);
                break;
            case R.id.nav_gyro:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new GyroFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.gyro_title_text);
                break;
            case R.id.nav_gravity:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new GravityFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.gravity_title_text);
                break;
            case R.id.nav_step_counter:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new StepCounterFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.step_counter_title_text);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.settings_button:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new SettingsFragment()).commit();
//                if (getSupportActionBar() != null)
//                    getSupportActionBar().setTitle(R.string.settings_title_text);
//                break;
            case R.id.all_sensors_button:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ListofAllAvailableSensorsFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.sensor_list_title);
                break;
            case R.id.all_sessions_btn:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SessionFragment()).commit();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle("Session List");
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }
}