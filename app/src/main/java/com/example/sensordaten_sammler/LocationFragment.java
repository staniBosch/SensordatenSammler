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
import android.os.Debug;
import android.os.Looper;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationFragment extends Fragment implements LocationListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int FINE_LOCATION_PERMISSION_CODE = 1;
    Button startStopBtn, svBtn, choosedLocMethBtn;
    EditText timeIntervMs, posChangeInM, fastesTimeIntervMs;
    TextView tvLatHighAcc, tvLongHighAcc, tvAltHighAcc, tvSpeedHighAcc, tvAccHighAcc
            , tvLatBalanced, tvLongBalanced, tvAltBalanced, tvSpeedBalanced, tvAccBalanced
            ,tvLatLowPow, tvLongLowPow, tvAltLowPow, tvSpeedLowPow, tvAccLowPow
            ,tvLatNoPow, tvLongNoPow, tvAltNoPow, tvSpeedNoPow, tvAccNoPow
            ,tvLatGPS, tvLongGPS, tvAltGPS, tvSpeedGPS, tvAccGPS
            ,tvLatNetwork, tvLongNetwork, tvAltNetwork, tvSpeedNetwork, tvAccNetwork;
    String fileNameGPS = "GPSFile.csv", fileNameNetwork = "NetworkFile.csv"
            , fileNameHighAcc = "HighAccFile.csv", fileNameBalanced = "BalancedFile.csv", fileNameLowPow = "LowPowFile.csv", fileNameNoPow = "NoPowFile.csv";
    CheckBox csv;
    String[] listItems;
    boolean[] checkedItems;
    ArrayList<Integer> selectedItems;
    private boolean startButtonPressed = false;
    double latitudeGPS, latitudeNetwork, latitudeHighAcc, latitudeBalanced, latitudeLowPow, latitudeNoPow;
    double longitudeGPS, longitudeNetwork, longitudeHighAcc, longitudeBalanced, longitudeLowPow, longitudeNoPow;
    double altitudeGPS, altitudeNetwork, altitudeHighAcc, altitudeBalanced, altitudeLowPow, altitudeNoPow;
    float speedGPS, speedNetwork, speedHighAcc, speedBalanced, speedLowPow, speedNoPow, accuracyGPS, accuracyHighAcc, accuracyBalanced, accuracyLowPow, accuracyNoPow, accuracyNetwork;
    public LocationRequest locationRequestHighAcc, locationRequestBalanced, locationRequestLowPower, locationRequestNoPower;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallbackHighAcc, mLocationCallbackBalanced, mLocationCallbackLowPow, mLocationCallbackNoPow;
    GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        startStopBtn = view.findViewById(R.id.bStartStopLoc);
        choosedLocMethBtn = view.findViewById(R.id.chooseLocMethodsBtn);
        startStopBtn.setOnClickListener(this);
        choosedLocMethBtn.setOnClickListener(this);
        svBtn = view.findViewById(R.id.svbtnLocMan);
        csv = view.findViewById(R.id.csvBoxLoc);
        csv.setEnabled(true);
        selectedItems = new ArrayList<>();
        listItems = getResources().getStringArray(R.array.loc_methods_items);
        checkedItems = new boolean[listItems.length];
        saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ", Genauigkeit" + "\n", fileNameGPS, false);
        saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ", Genauigkeit" + "\n", fileNameNetwork, false);
        saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ", Genauigkeit" + "\n", fileNameHighAcc, false);
        saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ", Genauigkeit" + "\n", fileNameBalanced, false);
        saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ", Genauigkeit" + "\n", fileNameLowPow, false);
        saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ", Genauigkeit" + "\n", fileNameNoPow, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTVs(view);
        timeIntervMs = view.findViewById(R.id.minIntervallTimeLoc);
        posChangeInM = view.findViewById(R.id.minPosChangeLocMan);
        fastesTimeIntervMs = view.findViewById(R.id.fastesIntervallTimeLoc);
        timeIntervMs.setVisibility(View.INVISIBLE);
        posChangeInM.setVisibility(View.INVISIBLE);
        fastesTimeIntervMs.setVisibility(View.INVISIBLE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        svBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sendDataRest("gps", latitudeGPS, longitudeGPS, altitudeGPS);
               sendDataRest("netzwerklokalisierung", latitudeNetwork, longitudeNetwork, altitudeNetwork);
            }
        });
    }

    private void initTVs(View view){
        tvLatHighAcc = view.findViewById(R.id.tvLatHighAcc);
        tvLongHighAcc = view.findViewById(R.id.tvLonHighAcc);
        tvAltHighAcc = view.findViewById(R.id.tvAltHighAcc);
        tvSpeedHighAcc = view.findViewById(R.id.tvSpeedHighAcc);
        tvAccHighAcc = view.findViewById(R.id.tvAccHighAcc);
        tvLatBalanced = view.findViewById(R.id.tvLatBalanced);
        tvLongBalanced = view.findViewById(R.id.tvLonBalanced);
        tvAltBalanced = view.findViewById(R.id.tvAltBalanced);
        tvSpeedBalanced = view.findViewById(R.id.tvSpeedBalanced);
        tvAccBalanced = view.findViewById(R.id.tvAccBalanced);
        tvLatLowPow = view.findViewById(R.id.tvLatLowPow);
        tvLongLowPow = view.findViewById(R.id.tvLonLowPow);
        tvAltLowPow = view.findViewById(R.id.tvAltLowPow);
        tvSpeedLowPow = view.findViewById(R.id.tvSpeedLowPow);
        tvAccLowPow = view.findViewById(R.id.tvAccLowPow);
        tvLatNoPow = view.findViewById(R.id.tvLatNoPow);
        tvLongNoPow = view.findViewById(R.id.tvLonNoPow);
        tvAltNoPow = view.findViewById(R.id.tvAltNoPow);
        tvSpeedNoPow = view.findViewById(R.id.tvSpeedNoPow);
        tvAccNoPow = view.findViewById(R.id.tvAccNoPow);
        tvLatGPS = view.findViewById(R.id.tvLatGPS);
        tvLongGPS = view.findViewById(R.id.tvLonGPS);
        tvAltGPS = view.findViewById(R.id.tvAltGPS);
        tvSpeedGPS = view.findViewById(R.id.tvSpeedGPS);
        tvAccGPS = view.findViewById(R.id.tvAccGPS);
        tvLatNetwork = view.findViewById(R.id.tvLatNetwork);
        tvLongNetwork = view.findViewById(R.id.tvLonNetwork);
        tvAltNetwork = view.findViewById(R.id.tvAltNetwork);
        tvSpeedNetwork = view.findViewById(R.id.tvSpeedNetwork);
        tvAccNetwork = view.findViewById(R.id.tvAccNetwork);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if(location.getProvider().equalsIgnoreCase("gps")){
                this.latitudeGPS = location.getLatitude();
                this.longitudeGPS = location.getLongitude();
                this.altitudeGPS = location.getAltitude();
                this.speedGPS = location.getSpeed();
                this.accuracyGPS = location.getAccuracy();
                Activity activity = getActivity();
                String lat = convertLatitude(latitudeGPS);
                String lon = convertLongitude(longitudeGPS);
                if (isAdded() && activity != null) {
                    tvLatGPS.setText(lat);
                    tvLongGPS.setText(lon);
                    tvAltGPS.setText(Double.toString(altitudeGPS));
                    tvSpeedGPS.setText(Float.toString(speedGPS));
                    if(csv.isChecked()) {
                        saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeGPS + "," + speedGPS + "," + accuracyGPS + "\n", fileNameGPS, true);
                    }
                }
            }
            else if(location.getProvider().equalsIgnoreCase("network")){
                this.latitudeNetwork = location.getLatitude();
                this.longitudeNetwork = location.getLongitude();
                this.altitudeNetwork = location.getAltitude();
                this.speedNetwork = location.getSpeed();
                this.accuracyNetwork = location.getAccuracy();
                Activity activity = getActivity();
                String lat = convertLatitude(latitudeNetwork);
                String lon = convertLongitude(longitudeNetwork);
                if (isAdded() && activity != null) {
                    tvLatNetwork.setText(lat);
                    tvLongNetwork.setText(lon);
                    tvAltNetwork.setText(Double.toString(altitudeNetwork));
                    tvSpeedNetwork.setText(Float.toString(speedNetwork));
                    if(csv.isChecked()) {
                        saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeNetwork + "," + speedNetwork + "," + accuracyNetwork + "\n", fileNameNetwork, true);
                    }
                }
            }
        }
    }

    private void onLocationChangedHighAcc(Location location) {
        this.latitudeHighAcc = location.getLatitude();
        this.longitudeHighAcc  = location.getLongitude();
        this.altitudeHighAcc  = location.getAltitude();
        this.speedHighAcc  = location.getSpeed();
        this.accuracyHighAcc = location.getAccuracy();
        Activity activity = getActivity();
        String lat = convertLatitude(latitudeHighAcc);
        String lon = convertLongitude(longitudeHighAcc);
        if (isAdded() && activity != null) {
            tvLatHighAcc .setText(lat);
            tvLongHighAcc .setText(lon);
            tvAltHighAcc .setText(Double.toString(altitudeHighAcc ));
            tvSpeedHighAcc .setText(Float.toString(speedHighAcc ));
            tvAccHighAcc .setText(Float.toString(accuracyHighAcc ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeHighAcc + "," + speedHighAcc + "," + accuracyHighAcc + "\n", fileNameHighAcc, true);
            }
        }
    }

    private void onLocationChangedBalanced(Location location) {
        this.latitudeBalanced = location.getLatitude();
        this.longitudeBalanced  = location.getLongitude();
        this.altitudeBalanced  = location.getAltitude();
        this.speedBalanced  = location.getSpeed();
        this.accuracyBalanced = location.getAccuracy();
        Activity activity = getActivity();
        String lat = convertLatitude(latitudeBalanced);
        String lon = convertLongitude(longitudeBalanced);
        if (isAdded() && activity != null) {
            tvLatBalanced .setText(lat);
            tvLongBalanced .setText(lon);
            tvAltBalanced .setText(Double.toString(altitudeBalanced ));
            tvSpeedBalanced .setText(Float.toString(speedBalanced ));
            tvAccBalanced .setText(Float.toString(accuracyBalanced ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeBalanced + "," + speedBalanced + "," + accuracyBalanced + "\n", fileNameBalanced, true);
            }
        }
    }

    private void onLocationChangedLowPow(Location location) {
        this.latitudeLowPow = location.getLatitude();
        this.longitudeLowPow  = location.getLongitude();
        this.altitudeLowPow  = location.getAltitude();
        this.speedLowPow  = location.getSpeed();
        this.accuracyLowPow = location.getAccuracy();
        Activity activity = getActivity();
        String lat = convertLatitude(latitudeLowPow);
        String lon = convertLongitude(longitudeLowPow);
        if (isAdded() && activity != null) {
            tvLatLowPow .setText(lat);
            tvLongLowPow .setText(lon);
            tvAltLowPow .setText(Double.toString(altitudeLowPow ));
            tvSpeedLowPow .setText(Float.toString(speedLowPow ));
            tvAccLowPow .setText(Float.toString(accuracyLowPow ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeLowPow + "," + speedLowPow + "," + accuracyLowPow + "\n", fileNameLowPow, true);
            }
        }
    }

    private void onLocationChangedNoPow(Location location) {
        this.latitudeNoPow = location.getLatitude();
        this.longitudeNoPow  = location.getLongitude();
        this.altitudeNoPow = location.getAltitude();
        this.speedNoPow  = location.getSpeed();
        this.accuracyNoPow = location.getAccuracy();
        Activity activity = getActivity();
        String lat = convertLatitude(latitudeNoPow);
        String lon = convertLongitude(longitudeNoPow);
        if (isAdded() && activity != null) {
            tvLatNoPow .setText(lat);
            tvLongNoPow .setText(lon);
            tvAltNoPow .setText(Double.toString(altitudeNoPow ));
            tvSpeedNoPow .setText(Float.toString(speedNoPow ));
            tvAccNoPow .setText(Double.toString(accuracyNoPow ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeNoPow + "," + speedNoPow + ", " + accuracyNoPow + "\n", fileNameNoPow, true);
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
            case R.id.bStartStopLoc:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestFineLocationPermission();
                } else {
                    if(selectedItems.isEmpty()){
                        Toast.makeText(getActivity(), "Du hast kein Positionierungsverfahren ausgewählt!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long timeInterv, fastesTimeInterv;
                    float posDiff;
                    String buttonText = startStopBtn.getText().toString();
                    if(selectedItems.contains(0)) {  // GPS_PROVIDER vom LocationManager ausgewählt
                        if (MainActivity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            if (timeIntervMs.getText().toString().equals("") || posChangeInM.getText().toString().equals("")) {
                                Toast.makeText(getActivity(), "Es werden die Eingaben benötigt!", Toast.LENGTH_SHORT).show();
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
                            if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_loc)) == 0) {  // Benutzer hat Start gedrückt
                                startStopBtn.setText(getResources().getString(R.string.stop_listening_btn_loc));
                                Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                                startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                                startButtonPressed = true;

                                MainActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterv, posDiff, this);
                                Location lastKnownLocation = MainActivity.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (lastKnownLocation != null) {
                                    double latitude = lastKnownLocation.getLatitude();
                                    double longitude = lastKnownLocation.getLongitude();
                                    double altitude = lastKnownLocation.getAltitude();

                                    if (tvLatGPS != null)
                                        tvLatGPS.setText(convertLatitude(latitude));
                                    if (tvLongGPS != null)
                                        tvLongGPS.setText(convertLongitude(longitude));
                                    if (tvAltGPS != null)
                                        tvAltGPS.setText(Double.toString(altitude));
                                }
                            } else {  // Benutzer hat Stop gedrückt
                                MainActivity.locationManager.removeUpdates(this);
                                startStopBtn.setText(getResources().getString(R.string.start_listening_btn_loc));
                                Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                                startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                            }

                        } else {
                            Toast.makeText(getActivity(), "Dein Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if(selectedItems.contains(1)) {  // NETWORK_PROVIDER vom LocationManager ausgewählt
                        if (MainActivity.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            if (timeIntervMs.getText().toString().equals("") || posChangeInM.getText().toString().equals("")) {
                                Toast.makeText(getActivity(), "Es werden die Eingaben benötigt!", Toast.LENGTH_SHORT).show();
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
                            if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_loc)) == 0) {  // Benutzer hat Start gedrückt
                                startStopBtn.setText(getResources().getString(R.string.stop_listening_btn_loc));
                                Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                                startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                                startButtonPressed = true;

                                MainActivity.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeInterv, posDiff, this);
                                Location lastKnownLocation = MainActivity.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                if (lastKnownLocation != null) {
                                    double latitude = lastKnownLocation.getLatitude();
                                    double longitude = lastKnownLocation.getLongitude();
                                    double altitude = lastKnownLocation.getAltitude();

                                    if (tvLatGPS != null)
                                        tvLatGPS.setText(convertLatitude(latitude));
                                    if (tvLongGPS != null)
                                        tvLongGPS.setText(convertLongitude(longitude));
                                    if (tvAltGPS != null)
                                        tvAltGPS.setText(Double.toString(altitude));
                                }
                            } else {  // Benutzer hat Stop gedrückt
                                MainActivity.locationManager.removeUpdates(this);
                                startStopBtn.setText(getResources().getString(R.string.start_listening_btn_loc));
                                Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                                startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                            }

                        } else {
                            Toast.makeText(getActivity(), "Dein Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if(selectedItems.contains(2)) {  //  FusedLocationProvider mit Priorität HIGH_ACCURACY ausgewählt
                        try {
                            startFusedLocationTracking(LocationRequest.PRIORITY_HIGH_ACCURACY, buttonText);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                    if(selectedItems.contains(3)) {  //  FusedLocationProvider mit Priorität BALANCED_POWER_ACCURACY ausgewählt
                        try {
                            startFusedLocationTracking(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, buttonText);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                    if(selectedItems.contains(4)) {  //  FusedLocationProvider mit Priorität LOW_POWER ausgewählt
                        try {
                            startFusedLocationTracking(LocationRequest.PRIORITY_LOW_POWER, buttonText);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                    if(selectedItems.contains(5)) {  //  FusedLocationProvider mit Priorität NO_POWER ausgewählt
                        try {
                            startFusedLocationTracking(LocationRequest.PRIORITY_NO_POWER, buttonText);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.chooseLocMethodsBtn:
                AlertDialog.Builder adBuilder = new AlertDialog.Builder(getActivity());
                adBuilder.setTitle(R.string.dialog_title);
                adBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked){
                            if(!selectedItems.contains(which)){
                                selectedItems.add(which);
                            }
                        }
                        else if(selectedItems.contains(which)){
                            selectedItems.remove((Integer) which);
                        }
                    }
                });
                adBuilder.setCancelable(false);
                adBuilder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(selectedItems.contains(0) || selectedItems.contains(1)){  // GPS_PROVIDER oder NETWORK_PROVIDER vom LocationManager ausgewählt, oder auch beides
                            if(timeIntervMs.getVisibility() == View.INVISIBLE || posChangeInM.getVisibility() == View.INVISIBLE){
                                timeIntervMs.setVisibility(View.VISIBLE);
                                posChangeInM.setVisibility(View.VISIBLE);
                            }
                        } else {
                            timeIntervMs.setVisibility(View.INVISIBLE);
                            posChangeInM.setVisibility(View.INVISIBLE);
                        }
                        if(selectedItems.contains(2) || selectedItems.contains(3) || selectedItems.contains(4) || selectedItems.contains(5)) {  // Eine oder mehrere Prioritäten des FusedLocationProviders sind aufgewählt worden
                            if(fastesTimeIntervMs.getVisibility() == View.INVISIBLE || timeIntervMs.getVisibility() == View.INVISIBLE){
                                timeIntervMs.setVisibility(View.VISIBLE);
                                fastesTimeIntervMs.setVisibility(View.VISIBLE);
                            }
                        } else {
                            fastesTimeIntervMs.setVisibility(View.INVISIBLE);
                        }
                    }
                });
//                adBuilder.setNegativeButton(R.string.dismiss_label, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        if(selectedItems.contains(0) || selectedItems.contains(1)){  // GPS_PROVIDER oder NETWORK_PROVIDER vom LocationManager ausgewählt, oder auch beides
//                            if(timeIntervMs.getVisibility() == View.INVISIBLE || posChangeInM.getVisibility() == View.INVISIBLE){
//                                timeIntervMs.setVisibility(View.VISIBLE);
//                                posChangeInM.setVisibility(View.VISIBLE);
//                            }
//                        } else {
//                            timeIntervMs.setVisibility(View.INVISIBLE);
//                            posChangeInM.setVisibility(View.INVISIBLE);
//                        }
//                        if(selectedItems.contains(2) || selectedItems.contains(3) || selectedItems.contains(4) || selectedItems.contains(5)) {  // Eine oder mehrere Prioritäten des FusedLocationProviders sind aufgewählt worden
//                            if(fastesTimeIntervMs.getVisibility() == View.INVISIBLE || timeIntervMs.getVisibility() == View.INVISIBLE){
//                                timeIntervMs.setVisibility(View.VISIBLE);
//                                fastesTimeIntervMs.setVisibility(View.VISIBLE);
//                            }
//                        } else {
//                            fastesTimeIntervMs.setVisibility(View.INVISIBLE);
//                        }
//                    }
//                });
                adBuilder.setNeutralButton(R.string.clear_all_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0;i < checkedItems.length; i++){
                            checkedItems[i] = false;
                            selectedItems.clear();
                        }
                        if(selectedItems.contains(0) || selectedItems.contains(1)){  // GPS_PROVIDER oder NETWORK_PROVIDER vom LocationManager ausgewählt, oder auch beides
                            if(timeIntervMs.getVisibility() == View.INVISIBLE || posChangeInM.getVisibility() == View.INVISIBLE){
                                timeIntervMs.setVisibility(View.VISIBLE);
                                posChangeInM.setVisibility(View.VISIBLE);
                            }
                        } else {
                            timeIntervMs.setVisibility(View.INVISIBLE);
                            posChangeInM.setVisibility(View.INVISIBLE);
                        }
                        if(selectedItems.contains(2) || selectedItems.contains(3) || selectedItems.contains(4) || selectedItems.contains(5)) {  // Eine oder mehrere Prioritäten des FusedLocationProviders sind aufgewählt worden
                            if(fastesTimeIntervMs.getVisibility() == View.INVISIBLE || timeIntervMs.getVisibility() == View.INVISIBLE){
                                timeIntervMs.setVisibility(View.VISIBLE);
                                fastesTimeIntervMs.setVisibility(View.VISIBLE);
                            }
                        } else {
                            fastesTimeIntervMs.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                AlertDialog ad = adBuilder.create();
                ad.show();
                break;
        }
    }

    private void startFusedLocationTracking(int locationRequestPriorityNr, String buttonText) throws NoSuchMethodException {
        LocationCallback callback = null;
        int priority = -1;
        LocationRequest newLocationRequest = new LocationRequest();
        switch(locationRequestPriorityNr){
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                locationRequestHighAcc = newLocationRequest;
                callback = mLocationCallbackHighAcc;
                break;
            case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                locationRequestBalanced = newLocationRequest;
                callback = mLocationCallbackBalanced;
                break;
            case LocationRequest.PRIORITY_LOW_POWER:
                priority = LocationRequest.PRIORITY_LOW_POWER;
                locationRequestLowPower = newLocationRequest;
                callback = mLocationCallbackLowPow;
                break;
            case LocationRequest.PRIORITY_NO_POWER:
                priority = LocationRequest.PRIORITY_NO_POWER;
                locationRequestNoPower = newLocationRequest;
                callback = mLocationCallbackNoPow;
                break;
        }
        if(priority == -1){
            Toast.makeText(getActivity(), "Fehler beim erkennen der LocationRequest Priority!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestFineLocationPermission();
        } else {
            long timeInterv, fastesTimeInterv;
            if (MainActivity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && MainActivity.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (timeIntervMs.getText().toString().equals("") || fastesTimeIntervMs.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Es werden die Eingaben benötigt!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    timeInterv = Long.parseLong(timeIntervMs.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Zeitintervall-Eingabe muss positiv und ganzzahlig sein!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    fastesTimeInterv = Long.parseLong(fastesTimeIntervMs.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Zeitintervall-Eingabe muss eine positive Zahl sein!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_loc)) == 0) {  // Benutzer hat Start gedrückt
                    startStopBtn.setText(getResources().getString(R.string.stop_listening_btn_loc));
                    Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                    startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    startButtonPressed = true;

                    newLocationRequest.setPriority(priority);
                    newLocationRequest.setInterval(timeInterv);
                    newLocationRequest.setFastestInterval(fastesTimeInterv);

                    LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(newLocationRequest, callback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                return;
                            }
                            switch(locationRequestPriorityNr){
                                case LocationRequest.PRIORITY_HIGH_ACCURACY:
                                    onLocationChangedHighAcc(locationResult.getLastLocation());
                                    break;
                                case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                                    onLocationChangedBalanced(locationResult.getLastLocation());
                                    break;
                                case LocationRequest.PRIORITY_LOW_POWER:
                                    onLocationChangedLowPow(locationResult.getLastLocation());
                                    break;
                                case LocationRequest.PRIORITY_NO_POWER:
                                    onLocationChangedNoPow(locationResult.getLastLocation());
                                    break;
                            }
                        }
                    }, Looper.myLooper());

                    switch(locationRequestPriorityNr){
                        case LocationRequest.PRIORITY_HIGH_ACCURACY:
                            mLocationCallbackHighAcc = callback;
                            break;
                        case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                            mLocationCallbackBalanced = callback;
                            break;
                        case LocationRequest.PRIORITY_LOW_POWER:
                            mLocationCallbackLowPow = callback;
                            break;
                        case LocationRequest.PRIORITY_NO_POWER:
                            mLocationCallbackNoPow = callback;
                            break;
                    }
                } else {  // Benutzer hat Stop gedrückt
                    LocationServices.getFusedLocationProviderClient(getActivity()).removeLocationUpdates(callback);
                    startStopBtn.setText(getResources().getString(R.string.start_listening_btn_loc));
                    Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                    startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                }

            } else {
                Toast.makeText(getActivity(), "Dein Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
                return;
            }
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
        Log.d("test", "\ninOnPause 1\n");
        MainActivity.locationManager.removeUpdates(this);
        Log.d("test", "\ninOnPause 2\n");
        if(mLocationCallbackHighAcc != null && mFusedLocationClient != null) {
            Log.d("test", "\ninOnPause 3\n");
            mFusedLocationClient.removeLocationUpdates(mLocationCallbackHighAcc);
            Log.d("test", "\ninOnPause 4\n");
            mLocationCallbackHighAcc = null;
        }
        if(mLocationCallbackBalanced != null && mFusedLocationClient != null) {
            Log.d("test", "\ninOnPause 5\n");
            mFusedLocationClient.removeLocationUpdates(mLocationCallbackBalanced);
            Log.d("test", "\ninOnPause 6\n");
            mLocationCallbackBalanced = null;
        }
        if(mLocationCallbackLowPow != null && mFusedLocationClient != null) {
            Log.d("test", "\ninOnPause 7\n");
            mFusedLocationClient.removeLocationUpdates(mLocationCallbackLowPow);
            Log.d("test", "\ninOnPause 8\n");
            mLocationCallbackLowPow = null;
        }
        if(mLocationCallbackNoPow != null && mFusedLocationClient != null){
            Log.d("test", "\ninOnPause 9\n");
            mFusedLocationClient.removeLocationUpdates(mLocationCallbackNoPow);
            Log.d("test", "\ninOnPause 10\n");
            mLocationCallbackNoPow = null;
        }

        String buttonText = startStopBtn.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn_loc)) == 0) {
            startStopBtn.setText(getResources().getString(R.string.start_listening_btn_loc));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("test", "\ninOnDestroy 1\n");
        MainActivity.locationManager.removeUpdates(this);
        String buttonText = startStopBtn.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn_loc)) == 0) {
            startStopBtn.setText(getResources().getString(R.string.start_listening_btn_loc));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    public void saveFile(String text, String fileName, boolean append)
    {
        FileOutputStream fos = null;

        try {
            if(append)
                fos = getActivity().openFileOutput(fileName,getActivity().MODE_APPEND);
            else
                fos = getActivity().openFileOutput(fileName,getActivity().MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();
            //Toast.makeText(getActivity(), "Gespeichert!", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
    public String getFileContent(String file)
    {
        String text = "";
        try {
            FileInputStream fis = getActivity().openFileInput(file);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            text = new String(buffer);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return text;
    }

    private void sendDataRest(String locMethod, double ... params){
        JSONObject locData = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try{
            locData.put("Latitude", params[0]);
            locData.put("Longitude", params[1]);
            locData.put("Hoehe", params[2]);
            locData.put("session_id", Session.getID());
            jsonArray.put(locData);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        new ConnectionRest().execute(locMethod,jsonArray.toString());
        Log.d("RESTAPI",locData.toString());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
}
