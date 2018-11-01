package com.example.sensordaten_sammler;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class GPSFragment extends Fragment implements LocationListener, View.OnClickListener {

    private static final int FINE_LOCATION_PERMISSION_CODE = 1;
    Button startStopBtnGPS;
    EditText timeIntervMs, posChangeInM;
    TextView tvLat, tvLong, tvAlt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gps, container, false);
        startStopBtnGPS = view.findViewById(R.id.bStartStopGPS);
        startStopBtnGPS.setOnClickListener(this);
        timeIntervMs = view.findViewById(R.id.minIntervallTimeGPS);
        posChangeInM = view.findViewById(R.id.minPosChangeGPS);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvLat = view.findViewById(R.id.latValueGPS);
        tvLong = view.findViewById(R.id.longValueGPS);
        tvAlt = view.findViewById(R.id.altValueGPS);
        tvLat.setText(getString(R.string.lat_valGPSEmpty, "--"));
        tvLong.setText(getString(R.string.long_valGPSEmpty, "--"));
        tvAlt.setText(getString(R.string.alt_valGPSEmpty, "--"));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("test", "in onLocationChanged");
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double altitude = location.getAltitude();

            Activity activity = getActivity();
            if (isAdded() && activity != null) {
                tvLat.setText(getString(R.string.lat_valGPS, convertLatitude(latitude)));
                tvLong.setText(getString(R.string.long_valGPS, convertLongitude(longitude)));
                tvAlt.setText(getString(R.string.alt_valGPS, altitude));
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Erlaubnis für Zugriff auf Standort-Informationen ist nun hiermit erteilt worden", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Erlaubnis für Zugriff auf Standort-Informationen nicht erteilt", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(getActivity()).setTitle("Erlaubnis benötigt").setMessage("Zum Anzeigen der GPS-Daten, wird deine Erlaubnis benötigt")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopGPS:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestFineLocationPermission();
                } else {
                    if (MainActivity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        long timeInterv;
                        float posDiff;
                        if (timeIntervMs.getText().toString().equals("") || posChangeInM.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), "Beide Eingaben werden benötigt!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            timeInterv = Long.parseLong(timeIntervMs.getText().toString());
                        } catch (NumberFormatException e) {
                            Toast.makeText(getActivity(), "Zeitintervall-Eingabe muss positiv und ganzzahlig sein!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            posDiff = Float.parseFloat(posChangeInM.getText().toString());
                        } catch (NumberFormatException e) {
                            Toast.makeText(getActivity(), "Der Positionsunterschied muss eine positive Zahl sein!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String buttonText = startStopBtnGPS.getText().toString();
                        if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_gps)) == 0) {
                            startStopBtnGPS.setText(getResources().getString(R.string.stop_listening_btn_gps));
                            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                            startStopBtnGPS.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);

                            MainActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterv, posDiff, this);
                            Location lastKnownLocation = MainActivity.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (lastKnownLocation != null) {
                                double latitude = lastKnownLocation.getLatitude();
                                double longitude = lastKnownLocation.getLongitude();
                                double altitude = lastKnownLocation.getAltitude();

                                if (tvLat != null)
                                    tvLat.setText(getString(R.string.lat_valGPS, convertLatitude(latitude)));
                                if (tvLong != null)
                                    tvLong.setText(getString(R.string.long_valGPS, convertLongitude(longitude)));
                                if (tvAlt != null)
                                    tvAlt.setText(getString(R.string.alt_valGPS, altitude));

                                JSONObject gpsData = new JSONObject();
                                JSONArray jsonArray = new JSONArray();
                                try{
                                    gpsData.put("Latitude", latitude);
                                    gpsData.put("Longitude", longitude);
                                    gpsData.put("Hoehe", altitude);
                                    jsonArray.put(gpsData);
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                                new ConnectionRest().execute("gps",jsonArray.toString());
                                Log.d("RESTAPI",jsonArray.toString());
                            }
                        } else {
                            MainActivity.locationManager.removeUpdates(this);
                            startStopBtnGPS.setText(getResources().getString(R.string.start_listening_btn_gps));
                            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                            startStopBtnGPS.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        }

                    } else {
                        Toast.makeText(getActivity(), "Dein GPS-Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
    }

    private static String convertLatitude(double latitude) {
        StringBuilder builder = new StringBuilder();

        if (latitude < 0) {
            builder.append("S ");
        } else {
            builder.append("N ");
        }

        String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
        String[] latitudeSplit = latitudeDegrees.split(":");
        builder.append(latitudeSplit[0]);
        builder.append("° ");
        builder.append(latitudeSplit[1]);
        builder.append("' ");
        builder.append(latitudeSplit[2]);
        builder.append("\"");

        return builder.toString();
    }

    private static String convertLongitude(double longitude) {
        StringBuilder builder = new StringBuilder();

        if (longitude < 0) {
            builder.append("W ");
        } else {
            builder.append("E ");
        }

        String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
        String[] longitudeSplit = longitudeDegrees.split(":");
        builder.append(longitudeSplit[0]);
        builder.append("° ");
        builder.append(longitudeSplit[1]);
        builder.append("' ");
        builder.append(longitudeSplit[2]);
        builder.append("\"");

        return builder.toString();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.locationManager.removeUpdates(this);
        String buttonText = startStopBtnGPS.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn_gps)) == 0) {
            startStopBtnGPS.setText(getResources().getString(R.string.start_listening_btn_gps));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnGPS.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

}
