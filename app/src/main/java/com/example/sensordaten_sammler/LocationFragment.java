package com.example.sensordaten_sammler;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sensordaten_sammler.rest.ConnectionRest;
import com.example.sensordaten_sammler.rest.Data2ServerHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationFragment extends Fragment implements LocationListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    private static final int FINE_LOCATION_PERMISSION_CODE = 1;
    Button startStopBtn, choosedLocMethBtn, addWPIndoor, addWPOutdoor, svIndoorTS, svOutdoorTS;
    EditText timeIntervMs, posChangeInM, fastesTimeIntervMs, etRouteLabel, etDistThreshold, etMaxSpeed;
    TextView tvLatHighAcc, tvLongHighAcc, tvAltHighAcc, tvSpeedHighAcc, tvAccHighAcc
            , tvLatBalanced, tvLongBalanced, tvAltBalanced, tvSpeedBalanced, tvAccBalanced
            ,tvLatLowPow, tvLongLowPow, tvAltLowPow, tvSpeedLowPow, tvAccLowPow
            ,tvLatNoPow, tvLongNoPow, tvAltNoPow, tvSpeedNoPow, tvAccNoPow
            ,tvLatGPS, tvLongGPS, tvAltGPS, tvSpeedGPS, tvAccGPS
            ,tvLatNetwork, tvLongNetwork, tvAltNetwork, tvSpeedNetwork, tvAccNetwork;
    String fileNameGPS = "GPSFile", fileNameNetwork = "NetworkFile"
            , fileNameHighAcc = "HighAccFile", fileNameBalanced = "BalancedFile", fileNameLowPow = "LowPowFile", fileNameNoPow = "NoPowFile"
            , fileNameGPSComplete = "GPSFile", fileNameNetworkComplete = "NetworkFile"
            , fileNameHighAccComplete = "HighAccFile", fileNameBalancedComplete = "BalancedFile", fileNameLowPowComplete = "LowPowFile", fileNameNoPowComplete = "NoPowFile";
    String GTWPSwithTSFileName;
    CheckBox csv, checkboxUseAccelerometer;
    String[] listItems;
    boolean[] checkedItems;
    ArrayList<Integer> selectedItems;
    //private boolean startButtonPressed = false;
    double latitudeGPS, latitudeNetwork, latitudeHighAcc, latitudeBalanced, latitudeLowPow, latitudeNoPow;
    double longitudeGPS, longitudeNetwork, longitudeHighAcc, longitudeBalanced, longitudeLowPow, longitudeNoPow;
    double altitudeGPS, altitudeNetwork, altitudeHighAcc, altitudeBalanced, altitudeLowPow, altitudeNoPow;
    float speedGPS, speedNetwork, speedHighAcc, speedBalanced, speedLowPow, speedNoPow, accuracyGPS, accuracyHighAcc, accuracyBalanced, accuracyLowPow, accuracyNoPow, accuracyNetwork;
    public LocationRequest locationRequestHighAcc, locationRequestBalanced, locationRequestLowPower, locationRequestNoPower;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallbackHighAcc, mLocationCallbackBalanced, mLocationCallbackLowPow, mLocationCallbackNoPow;
    GoogleApiClient mGoogleApiClient;
    public Location lastLocationGPS;
    float distThresholdInM, maxSpeed;
    List<Double> accAbsolutesList;
    public boolean requestingLocationUpdates;
    Spinner spinnerRoute;

    private Data2ServerHelper networkStateReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        startStopBtn = view.findViewById(R.id.bStartStopLoc);
        choosedLocMethBtn = view.findViewById(R.id.chooseLocMethodsBtn);
        svIndoorTS = view.findViewById(R.id.saveIndoorTimestamp);
        svOutdoorTS = view.findViewById(R.id.saveOutdoorTimestamp);
        addWPIndoor = view.findViewById(R.id.btnAddWPIndoor);
        addWPOutdoor = view.findViewById(R.id.btnAddWPOutdoor);
        startStopBtn.setOnClickListener(this);
        choosedLocMethBtn.setOnClickListener(this);
        svIndoorTS.setOnClickListener(this);
        svOutdoorTS.setOnClickListener(this);
        addWPIndoor.setOnClickListener(this);
        addWPOutdoor.setOnClickListener(this);
        spinnerRoute = view.findViewById(R.id.spRoutenTemp);
        csv = view.findViewById(R.id.csvBoxLoc);
        checkboxUseAccelerometer = view.findViewById(R.id.checkBoxAccele);
        csv.setEnabled(true);
        checkboxUseAccelerometer.setEnabled(false);
        selectedItems = new ArrayList<>();
        listItems = getResources().getStringArray(R.array.loc_methods_items);
        checkedItems = new boolean[listItems.length];
        accAbsolutesList = new LinkedList<>();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTVs(view);
        initSpRoutes();
        timeIntervMs = view.findViewById(R.id.minIntervallTimeLoc);
        posChangeInM = view.findViewById(R.id.minPosChangeLocMan);
        fastesTimeIntervMs = view.findViewById(R.id.fastesIntervallTimeLoc);
        etRouteLabel = view.findViewById(R.id.etRouteLabel);
        etDistThreshold = view.findViewById(R.id.distThreshold);
        etMaxSpeed = view.findViewById(R.id.maxSpeed);
        timeIntervMs.setVisibility(View.INVISIBLE);
        posChangeInM.setVisibility(View.INVISIBLE);
        fastesTimeIntervMs.setVisibility(View.INVISIBLE);
        etDistThreshold.setVisibility(View.INVISIBLE);
        etMaxSpeed.setVisibility(View.INVISIBLE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        etDistThreshold.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0 && etMaxSpeed.getText().toString().equals(""))
                    checkboxUseAccelerometer.setEnabled(true);
                else
                    checkboxUseAccelerometer.setEnabled(false);
            }
        });
        etMaxSpeed.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0)
                    checkboxUseAccelerometer.setEnabled(false);
                else if(!etDistThreshold.getText().toString().equals(""))
                    checkboxUseAccelerometer.setEnabled(true);
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
    public void onLocationChanged(Location newestLocation) {
        if (newestLocation != null) {
            if(newestLocation.getProvider().equalsIgnoreCase("gps")){
                this.latitudeGPS = newestLocation.getLatitude();
                this.longitudeGPS = newestLocation.getLongitude();
                this.altitudeGPS = newestLocation.getAltitude();
                this.speedGPS = newestLocation.getSpeed();
                this.accuracyGPS = newestLocation.getAccuracy();
                // Nur Distanzschwellwert angegeben:
                if(!etDistThreshold.getText().toString().equalsIgnoreCase("") && etMaxSpeed.getText().toString().equalsIgnoreCase("")
                        && (lastLocationGPS == null || lastLocationGPS.distanceTo(newestLocation) > distThresholdInM)){
                    this.lastLocationGPS = newestLocation;
                    sendGPSDataRest(latitudeGPS, longitudeGPS, altitudeGPS, speedGPS, accuracyGPS);
                    Toast.makeText(getActivity(), "Positionsfix an Server gesendet!", Toast.LENGTH_SHORT).show();
                }
                // Distanzschwellwert und Maximalgeschwindigkeit angegeben:
                else if(!etDistThreshold.getText().toString().equalsIgnoreCase("") && !etMaxSpeed.getText().toString().equalsIgnoreCase("")){
                    sendGPSDataRest(latitudeGPS, longitudeGPS, altitudeGPS, speedGPS, accuracyGPS);
                    Toast.makeText(getActivity(), "LocationUpdate empfangen!", Toast.LENGTH_SHORT).show();
                } else if(!timeIntervMs.getText().toString().equalsIgnoreCase("") && !posChangeInM.getText().toString().equalsIgnoreCase("")){
                    sendGPSDataRest(latitudeGPS, longitudeGPS, altitudeGPS, speedGPS, accuracyGPS);
                }
                Activity activity = getActivity();
                String lat = convertLatitude(latitudeGPS);
                String lon = convertLongitude(longitudeGPS);
                if (isAdded() && activity != null) {
                    tvLatGPS.setText(lat);
                    tvLongGPS.setText(lon);
                    tvAltGPS.setText(String.format(Locale.GERMAN, "%f",altitudeGPS));
                    tvSpeedGPS.setText(String.format(Locale.GERMAN, "%f",speedGPS));
                    if(csv.isChecked()) {
                        saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeGPS + "," + speedGPS + "," + accuracyGPS + "\n", fileNameGPSComplete, true);
                    }
                }
            }
            else if(newestLocation.getProvider().equalsIgnoreCase("network")){
                this.latitudeNetwork = newestLocation.getLatitude();
                this.longitudeNetwork = newestLocation.getLongitude();
                this.altitudeNetwork = newestLocation.getAltitude();
                this.speedNetwork = newestLocation.getSpeed();
                this.accuracyNetwork = newestLocation.getAccuracy();
                Activity activity = getActivity();
                String lat = convertLatitude(latitudeNetwork);
                String lon = convertLongitude(longitudeNetwork);
                if (isAdded() && activity != null) {
                    tvLatNetwork.setText(lat);
                    tvLongNetwork.setText(lon);
                    tvAltNetwork.setText(String.format(Locale.GERMAN, "%f",altitudeNetwork));
                    tvSpeedNetwork.setText(String.format(Locale.GERMAN, "%f",speedNetwork));
                    if(csv.isChecked()) {
                        saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeNetwork + "," + speedNetwork + "," + accuracyNetwork + "\n", fileNameNetworkComplete, true);
                    }
                }
            }
        }
    }

    private void onLocationChangedHighAcc(Location location) {
        Log.e("TEST", "8");
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
            tvAltHighAcc .setText(String.format(Locale.GERMAN, "%f",altitudeHighAcc ));
            tvSpeedHighAcc .setText(String.format(Locale.GERMAN, "%f",speedHighAcc ));
            tvAccHighAcc .setText(String.format(Locale.GERMAN, "%f",accuracyHighAcc ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeHighAcc + "," + speedHighAcc + "," + accuracyHighAcc + "\n", fileNameHighAccComplete, true);
            }
        }
    }

    private void onLocationChangedBalanced(Location location) {
        Log.e("TEST", "7");
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
            tvAltBalanced .setText(String.format(Locale.GERMAN, "%f",altitudeBalanced ));
            tvSpeedBalanced .setText(String.format(Locale.GERMAN, "%f",speedBalanced ));
            tvAccBalanced .setText(String.format(Locale.GERMAN, "%f",accuracyBalanced ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeBalanced + "," + speedBalanced + "," + accuracyBalanced + "\n", fileNameBalancedComplete, true);
            }
        }
    }

    private void onLocationChangedLowPow(Location location) {
        Log.e("TEST", "6");
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
            tvAltLowPow .setText(String.format(Locale.GERMAN, "%f",altitudeLowPow ));
            tvSpeedLowPow .setText(String.format(Locale.GERMAN, "%f",speedLowPow ));
            tvAccLowPow .setText(String.format(Locale.GERMAN, "%f",accuracyLowPow ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeLowPow + "," + speedLowPow + "," + accuracyLowPow + "\n", fileNameLowPowComplete, true);
            }
        }
    }

    private void onLocationChangedNoPow(Location location) {
        Log.e("TEST", "5");
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
            tvAltNoPow .setText(String.format(Locale.GERMAN, "%f",altitudeNoPow ));
            tvSpeedNoPow .setText(String.format(Locale.GERMAN, "%f",speedNoPow ));
            tvAccNoPow .setText(String.format(Locale.GERMAN, "%f",accuracyNoPow ));
            if(csv.isChecked()) {
                saveFile(System.currentTimeMillis() + "," + lat + "," + lon + "," + altitudeNoPow + "," + speedNoPow + "," + accuracyNoPow + "\n", fileNameNoPowComplete, true);
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(getActivity()).setTitle("Erlaubnis benötigt").setMessage("Zum Anzeigen der GPS-Daten, wird deine Erlaubnis benötigt")
                    .setPositiveButton("ok", (dialog, which)->
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE))
                    .setNegativeButton("cancel", (dialog, which)->dialog.dismiss())
                    .create().show();
        } else
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopLoc:
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestFineLocationPermission();
                } else {
                    if(selectedItems.isEmpty()){
                        Toast.makeText(requireActivity(), "Du hast kein Positionierungsverfahren ausgewählt!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long timeInterv = 0/*, fastesTimeInterv*/;
                    float posDiff = 0;
                    String buttonText = startStopBtn.getText().toString();
                    if(selectedItems.contains(0)) {  // GPS_PROVIDER vom LocationManager ausgewählt
                        if (MainActivity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_loc)) == 0) {  // Benutzer hat Start gedrückt
                                if (timeIntervMs.getText().toString().equals("") && posChangeInM.getText().toString().equals("")
                                        && etRouteLabel.getText().toString().equals("") && etDistThreshold.getText().toString().equals("")&& etMaxSpeed.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "Es werden Eingaben benötigt!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if ((!timeIntervMs.getText().toString().equals("") || !posChangeInM.getText().toString().equals(""))
                                        && (!etDistThreshold.getText().toString().equals("") || !etMaxSpeed.getText().toString().equals(""))) {
                                    Toast.makeText(getActivity(), "Bitte mache entweder die Eingaben Positionsdifferenz + Zeitintervall " +
                                            "ODER Distanzschwelle ODER Distanzschwelle + Maximalgeschwindigkeit!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (!etMaxSpeed.getText().toString().equals("") && etDistThreshold.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "Wenn Du eine Maximalgeschwindigkeit angegeben hast, benötigst Du auch eine Distanzschwelle!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (etRouteLabel.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "Du benötigst einen Namen für deine Route!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if(etDistThreshold.getText().toString().equals("") && etMaxSpeed.getText().toString().equals("")){
                                    try {
                                        timeInterv = Long.parseLong(timeIntervMs.getText().toString());
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(getActivity(), "Zeitintervall-Eingabe muss positiv und ganzzahlig sein!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                if(etDistThreshold.getText().toString().equals("")&& etMaxSpeed.getText().toString().equals("")){
                                    try {
                                        posDiff = Float.parseFloat(posChangeInM.getText().toString());
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(getActivity(), "Der Positionsunterschied muss eine positive Zahl sein!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                if(!etDistThreshold.getText().toString().equals("")){
                                    try {
                                        distThresholdInM = Float.parseFloat(etDistThreshold.getText().toString());
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(getActivity(), "Fehler bei Konvertierung der Distanzschwelle!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                if(!etMaxSpeed.getText().toString().equals("")){
                                    try {
                                        maxSpeed = Float.parseFloat(etMaxSpeed.getText().toString());
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(getActivity(), "Fehler bei Konvertierung der Maximalgeschwindigkeit!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                fileNameGPSComplete = etRouteLabel.getText().toString() + fileNameGPS +Session.getID()+ ".csv";
                                GTWPSwithTSFileName = etRouteLabel.getText().toString()+ Session.getID() + "GTWPSwithTS" + ".csv";
                                if(MainActivity.fileExists(requireActivity(), fileNameGPSComplete) || MainActivity.fileExists(requireActivity(), GTWPSwithTSFileName)){
                                    Toast.makeText(requireActivity(), "Der Routenname existiert bereits, bitte ändere die Bezeichnung der Route", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ",Genauigkeit" + "\n", fileNameGPSComplete, false);
                                startStopBtn.setText(getResources().getString(R.string.stop_listening_btn_loc));
                                Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_stop, null);
                                startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                                //startButtonPressed = true;
                                // Distanzschwellwert angegeben:
                                if(!etDistThreshold.getText().toString().equalsIgnoreCase("") && etMaxSpeed.getText().toString().equalsIgnoreCase("")){
                                    Log.e("TESTT", "Distanzschwellwert angegeben");
                                    MainActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                                    requestingLocationUpdates = true;
                                    if(checkboxUseAccelerometer.isChecked() && (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)){
                                        Log.e("TESTT", "checkboxUseAccelerometer.isSelected() && (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null");
                                        Sensor sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, 400000);
                                    }
                                }
                                // Distanzschwellwert und Maximalgeschwindigkeit angegeben:
                                else if(!etDistThreshold.getText().toString().equalsIgnoreCase("") && !etMaxSpeed.getText().toString().equalsIgnoreCase("")){
//                                    long calcTimeInterv;
//                                    float calcMinDist;
                                    MainActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) (distThresholdInM / maxSpeed) * 1000, distThresholdInM, this);
                                }
                                else{
                                    MainActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterv, posDiff, this);
                                }
//                                timeIntervMs.setEnable(false);
                                timeIntervMs.setClickable(false);
//                                posChangeInM.setEnable(false);
                                posChangeInM.setClickable(false);
//                                etDistThreshold.setEnable(false);
                                etDistThreshold.setClickable(false);
//                                etMaxSpeed.setEnable(false);
                                etMaxSpeed.setClickable(false);
//                                etRouteLabel.setEnable(false);
                                etRouteLabel.setClickable(false);

//                                Location lastKnownLocation = MainActivity.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                                if (lastKnownLocation != null) {
//                                    double latitude = lastKnownLocation.getLatitude();
//                                    double longitude = lastKnownLocation.getLongitude();
//                                    double altitude = lastKnownLocation.getAltitude();
//
//                                    if (tvLatGPS != null)
//                                        tvLatGPS.setText(convertLatitude(latitude));
//                                    if (tvLongGPS != null)
//                                        tvLongGPS.setText(convertLongitude(longitude));
//                                    if (tvAltGPS != null)
//                                        tvAltGPS.setText(Double.toString(altitude));
//                                }
                            }

                        } else {
                            Toast.makeText(getActivity(), "Dein Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if(selectedItems.contains(1)) {  // NETWORK_PROVIDER vom LocationManager ausgewählt
                        if (MainActivity.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_loc)) == 0) {  // Benutzer hat Start gedrückt
                                if (timeIntervMs.getText().toString().equals("") || posChangeInM.getText().toString().equals("") || etRouteLabel.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "Es werden alle Eingaben benötigt!", Toast.LENGTH_SHORT).show();
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
                                fileNameNetworkComplete = etRouteLabel.getText().toString() + fileNameNetwork + ".csv";
                                GTWPSwithTSFileName = etRouteLabel.getText().toString() + "GTWPSwithTS" + ".csv";
                                if(MainActivity.fileExists(requireActivity(), fileNameNetworkComplete) || MainActivity.fileExists(requireActivity(), GTWPSwithTSFileName)){
                                    Toast.makeText(getActivity(), "Der Routenname existiert bereits, bitte ändere die Bezeichnung der Route", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ",Genauigkeit" + "\n", fileNameNetworkComplete, false);
                                startStopBtn.setText(getResources().getString(R.string.stop_listening_btn_loc));
                                Drawable img = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_stop,null);
                                startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                                //startButtonPressed = true;

                                MainActivity.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, timeInterv, posDiff, this);
//                                Location lastKnownLocation = MainActivity.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                                if (lastKnownLocation != null) {
//                                    double latitude = lastKnownLocation.getLatitude();
//                                    double longitude = lastKnownLocation.getLongitude();
//                                    double altitude = lastKnownLocation.getAltitude();
//
//                                    if (tvLatNetwork != null)
//                                        tvLatNetwork.setText(convertLatitude(latitude));
//                                    if (tvLongNetwork != null)
//                                        tvLongNetwork.setText(convertLongitude(longitude));
//                                    if (tvAltNetwork != null)
//                                        tvAltNetwork.setText(Double.toString(altitude));
//                                }
                            }

                        } else {
                            Toast.makeText(getActivity(), "Dein Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if(selectedItems.contains(2)) {  //  FusedLocationProvider mit Priorität HIGH_ACCURACY ausgewählt

                            boolean successfullyStarted = startFusedLocationTracking(LocationRequest.PRIORITY_HIGH_ACCURACY, buttonText);
                            if(!successfullyStarted)
                                return;
                    }
                    if(selectedItems.contains(3)) {  //  FusedLocationProvider mit Priorität BALANCED_POWER_ACCURACY ausgewählt

                            boolean successfullyStarted = startFusedLocationTracking(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, buttonText);
                            if(!successfullyStarted)
                                return;

                    }
                    if(selectedItems.contains(4)) {  //  FusedLocationProvider mit Priorität LOW_POWER ausgewählt

                            boolean successfullyStarted = startFusedLocationTracking(LocationRequest.PRIORITY_LOW_POWER, buttonText);
                            if(!successfullyStarted)
                                return;

                    }
                    if(selectedItems.contains(5)) {  //  FusedLocationProvider mit Priorität NO_POWER ausgewählt

                            boolean successfullyStarted = startFusedLocationTracking(LocationRequest.PRIORITY_NO_POWER, buttonText);
                            if(!successfullyStarted)
                                return;

                    }
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_loc)) == 0) {  // Benutzer hat Start gedrückt
                        startStopBtn.setText(getResources().getString(R.string.stop_listening_btn_loc));
                        Drawable img = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_stop,null);
                        startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        //startButtonPressed = true;
                        accAbsolutesList.clear();
                        createRouteVersuch();
                        if(csv.isChecked()){
                            GTWPSwithTSFileName = etRouteLabel.getText().toString() + "GTWPSwithTS" + ".csv";
                            saveFile("WP-Nr" + "," + "Zeit" + "," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + "\n", GTWPSwithTSFileName, false);
                        }
                    }
                    else {  // Benutzer hat Stop gedrückt
                        Log.e("TEST", "21");
                        MainActivity.locationManager.removeUpdates(this);
                        MainActivity.sensorManager.unregisterListener(this);
                        if(mLocationCallbackHighAcc != null) {
                            Log.e("TEST", "22");
                            LocationServices.getFusedLocationProviderClient(requireActivity()).removeLocationUpdates(mLocationCallbackHighAcc);
                        }
                        if(mLocationCallbackBalanced != null) {
                            Log.e("TEST", "23");
                            LocationServices.getFusedLocationProviderClient(requireActivity()).removeLocationUpdates(mLocationCallbackBalanced);
                        }
                        if(mLocationCallbackLowPow != null)
                         LocationServices.getFusedLocationProviderClient(requireActivity()).removeLocationUpdates(mLocationCallbackLowPow);
                        if(mLocationCallbackNoPow != null)
                            LocationServices.getFusedLocationProviderClient(requireActivity()).removeLocationUpdates(mLocationCallbackNoPow);
                        startStopBtn.setText(getResources().getString(R.string.start_listening_btn_loc));
                        Drawable img = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_arrow, null);
                        startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        //                                timeIntervMs.setEnable(false);
                        timeIntervMs.setClickable(true);
//                                posChangeInM.setEnable(false);
                        posChangeInM.setClickable(true);
//                                etDistThreshold.setEnable(false);
                        etDistThreshold.setClickable(true);
//                                etMaxSpeed.setEnable(false);
                        etMaxSpeed.setClickable(true);
//                                etRouteLabel.setEnable(false);
                        etRouteLabel.setClickable(true);
                    }
                }
                break;
            case R.id.chooseLocMethodsBtn:
                AlertDialog.Builder adBuilder = new AlertDialog.Builder(getActivity());
                adBuilder.setTitle(R.string.dialog_title);
                adBuilder.setMultiChoiceItems(listItems, checkedItems, (dialog, which, isChecked)-> {
                    if(isChecked){
                        if(!selectedItems.contains(which)){
                            selectedItems.add(which);
                        }
                    }
                    else if(selectedItems.contains(which)){
                        selectedItems.remove((Integer) which);
                    }
                });
                adBuilder.setCancelable(false);
                adBuilder.setPositiveButton(R.string.ok_label, (dialog, which)->{
                    dialog.dismiss();
                    if(selectedItems.contains(0) || selectedItems.contains(1)){  // GPS_PROVIDER oder NETWORK_PROVIDER vom LocationManager ausgewählt, oder auch beides
                        if(timeIntervMs.getVisibility() == View.INVISIBLE || posChangeInM.getVisibility() == View.INVISIBLE){
                            timeIntervMs.setVisibility(View.VISIBLE);
                            posChangeInM.setVisibility(View.VISIBLE);
                            etDistThreshold.setVisibility(View.VISIBLE);
                            etMaxSpeed.setVisibility(View.VISIBLE);
                        }
                    } else {
                        timeIntervMs.setVisibility(View.INVISIBLE);
                        posChangeInM.setVisibility(View.INVISIBLE);
                        etDistThreshold.setVisibility(View.INVISIBLE);
                        etMaxSpeed.setVisibility(View.INVISIBLE);
                    }
                    if(selectedItems.contains(2) || selectedItems.contains(3) || selectedItems.contains(4) || selectedItems.contains(5)) {  // Eine oder mehrere Prioritäten des FusedLocationProviders sind aufgewählt worden
                        if(fastesTimeIntervMs.getVisibility() == View.INVISIBLE || timeIntervMs.getVisibility() == View.INVISIBLE){
                            timeIntervMs.setVisibility(View.VISIBLE);
                            fastesTimeIntervMs.setVisibility(View.VISIBLE);
                        }
                    } else {
                        fastesTimeIntervMs.setVisibility(View.INVISIBLE);
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
                adBuilder.setNeutralButton(R.string.clear_all_label, (dialog, which)->{
                    for(int i = 0;i < checkedItems.length; i++){
                        checkedItems[i] = false;
                        selectedItems.clear();
                    }
                    if(selectedItems.contains(0) || selectedItems.contains(1)){  // GPS_PROVIDER oder NETWORK_PROVIDER vom LocationManager ausgewählt, oder auch beides
                        if(timeIntervMs.getVisibility() == View.INVISIBLE || posChangeInM.getVisibility() == View.INVISIBLE){
                            timeIntervMs.setVisibility(View.VISIBLE);
                            posChangeInM.setVisibility(View.VISIBLE);
                            etDistThreshold.setVisibility(View.VISIBLE);
                            etMaxSpeed.setVisibility(View.VISIBLE);
                        }
                    } else {
                        timeIntervMs.setVisibility(View.INVISIBLE);
                        posChangeInM.setVisibility(View.INVISIBLE);
                        etDistThreshold.setVisibility(View.INVISIBLE);
                        etMaxSpeed.setVisibility(View.INVISIBLE);
                    }
                    if(selectedItems.contains(2) || selectedItems.contains(3) || selectedItems.contains(4) || selectedItems.contains(5)) {  // Eine oder mehrere Prioritäten des FusedLocationProviders sind aufgewählt worden
                        if(fastesTimeIntervMs.getVisibility() == View.INVISIBLE || timeIntervMs.getVisibility() == View.INVISIBLE){
                            timeIntervMs.setVisibility(View.VISIBLE);
                            fastesTimeIntervMs.setVisibility(View.VISIBLE);
                        }
                    } else {
                        fastesTimeIntervMs.setVisibility(View.INVISIBLE);
                    }
                });
                AlertDialog ad = adBuilder.create();
                ad.show();
                break;

            case R.id.btnAddWPIndoor:
                addNewWaypointToIndoorRoute();
                break;

            case R.id.btnAddWPOutdoor:
                addNewWaypointToOutdoorRoute();
                break;

            case R.id.saveIndoorTimestamp:
                TableLayout indoorRouteTable = requireActivity().findViewById(R.id.routeIndoors);
                TableRow row = null;
                for(int i = 1; i < indoorRouteTable.getChildCount(); i++){ // Starte bei zweiten Zeile (Index 1), da sich in der ersten Zeile nur die Spaltenüberschriften befinden
                    row = (TableRow) indoorRouteTable.getChildAt(i);
                    TextView tvTimestamp = (TextView) row.getChildAt(4);
                    if(tvTimestamp.getText().equals(getString(R.string.loc_data_empty))){
                        tvTimestamp.setText(String.format(Locale.GERMAN, "%d",System.currentTimeMillis()));
                        break;
                    }
                }
                if(row != null) {
                    TextView tvWPNr = (TextView) row.getChildAt(0);
                    EditText etLat = (EditText) row.getChildAt(1);
                    EditText etLong = (EditText) row.getChildAt(2);
                    EditText etAlt = (EditText) row.getChildAt(3);
                    Long timestamp = System.currentTimeMillis();
                    if (csv.isChecked()) {
                        saveFile(tvWPNr.getText().toString() + "," + timestamp + "," + etLat.getText().toString() + "," + etLong.getText().toString() + "," + etAlt.getText().toString() + "\n", GTWPSwithTSFileName, true);
                    }
                    //send timestamp to server
                    JSONObject obj = new JSONObject();
                    try{
                        obj.put("latitude", Double.parseDouble(etLat.getText().toString()));
                        obj.put("longitude", Double.parseDouble(etLong.getText().toString()));
                        obj.put("altitude", Double.parseDouble(etAlt.getText().toString()));
                        obj.put("messwerteroute_name", etRouteLabel.getText().toString());
                        obj.put("timestamp", Long.toString(timestamp));
                        obj.put("session_id", Session.getID());
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    new ConnectionRest().execute("messwerte",obj.toString());
                }
                break;

            case R.id.saveOutdoorTimestamp:
                TableLayout outdoorRouteTable = requireActivity().findViewById(R.id.routeOutdoor);
                TableRow rowOutdoors = null;
                for(int i = 1; i < outdoorRouteTable.getChildCount(); i++){ // Starte bei zweiten Zeile (Index 1), da sich in der ersten Zeile nur die Spaltenüberschriften befinden
                    rowOutdoors = (TableRow) outdoorRouteTable.getChildAt(i);
                    TextView tvTimestamp = (TextView) rowOutdoors.getChildAt(4);
                    if(tvTimestamp.getText().equals(getString(R.string.loc_data_empty))){
                        tvTimestamp.setText(String.format(Locale.getDefault(), "%d",System.currentTimeMillis()));
                        break;
                    }
                }
                if(rowOutdoors!=null) {
                    TextView tvWPNrOutdoors = (TextView) rowOutdoors.getChildAt(0);
                    EditText etLatOutdoors = (EditText) rowOutdoors.getChildAt(1);
                    EditText etLongOutdoors = (EditText) rowOutdoors.getChildAt(2);
                    EditText etAltOutdoors = (EditText) rowOutdoors.getChildAt(3);
                    if(csv.isChecked()) {
                        saveFile( tvWPNrOutdoors.getText().toString() + "," + System.currentTimeMillis() + "," + etLatOutdoors.getText().toString() + "," + etLongOutdoors.getText().toString() + "," + etAltOutdoors.getText().toString() + "\n", GTWPSwithTSFileName, true);
                    }
                }
                break;
        }
    }

    private void addNewWaypointToIndoorRoute(){
        // Neue Zeile zur Tabelle hinzugügen ( = neuer Waypoint zur Indoor-Route)
        TableLayout indoorRouteTable = requireActivity().findViewById(R.id.routeIndoors);
        TableRow newIndoorWPT = new TableRow(getActivity());
        int newWPIDIndoor = indoorRouteTable.getChildCount(); // WPID = Nummer des Waypoints; 1 = A, 2 = B, usw.

        TextView tvNewWPTIDIndoor = new TextView(getActivity()), tvTimestampIndoor = new TextView(getActivity());
        tvNewWPTIDIndoor.setTextSize(12);
        tvNewWPTIDIndoor.setGravity(Gravity.START);
        tvNewWPTIDIndoor.setPadding(2,2,2,2);
        tvNewWPTIDIndoor.setText(String.format(Locale.GERMAN, "%d",newWPIDIndoor));
        tvTimestampIndoor.setText(getString(R.string.loc_data_empty));
        tvTimestampIndoor.setTextSize(12);
        tvTimestampIndoor.setGravity(Gravity.START);
        tvTimestampIndoor.setPadding(2,2,2,2);

        Button bIndoor = new Button(getActivity());
        bIndoor.setOnClickListener(getRmWPTButtonListener());
        bIndoor.setText(getString(R.string.btn_rm_wpt));
        bIndoor.setTextSize(12);
        bIndoor.setMinWidth(Conversion.dpToPx(2, requireActivity()));
        bIndoor.setMinimumWidth(Conversion.dpToPx(2, requireActivity()));
        bIndoor.setMinHeight(Conversion.dpToPx(2, requireActivity()));
        bIndoor.setMinimumHeight(Conversion.dpToPx(2, requireActivity()));
                /*
                EditTexts ermöglichen die Eingabe der Positionsdaten,
                die Daten einer Beispielroute sind aber schon unterlegt (habe ich selbst erstellt, könnten wir auch so nutzen)
                 */
        EditText etLatIndoor = new EditText(requireActivity()), etLongIndoor = new EditText(requireActivity()), etAltIndoor = new EditText(requireActivity());
        if(newWPIDIndoor < 9){
            String wpLatResTextIndoor = "wp" + newWPIDIndoor + "_lat_ind";
            etLatIndoor.setText(getString(getResources().getIdentifier(wpLatResTextIndoor, "string", requireActivity().getPackageName())));
        }
        etLatIndoor.setKeyListener(DigitsKeyListener.getInstance("0123456789.NESW° "));
        etLatIndoor.setTextSize(12);
        etLatIndoor.setGravity(Gravity.START);
        etLatIndoor.setPadding(2,2,2,2);

        if(newWPIDIndoor < 9){
            String wpLongResTextIndoor = "wp" + newWPIDIndoor + "_long_ind";
            etLongIndoor.setText(getString(getResources().getIdentifier(wpLongResTextIndoor, "string", requireActivity().getPackageName())));
        }
        etLongIndoor.setKeyListener(DigitsKeyListener.getInstance("0123456789.NESW° "));
        etLongIndoor.setTextSize(12);
        etLongIndoor.setGravity(Gravity.START);
        etLongIndoor.setPadding(2,2,2,2);

        if(newWPIDIndoor < 9){
            String wpAltResTextIndoor = "wp" + newWPIDIndoor + "_alt_ind";
            etAltIndoor.setText(getString(getResources().getIdentifier(wpAltResTextIndoor, "string", requireActivity().getPackageName())));
        }
        etAltIndoor.setKeyListener(DigitsKeyListener.getInstance("0123456789.NESW° "));
        etAltIndoor.setTextSize(12);
        etAltIndoor.setGravity(Gravity.START);
        etAltIndoor.setPadding(2,2,2,2);

        newIndoorWPT.addView(tvNewWPTIDIndoor);
        newIndoorWPT.addView(etLatIndoor);
        newIndoorWPT.addView(etLongIndoor);
        newIndoorWPT.addView(etAltIndoor);
        newIndoorWPT.addView(tvTimestampIndoor);
        newIndoorWPT.addView(bIndoor);
        indoorRouteTable.addView(newIndoorWPT);
    }

    private void addNewWaypointToOutdoorRoute(){
        // Neue Zeile zur Tabelle hinzugügen ( = neuer Waypoint zur Outdoor-Route)
        TableLayout outdoorRouteTable = requireActivity().findViewById(R.id.routeOutdoor);
        TableRow newOutdoorWPT = new TableRow(getActivity());
        int newWPIDOutdoor = outdoorRouteTable.getChildCount(); // WPID = Nummer des Waypoints; 1 = A, 2 = B, usw.

        TextView tvNewWPTIDOutdoor = new TextView(getActivity()), tvTimestampOutdoor = new TextView(getActivity());
        tvNewWPTIDOutdoor.setTextSize(12);
        tvNewWPTIDOutdoor.setGravity(Gravity.START);
        tvNewWPTIDOutdoor.setPadding(2,2,2,2);
        tvNewWPTIDOutdoor.setText(String.format(Locale.GERMAN, "%d",newWPIDOutdoor));
        tvTimestampOutdoor.setText(getString(R.string.loc_data_empty));
        tvTimestampOutdoor.setTextSize(12);
        tvTimestampOutdoor.setGravity(Gravity.START);
        tvTimestampOutdoor.setPadding(2,2,2,2);

        Button bOutdoor = new Button(getActivity());
        bOutdoor.setOnClickListener(getRmWPTButtonListener());
        bOutdoor.setText(getString(R.string.btn_rm_wpt));
        bOutdoor.setTextSize(12);
        bOutdoor.setMinWidth(Conversion.dpToPx(2, requireActivity()));
        bOutdoor.setMinimumWidth(Conversion.dpToPx(2, requireActivity()));
        bOutdoor.setMinHeight(Conversion.dpToPx(2, requireActivity()));
        bOutdoor.setMinimumHeight(Conversion.dpToPx(2, requireActivity()));

                /*
                EditTexts ermöglichen die Eingabe der Positionsdaten,
                die Daten einer Beispielroute sind aber schon unterlegt (habe ich selbst erstellt, könnten wir auch so nutzen)
                 */
        EditText etLatOutdoor = new EditText(getActivity()), etLongOutdoor = new EditText(getActivity()), etAltOutdoor = new EditText(getActivity());
        if(newWPIDOutdoor < 9){
            String wpLatResTextOutdoor = "wp" + newWPIDOutdoor + "_lat_out";
            etLatOutdoor.setText(getString(getResources().getIdentifier(wpLatResTextOutdoor, "string", requireActivity().getPackageName())));
        }
        etLatOutdoor.setKeyListener(DigitsKeyListener.getInstance("0123456789.NESW° "));
        etLatOutdoor.setTextSize(12);
        etLatOutdoor.setGravity(Gravity.START);
        etLatOutdoor.setPadding(2,2,2,2);

        if(newWPIDOutdoor < 9){
            String wpLongResTextOutdoor = "wp" + newWPIDOutdoor + "_long_out";
            etLongOutdoor.setText(getString(getResources().getIdentifier(wpLongResTextOutdoor, "string", requireActivity().getPackageName())));
        }
        etLongOutdoor.setKeyListener(DigitsKeyListener.getInstance("0123456789.NESW° "));
        etLongOutdoor.setTextSize(12);
        etLongOutdoor.setGravity(Gravity.START);
        etLongOutdoor.setPadding(2,2,2,2);

        if(newWPIDOutdoor < 9){
            String wpAltResTextOutdoor = "wp" + newWPIDOutdoor + "_alt_out";
            etAltOutdoor.setText(getString(getResources().getIdentifier(wpAltResTextOutdoor, "string", requireActivity().getPackageName())));
        }
        etAltOutdoor.setKeyListener(DigitsKeyListener.getInstance("0123456789.NESW° "));
        etAltOutdoor.setTextSize(12);
        etAltOutdoor.setGravity(Gravity.START);
        etAltOutdoor.setPadding(2,2,2,2);

        newOutdoorWPT.addView(tvNewWPTIDOutdoor);
        newOutdoorWPT.addView(etLatOutdoor);
        newOutdoorWPT.addView(etLongOutdoor);
        newOutdoorWPT.addView(etAltOutdoor);
        newOutdoorWPT.addView(tvTimestampOutdoor);
        newOutdoorWPT.addView(bOutdoor);
        outdoorRouteTable.addView(newOutdoorWPT);
    }

    private boolean startFusedLocationTracking(int locationRequestPriorityNr, String buttonText) {
        LocationCallback callback;
        int priority = -1;
        LocationRequest newLocationRequest = new LocationRequest();
        String fileNameComplete = null;
        switch(locationRequestPriorityNr){
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                locationRequestHighAcc = newLocationRequest;
                callback = mLocationCallbackHighAcc;
                fileNameHighAccComplete = etRouteLabel.getText().toString() + fileNameHighAcc + ".csv";
                fileNameComplete = fileNameHighAccComplete;
                break;
            case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                locationRequestBalanced = newLocationRequest;
                callback = mLocationCallbackBalanced;
                fileNameBalancedComplete = etRouteLabel.getText().toString() + fileNameBalanced + ".csv";
                fileNameComplete = fileNameBalancedComplete;
                break;
            case LocationRequest.PRIORITY_LOW_POWER:
                priority = LocationRequest.PRIORITY_LOW_POWER;
                locationRequestLowPower = newLocationRequest;
                callback = mLocationCallbackLowPow;
                fileNameLowPowComplete = etRouteLabel.getText().toString() + fileNameLowPow + ".csv";
                fileNameComplete = fileNameLowPowComplete;
                break;
            case LocationRequest.PRIORITY_NO_POWER:
                priority = LocationRequest.PRIORITY_NO_POWER;
                locationRequestNoPower = newLocationRequest;
                callback = mLocationCallbackNoPow;
                fileNameNoPowComplete = etRouteLabel.getText().toString() + fileNameNoPow + ".csv";
                fileNameComplete = fileNameNoPowComplete;
                break;
        }
        if(priority == -1){
            Toast.makeText(getActivity(), "Fehler beim erkennen der LocationRequest Priority!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestFineLocationPermission();
        } else {
            long timeInterv, fastesTimeInterv;
            if (MainActivity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && MainActivity.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn_loc)) == 0) {  // Benutzer hat Start gedrückt
                    if (timeIntervMs.getText().toString().equals("") || fastesTimeIntervMs.getText().toString().equals("") || etRouteLabel.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Es werden alle Eingaben benötigt!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    try {
                        timeInterv = Long.parseLong(timeIntervMs.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Zeitintervall-Eingabe muss positiv und ganzzahlig sein!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    try {
                        fastesTimeInterv = Long.parseLong(fastesTimeIntervMs.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Zeitintervall-Eingabe muss eine positive Zahl sein!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    GTWPSwithTSFileName = etRouteLabel.getText().toString() + "GTWPSwithTS" + ".csv";
                    if(MainActivity.fileExists(requireActivity(), fileNameComplete) || MainActivity.fileExists(requireActivity(), GTWPSwithTSFileName)){
                        Toast.makeText(getActivity(), "Der Routenname existiert bereits, bitte ändere die Bezeichnung der Route", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if(fileNameComplete==null){
                        Toast.makeText(getActivity(), "Fehler beim Starten einer FusedLocation-Positionierungsvariante", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    saveFile("Zeit"+"," + "Breitengrad" + "," + "Längengrad" + ","+ "Höhe" + ",Speed" + ",Genauigkeit" + "\n", fileNameComplete, false);

                    newLocationRequest.setPriority(priority);
                    newLocationRequest.setInterval(timeInterv);
                    newLocationRequest.setFastestInterval(fastesTimeInterv);

                    LocationServices.getFusedLocationProviderClient(requireActivity()).requestLocationUpdates(newLocationRequest, callback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                return;
                            }
                            switch(locationRequestPriorityNr){
                                case LocationRequest.PRIORITY_HIGH_ACCURACY:
                                    Log.e("TEST", "1");
                                    onLocationChangedHighAcc(locationResult.getLastLocation());
                                    break;
                                case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                                    Log.e("TEST", "2");
                                    onLocationChangedBalanced(locationResult.getLastLocation());
                                    break;
                                case LocationRequest.PRIORITY_LOW_POWER:
                                    Log.e("TEST", "3");
                                    onLocationChangedLowPow(locationResult.getLastLocation());
                                    break;
                                case LocationRequest.PRIORITY_NO_POWER:
                                    Log.e("TEST", "4");
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
                }

            } else {
                Toast.makeText(getActivity(), "Dein Standortdienst ist nicht aktiviert!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    View.OnClickListener getRmWPTButtonListener()  {
        return  (v)-> {
            TableRow tr = (TableRow) v.getParent();
            TableLayout tl = (TableLayout) tr.getParent();
            int rowIndex = tl.indexOfChild(tr);
            for(int i = rowIndex + 1; i < tl.getChildCount(); i++){
                TableRow row = (TableRow) tl.getChildAt(i);
                TextView wptNum = (TextView) row.getChildAt(0);
                wptNum.setText(String.format(Locale.GERMAN, "%d", (i - 1)));
            }
            tl.removeView(tr);
        };
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
        if (networkStateReceiver == null) {
            networkStateReceiver = new Data2ServerHelper();
            this.requireActivity().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("test", "\ninOnPause 1\n");
        MainActivity.locationManager.removeUpdates(this);
        MainActivity.sensorManager.unregisterListener(this);
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
            Drawable img = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_arrow, null);
            startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
        accAbsolutesList.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("test", "\ninOnDestroy 1\n");
        MainActivity.locationManager.removeUpdates(this);
        MainActivity.sensorManager.unregisterListener(this);
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
            Drawable img = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_arrow, null);
            startStopBtn.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
        accAbsolutesList.clear();

        // Remove network state receiver and listener as we don't need them at this point
        if (networkStateReceiver != null) {
            this.requireActivity().unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
        }
    }

    public void saveFile(String text, String fileName, boolean append)
    {
        FileOutputStream fos;

        try {
            if(append)
                fos = requireActivity().openFileOutput(fileName,Activity.MODE_APPEND);
            else
                fos = requireActivity().openFileOutput(fileName,Activity.MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();
            //Toast.makeText(getActivity(), "Gespeichert!", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
    /*
    public String getFileContent(String file)
    {
        String text = "";
        try {
            FileInputStream fis = requireActivity().openFileInput(file);
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
    }*/

    private void createRouteVersuch(){
        JSONObject obj = new JSONObject();
        try{
            String route_template = spinnerRoute.getSelectedItem()!=null?spinnerRoute.getSelectedItem().toString():"";
            obj.put("name", etRouteLabel.getText().toString());
            obj.put("route_template", route_template);
            obj.put("session_id", Session.getID());

        }catch (JSONException e){
            e.printStackTrace();
        }
        new ConnectionRest().execute("messwerteroute", obj.toString());
    }

    private void sendGPSDataRest(double ... params){
        JSONObject locData = new JSONObject();
        try{
            locData.put("latitude", params[0]);
            locData.put("longitude", params[1]);
            locData.put("altitude", params[2]);
            locData.put("speed", params[3]);
            locData.put("accuracy", params[4]);
            locData.put("timestamp", System.currentTimeMillis());
            locData.put("session_id", Session.getID());
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        new ConnectionRest().execute("lokalisierung",locData.toString());
    }

    private void initSpRoutes() {
        this.spinnerRoute.setOnTouchListener((View v, MotionEvent motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                new ConnectionRest(
                        (json) -> {
                            if(json != null)
                            try {
                                String[] items = new String[json.length()];
                                for (int i = 0; i < json.length(); i++) {
                                    items[i] = json.getJSONObject(i).getString("name");
                                }
                                boolean oldList = this.spinnerRoute.getAdapter().getCount() == items.length;
                                if (oldList)
                                    for (int i = 0; i < items.length; i++)
                                        oldList = oldList && (items[i].equals(this.spinnerRoute.getItemAtPosition(i).toString()));
                                if (!oldList) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                                            android.R.layout.simple_spinner_item, items);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    this.spinnerRoute.setAdapter(adapter);
                                }

                            } catch (Exception e) {
                                Log.d("REST ERROR", e.getMessage());
                            }
                        }
                ).execute("route");
                v.performClick();
            }
            return true;
        });
        spinnerRoute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new ConnectionRest(
                        (json) -> {
                            try {
                                if(json == null)
                                    json = getOfflineRoute().getJSONObject(i).getJSONArray("waypoints");

                                TableLayout indoorRouteTable = requireActivity().findViewById(R.id.routeIndoors);
                                if (indoorRouteTable.getChildCount() > 1)
                                    indoorRouteTable.removeViews(1, indoorRouteTable.getChildCount() - 1);
                                for (int j = 0; j < json.length(); j++)
                                    createTableRow(json.getJSONObject(j).getString("latitude"),
                                            json.getJSONObject(j).getString("longitude"),
                                            json.getJSONObject(j).getString("altitude"));
                            } catch (Exception e) {
                                Log.d("REST ERROR", e.getMessage());
                            }
                        }
                ).execute("route/" + spinnerRoute.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        new ConnectionRest(
                (json) -> {

                    try {
                        if (json != null)
                            setOfflineRoute(json.toString());
                        else
                            json = getOfflineRoute();
                            String[] s = new String[json.length()];

                            for (int i = 0; i < json.length(); i++) {
                                s[i] = json.getJSONObject(i).getString("name");
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                                    android.R.layout.simple_spinner_item, s);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            this.spinnerRoute.setAdapter(adapter);

                    } catch(Exception e){
                        Log.d("REST ERROR", e.getMessage());
                    }
                }
        ).execute("route");
    }
    private JSONArray getOfflineRoute() {
        return Data2ServerHelper.rcJsonFile("route.json","[]");
    }
    private void setOfflineRoute(String routen) throws JSONException {

        JSONArray arr = new JSONArray(routen);

        for(int i = 0; i<arr.length();i++){
            final int j = i;
            new ConnectionRest(
                    (json) -> {
                        try {
                            arr.getJSONObject(j).put("waypoints",json);
                            FileOutputStream fos =  requireContext().openFileOutput("route.json", Context.MODE_PRIVATE);
                            fos.write(arr.toString().getBytes());
                            fos.close();
                        } catch(Exception e){
                            Log.d("REST ERROR", e.getMessage());
                        }
                    }
            ).execute("route/"+arr.getJSONObject(i).getString("name"));
        }
    }

    public void createTableRow(String ... args){
        // Neue Zeile zur Tabelle hinzugügen ( = neuer Waypoint zur Route)
        TableLayout indoorRouteTable = requireActivity().findViewById(R.id.routeIndoors);
        TableRow newIndoorWPT = new TableRow(getActivity());
        int newWPIDIndoor = indoorRouteTable.getChildCount(); // WPID = Nummer des Waypoints; 1 = A, 2 = B, usw.

        TextView tvNewWPTIDIndoor = new TextView(getActivity()),
                tvTimestampIndoor = new TextView(getActivity());
        tvNewWPTIDIndoor.setTextSize(12);
        tvNewWPTIDIndoor.setGravity(Gravity.START);
        tvNewWPTIDIndoor.setPadding(2,2,2,2);
        tvNewWPTIDIndoor.setText(String.format(Locale.getDefault(),"%d",newWPIDIndoor));
        tvTimestampIndoor.setText(getString(R.string.loc_data_empty));
        tvTimestampIndoor.setTextSize(12);
        tvTimestampIndoor.setGravity(Gravity.START);
        tvTimestampIndoor.setPadding(2,2,2,2);

                /*
                EditTexts ermöglichen die Eingabe der Positionsdaten,
                die Daten einer Beispielroute sind aber schon unterlegt (habe ich selbst erstellt, könnten wir auch so nutzen)
                 */
        EditText etLatIndoor = new EditText(getActivity()), etLongIndoor = new EditText(getActivity()), etAltIndoor = new EditText(getActivity());

        etLatIndoor.setText(args[0].substring(0,9));
        etLatIndoor.setTextSize(12);
        etLatIndoor.setGravity(Gravity.START);
        etLatIndoor.setPadding(2,2,2,2);

        etLongIndoor.setText(args[1].substring(0,9));
        etLongIndoor.setTextSize(12);
        etLongIndoor.setGravity(Gravity.START);
        etLongIndoor.setPadding(2,2,2,2);


        etAltIndoor.setText(args[2]);
        etAltIndoor.setTextSize(12);
        etAltIndoor.setGravity(Gravity.START);
        etAltIndoor.setPadding(2,2,2,2);

        newIndoorWPT.addView(tvNewWPTIDIndoor);
        newIndoorWPT.addView(etLatIndoor);
        newIndoorWPT.addView(etLongIndoor);
        newIndoorWPT.addView(etAltIndoor);
        newIndoorWPT.addView(tvTimestampIndoor);
        indoorRouteTable.addView(newIndoorWPT);
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
        mGoogleApiClient = new GoogleApiClient.Builder(requireActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double absolute = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
        accAbsolutesList.add(absolute);
        int standingStillCounter = 0;
        boolean moving = true;
//        Log.e("TESTT", "absolute: " + absolute);
        int numOfValsGreaterThan10 = 0;
        if(accAbsolutesList.size() > 30){
            for (int i = 0; i < accAbsolutesList.size(); i++){
                if(Math.abs(accAbsolutesList.get(i) - SensorManager.GRAVITY_EARTH) < 0.5){
                    standingStillCounter++;
                    if(standingStillCounter > 20){
                        moving = false;
                        break;
                    }
                }
            }
        }
        if(!moving){
            Log.e("TESTT", "!moving");
            MainActivity.locationManager.removeUpdates(this);
            Toast.makeText(getActivity(), "Stillstand festgestellt, LocationUpdates unterbrochen!", Toast.LENGTH_SHORT).show();
            requestingLocationUpdates = false;
        }
        if(!requestingLocationUpdates && moving){
            Log.e("TESTT", "!requestingLocationUpdates und moving");
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestFineLocationPermission();
            } else {
                MainActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                Toast.makeText(getActivity(), "Stillstand wieder aufgehoben, LocationUpdates fortgesetzt!", Toast.LENGTH_SHORT).show();
                requestingLocationUpdates = true;
            }
        }
        if(accAbsolutesList.size() > 30){
            accAbsolutesList.remove(0);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
