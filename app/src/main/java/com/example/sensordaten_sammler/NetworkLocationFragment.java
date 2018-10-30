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

public class NetworkLocationFragment extends Fragment implements LocationListener, View.OnClickListener {

    private static final int COARSE_LOCATION_PERMISSION_CODE = 2;
    Button startStopBtnNetwork;
    EditText timeIntervMs, posChangeInM;
    TextView tvLat, tvLong, tvAlt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_location, container, false);
        startStopBtnNetwork = view.findViewById(R.id.bStartStopNetwork);
        startStopBtnNetwork.setOnClickListener(this);
        timeIntervMs = view.findViewById(R.id.minIntervallTimeNetwork);
        posChangeInM = view.findViewById(R.id.minPosChangeNetwork);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvLat = view.findViewById(R.id.latValueNetwork);
        tvLong = view.findViewById(R.id.longValueNetwork);
        tvAlt = view.findViewById(R.id.altValueNetwork);
        tvLat.setText(getString(R.string.lat_valNetworkEmpty, "--"));
        tvLong.setText(getString(R.string.long_valNetworkEmpty, "--"));
        tvAlt.setText(getString(R.string.alt_valNetworkEmpty, "--"));
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
                tvLat.setText(getString(R.string.lat_valNetwork, convertLatitude(latitude)));
                tvLong.setText(getString(R.string.long_valNetwork, convertLongitude(longitude)));
                tvAlt.setText(getString(R.string.alt_valNetwork, altitude));
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
        if (requestCode == COARSE_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Erlaubnis für Zugriff auf Standort-Informationen ist nun hiermit erteilt worden", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Erlaubnis für Zugriff auf Standort-Informationen nicht erteilt", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestCoarseLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new AlertDialog.Builder(getActivity()).setTitle("Erlaubnis benötigt").setMessage("Zum Anzeigen der Netzwerk-Standortinformationen, wird deine Erlaubnis benötigt")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_CODE);
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
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopNetwork:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestCoarseLocationPermission();
                } else {
                    if (MainActivity.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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
                        String buttonText = startStopBtnNetwork.getText().toString();
                        if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_network)) == 0) {
                            startStopBtnNetwork.setText(getResources().getString(R.string.stop_listening_btn_network));
                            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                            startStopBtnNetwork.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);

                            MainActivity.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeInterv, posDiff, this);
                            Location lastKnownLocation = MainActivity.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
                            }
                        } else {
                            MainActivity.locationManager.removeUpdates(this);
                            startStopBtnNetwork.setText(getResources().getString(R.string.start_listening_btn_network));
                            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                            startStopBtnNetwork.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        }

                    } else {
                        Toast.makeText(getActivity(), "Dein Netzwerk-Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
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
        String buttonText = startStopBtnNetwork.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn_network)) == 0) {
            startStopBtnNetwork.setText(getResources().getString(R.string.start_listening_btn_network));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnNetwork.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

}
