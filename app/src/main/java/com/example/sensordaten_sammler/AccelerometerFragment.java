package com.example.sensordaten_sammler;

import android.annotation.TargetApi;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sensordaten_sammler.rest.ConnectionRest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AccelerometerFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnAcc;
    Spinner sampleFreqSpinnerAcc;
    TextView tvXVal, tvYVal, tvZVal, tvAbsoluteVal, tvAllDetailsAcc, tvCsvContent;
    Sensor sensorToBeListenedTo;
    GraphView graphAcc, graphAcc2;
    CheckBox csvAcc;
    LineGraphSeries<DataPoint> seriesX, seriesY, seriesZ;
    LineGraphSeries<DataPoint> seriesX2, seriesY2, seriesZ2;
    Switch saveswitch;
    double graphLastXValTime;
    double x1,y1,z1;
    Timer timer = new Timer();
    private static final String fileName = "ACCFile.csv";
    List<Double> accAbsolutesList;
    public boolean requestingLocationUpdates;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        csvAcc = view.findViewById(R.id.csvBoxAcc);
        csvAcc.setEnabled(true);
        startStopBtnAcc = view.findViewById(R.id.bStartStopAcc);
        startStopBtnAcc.setOnClickListener(this);
        sampleFreqSpinnerAcc = view.findViewById(R.id.spinnerSampleFreqAcc);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sampling_frequencies, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        sampleFreqSpinnerAcc.setAdapter(adapter);
        saveswitch = view.findViewById(R.id.switchsv_ac);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsAcc = getActivity().findViewById(R.id.detailsAcc);
        tvCsvContent = getActivity().findViewById(R.id.tvSavedCsvFileAcc);
        tvXVal = getActivity().findViewById(R.id.xValueAcc);
        tvYVal = getActivity().findViewById(R.id.yValueAcc);
        tvZVal = getActivity().findViewById(R.id.zValueAcc);
        tvAbsoluteVal = getActivity().findViewById(R.id.absValueAcc);
        accAbsolutesList = new LinkedList<>();
        tvXVal.setText(getString(R.string.x_valAccEmpty, "--"));
        tvYVal.setText(getString(R.string.y_valAccEmpty, "--"));
        tvZVal.setText(getString(R.string.z_valAccEmpty, "--"));
        tvAbsoluteVal.setText(getString(R.string.abs_valAccEmpty, "--"));
        sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensorToBeListenedTo != null){
            if(Build.VERSION.SDK_INT >= 24)
                displaySensorDetailsWithStyle(sensorToBeListenedTo);
            else
                displaySensorDetailsWithoutStyle(sensorToBeListenedTo);
            setUpGraphView();
            setUpGraphView2();
        }
        else{
            Toast.makeText(getActivity(), "Dein Gerät besitzt kein Accelerometer!", Toast.LENGTH_SHORT).show();
        }

        saveswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                        JSONObject data = new JSONObject();

                        try {
                            data.put("x", x1);
                            data.put("y", y1);
                            data.put("z", z1);
                            data.put("session_id", Session.getID());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new ConnectionRest().execute("accelerometer", data.toString());
                         Log.d("RESTAPI", data.toString());
                        }
                    }, 0, 1000);
                } else {
                    timer.cancel();
                }
            }
        });
    }

    private void setUpGraphView(){
        graphAcc = (GraphView) getActivity().findViewById(R.id.graphAcc);
        graphAcc.getViewport().setYAxisBoundsManual(true);
        graphAcc.getViewport().setMinY(-1 * sensorToBeListenedTo.getMaximumRange());
        graphAcc.getViewport().setMaxY(sensorToBeListenedTo.getMaximumRange());
        graphAcc.getViewport().setMinX(0);
        graphAcc.getViewport().setMaxX(100);
        graphAcc.getViewport().setXAxisBoundsManual(true);
        graphAcc.getLegendRenderer().setVisible(true);
        graphAcc.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphAcc.getLegendRenderer().setPadding(5);
        graphAcc.getLegendRenderer().setTextSize(25);
        graphAcc.getLegendRenderer().setMargin(30);
        graphAcc.getGridLabelRenderer().setVerticalAxisTitle("m/s²");
        graphAcc.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphAcc.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        seriesX = new LineGraphSeries<DataPoint>();
        seriesX.setColor(Color.BLUE);
        seriesX.setTitle("X");
        graphAcc.addSeries(seriesX);
        seriesY = new LineGraphSeries<DataPoint>();
        seriesY.setColor(Color.GREEN);
        seriesY.setTitle("Y");
        graphAcc.addSeries(seriesY);
        seriesZ = new LineGraphSeries<DataPoint>();
        seriesZ.setColor(Color.RED);
        seriesZ.setTitle("Z");
        graphAcc.addSeries(seriesZ);
    }

    private void setUpGraphView2(){
        graphAcc2 = (GraphView) getActivity().findViewById(R.id.graphAcc2);
        graphAcc2.getViewport().setYAxisBoundsManual(true);
        graphAcc2.getViewport().setMinY(-1 * sensorToBeListenedTo.getMaximumRange());
        graphAcc2.getViewport().setMaxY(sensorToBeListenedTo.getMaximumRange());
        graphAcc2.getViewport().setMinX(0);
        graphAcc2.getViewport().setMaxX(1000);
        graphAcc2.getViewport().setXAxisBoundsManual(true);
        graphAcc2.getLegendRenderer().setVisible(true);
        graphAcc2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphAcc2.getLegendRenderer().setPadding(5);
        graphAcc2.getLegendRenderer().setTextSize(25);
        graphAcc2.getLegendRenderer().setMargin(30);
        graphAcc2.getGridLabelRenderer().setVerticalAxisTitle("m/s²");
        graphAcc2.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphAcc2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        seriesX2 = new LineGraphSeries<DataPoint>();
        seriesX2.setColor(Color.BLUE);
        seriesX2.setTitle("X");
        graphAcc2.addSeries(seriesX2);
        seriesY2 = new LineGraphSeries<DataPoint>();
        seriesY2.setColor(Color.GREEN);
        seriesY2.setTitle("Y");
        graphAcc2.addSeries(seriesY2);
        seriesZ2 = new LineGraphSeries<DataPoint>();
        seriesZ2.setColor(Color.RED);
        seriesZ2.setTitle("Z");
        graphAcc2.addSeries(seriesZ2);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sensorManager.unregisterListener(this);
        String buttonText = startStopBtnAcc.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn)) == 0) {
            startStopBtnAcc.setText(getResources().getString(R.string.start_listening_btn));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        double absolute = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
//        double gX = event.values[0] / SensorManager.GRAVITY_EARTH;
//        double gY = event.values[1] / SensorManager.GRAVITY_EARTH;
//        double gZ = event.values[2] / SensorManager.GRAVITY_EARTH;
//
//        // gForce will be close to 1 when there is no movement.
//        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

//        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
//            final long now = System.currentTimeMillis();
//            // ignore shake events too close to each other (500ms)
//            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
//                return;
//            }
//
//            // reset the shake count after 3 seconds of no shakes
//            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
//                mShakeCount = 0;
//            }
//
//            mShakeTimestamp = now;
//            mShakeCount++;
//
//            mListener.onShake(mShakeCount);
//        }
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
            Toast.makeText(getActivity(), "Stillstand festgestellt!", Toast.LENGTH_SHORT).show();
            requestingLocationUpdates = false;
        }
        if(!requestingLocationUpdates && moving){
            Log.e("TESTT", "!requestingLocationUpdates und moving");
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                requestFineLocationPermission();
//            } else {
//                MainActivity.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Toast.makeText(getActivity(), "Stillstand wieder aufgehoben", Toast.LENGTH_SHORT).show();
                requestingLocationUpdates = true;
//            }
        }
        if(accAbsolutesList.size() > 30){
            accAbsolutesList.remove(0);
        }
        tvXVal.setText(getString(R.string.x_valAcc, event.values[0]));
        tvYVal.setText(getString(R.string.y_valAcc, event.values[1]));
        tvZVal.setText(getString(R.string.z_valAcc, event.values[2]));
        tvAbsoluteVal.setText(getString(R.string.abs_valAcc, absolute));
        seriesX.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        seriesY.appendData(new DataPoint(graphLastXValTime, event.values[1] ), true, 1000);
        seriesZ.appendData(new DataPoint(graphLastXValTime, event.values[2] ), true, 1000);
        seriesX2.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        seriesY2.appendData(new DataPoint(graphLastXValTime, event.values[1] ), true, 1000);
        seriesZ2.appendData(new DataPoint(graphLastXValTime, event.values[2] ), true, 1000);
        graphLastXValTime++;
        if(csvAcc.isChecked()) {
            saveFile(event.timestamp / 1000000 + " :  " + "x: " + event.values[0] + "           y: " + event.values[1] + "           z: " + event.values[2]+"\n", true);
            //Toast.makeText(getActivity(), "" + readFile("ACCFile.csv"), Toast.LENGTH_SHORT).show();
        }

        x1 = event.values[0];
        y1 = event.values[1];
        z1 = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void saveFile(String text, boolean append)
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopAcc:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnAcc.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {  // Benutzer hat Start gedrückt
                        String sampleFreq = sampleFreqSpinnerAcc.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                            graphAcc.setVisibility(View.VISIBLE);
                            graphAcc2.setVisibility(View.INVISIBLE);
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                            graphAcc.setVisibility(View.INVISIBLE);
                            graphAcc2.setVisibility(View.VISIBLE);
                        }
                        if(csvAcc.isChecked())
                            saveFile("Zeit"+"                  " + "X-Achse in m/s²" + "        " + "Y-Achse in m/s²" + "      "+ "Z-Achse in m/s²\n", false);
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnAcc.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    } else {  // Benutzer hat Stop gedrückt
                        Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        MainActivity.sensorManager.unregisterListener(this, sensor);
                        startStopBtnAcc.setText(getResources().getString(R.string.start_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                        startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        saveswitch.setChecked(false);
                        accAbsolutesList.clear();
                        if(csvAcc.isChecked()) {
                            Toast.makeText(getActivity(), "Datei-Speicherort: " + getActivity().getFilesDir() + "/" + fileName, Toast.LENGTH_LONG).show();
                            tvCsvContent.setText(getFileContent(fileName));
                        }
                    }
                } else {
                    // Failure! Sensor not found on device.
                    Toast.makeText(getActivity(), "Dein Gerät besitzt kein Accelerometer!", Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    @TargetApi(24)
    private void displaySensorDetailsWithStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_acc_withStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsAcc.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    private void displaySensorDetailsWithoutStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_acc_withoutStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsAcc.setText(text);
    }

}
