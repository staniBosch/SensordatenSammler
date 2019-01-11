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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GyroFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnGyro;
    Spinner sampleFreqSpinnerGyro;
    TextView tvXVal, tvYVal, tvZVal, tvAllDetailsGyro, tvCsvContent;
    Sensor sensorToBeListenedTo;
    private static final String fileName = "GyroFile.csv";
    CheckBox csvGyro;
    Switch switchsv;
    GraphView graphGyro, graphGyro2;
    LineGraphSeries<DataPoint> seriesX, seriesY, seriesZ;
    LineGraphSeries<DataPoint> seriesX2, seriesY2, seriesZ2;
    double graphLastXValTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gyro, container, false);
        startStopBtnGyro = view.findViewById(R.id.bStartStopGyro);
        startStopBtnGyro.setOnClickListener(this);
        sampleFreqSpinnerGyro = view.findViewById(R.id.spinnerSampleFreqGyro);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sampling_frequencies, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        sampleFreqSpinnerGyro.setAdapter(adapter);
        csvGyro = view.findViewById(R.id.csvBoxGyro);
        csvGyro.setEnabled(true);
        switchsv = view.findViewById(R.id.switchsv);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsGyro = getActivity().findViewById(R.id.detailsGyro);
        tvCsvContent = getActivity().findViewById(R.id.tvSavedCsvFileGyro);
        tvXVal = getActivity().findViewById(R.id.xValueGyro);
        tvYVal = getActivity().findViewById(R.id.yValueGyro);
        tvZVal = getActivity().findViewById(R.id.zValueGyro);
        tvXVal.setText(getString(R.string.x_valGyroEmpty, "--"));
        tvYVal.setText(getString(R.string.y_valGyroEmpty, "--"));
        tvZVal.setText(getString(R.string.z_valGyroEmpty, "--"));
        sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(sensorToBeListenedTo != null){
            if(Build.VERSION.SDK_INT >= 24)
                displaySensorDetailsWithStyle(sensorToBeListenedTo);
            else
                displaySensorDetailsWithoutStyle(sensorToBeListenedTo);
            setUpGraphView();
            setUpGraphView2();
        }
        else{
            Toast.makeText(getActivity(), "Dein Gerät besitzt kein Gyroskop!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpGraphView(){
        graphGyro = (GraphView) getActivity().findViewById(R.id.graphGyro);
        graphGyro.getViewport().setYAxisBoundsManual(true);
        graphGyro.getViewport().setMinY(-1 * sensorToBeListenedTo.getMaximumRange());
        graphGyro.getViewport().setMaxY(sensorToBeListenedTo.getMaximumRange());
        graphGyro.getViewport().setMinX(0);
        graphGyro.getViewport().setMaxX(100);
        graphGyro.getViewport().setXAxisBoundsManual(true);
        graphGyro.getLegendRenderer().setVisible(true);
        graphGyro.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphGyro.getLegendRenderer().setPadding(5);
        graphGyro.getLegendRenderer().setTextSize(25);
        graphGyro.getLegendRenderer().setMargin(30);
        graphGyro.getGridLabelRenderer().setVerticalAxisTitle("rad/s");
        graphGyro.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphGyro.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        seriesX = new LineGraphSeries<DataPoint>();
        seriesX.setColor(Color.BLUE);
        seriesX.setTitle("X");
        graphGyro.addSeries(seriesX);
        seriesY = new LineGraphSeries<DataPoint>();
        seriesY.setColor(Color.GREEN);
        seriesY.setTitle("Y");
        graphGyro.addSeries(seriesY);
        seriesZ = new LineGraphSeries<DataPoint>();
        seriesZ.setColor(Color.RED);
        seriesZ.setTitle("Z");
        graphGyro.addSeries(seriesZ);
    }

    private void setUpGraphView2(){
        graphGyro2 = (GraphView) getActivity().findViewById(R.id.graphGyro2);
        graphGyro2.getViewport().setYAxisBoundsManual(true);
        graphGyro2.getViewport().setMinY(-1 * sensorToBeListenedTo.getMaximumRange());
        graphGyro2.getViewport().setMaxY(sensorToBeListenedTo.getMaximumRange());
        graphGyro2.getViewport().setMinX(0);
        graphGyro2.getViewport().setMaxX(1000);
        graphGyro2.getViewport().setXAxisBoundsManual(true);
        graphGyro2.getLegendRenderer().setVisible(true);
        graphGyro2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphGyro2.getLegendRenderer().setPadding(5);
        graphGyro2.getLegendRenderer().setTextSize(25);
        graphGyro2.getLegendRenderer().setMargin(30);
        graphGyro2.getGridLabelRenderer().setVerticalAxisTitle("rad/s");
        graphGyro2.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphGyro2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        seriesX2 = new LineGraphSeries<DataPoint>();
        seriesX2.setColor(Color.BLUE);
        seriesX2.setTitle("X");
        graphGyro2.addSeries(seriesX2);
        seriesY2 = new LineGraphSeries<DataPoint>();
        seriesY2.setColor(Color.GREEN);
        seriesY2.setTitle("Y");
        graphGyro2.addSeries(seriesY2);
        seriesZ2 = new LineGraphSeries<DataPoint>();
        seriesZ2.setColor(Color.RED);
        seriesZ2.setTitle("Z");
        graphGyro2.addSeries(seriesZ2);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sensorManager.unregisterListener(this);
        String buttonText = startStopBtnGyro.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn)) == 0) {
            startStopBtnGyro.setText(getResources().getString(R.string.start_listening_btn));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnGyro.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        tvXVal.setText(getString(R.string.x_valGyro, event.values[0]));
        tvYVal.setText(getString(R.string.y_valGyro, event.values[1]));
        tvZVal.setText(getString(R.string.z_valGyro, event.values[2]));
        seriesX.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        seriesY.appendData(new DataPoint(graphLastXValTime, event.values[1] ), true, 1000);
        seriesZ.appendData(new DataPoint(graphLastXValTime, event.values[2] ), true, 1000);
        seriesX2.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        seriesY2.appendData(new DataPoint(graphLastXValTime, event.values[1] ), true, 1000);
        seriesZ2.appendData(new DataPoint(graphLastXValTime, event.values[2] ), true, 1000);
        graphLastXValTime++;
        if(csvGyro.isChecked()) {
            saveFile(event.timestamp / 1000000 + " :  " + "x: " + event.values[0] + "    y: " + event.values[1] + "    z: " + event.values[2]+"\n", true);
            //Toast.makeText(getActivity(), "" + readFile("ACCFile.csv"), Toast.LENGTH_SHORT).show();
        }

        if(switchsv.isChecked())
            sendDataRest(event.values[0],event.values[1],event.values[2]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopGyro:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnGyro.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {  // Benutzer hat Start gedrückt
                        String sampleFreq = sampleFreqSpinnerGyro.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                            graphGyro.setVisibility(View.VISIBLE);
                            graphGyro2.setVisibility(View.INVISIBLE);
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                            graphGyro.setVisibility(View.INVISIBLE);
                            graphGyro2.setVisibility(View.VISIBLE);
                        }
                        if(csvGyro.isChecked())
                            saveFile("Zeit"+"                 " + "X-Achse in rad/s" + "       " + "Y-Achse in rad/s" + "     "+ "Z-Achse in rad/s\n", false);
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnGyro.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnGyro.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    } else {  // Benutzer hat Stop gedrückt
                        Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                        MainActivity.sensorManager.unregisterListener(this, sensor);
                        startStopBtnGyro.setText(getResources().getString(R.string.start_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                        startStopBtnGyro.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        switchsv.setChecked(false);
                        if(csvGyro.isChecked()) {
                            Toast.makeText(getActivity(), "Datei-Speicherort: " + getActivity().getFilesDir() + "/" + fileName, Toast.LENGTH_LONG).show();
                            tvCsvContent.setText(getFileContent(fileName));
                        }
                    }
                } else {
                    // Failure! Sensor not found on device.
                    Toast.makeText(getActivity(), "Dein Gerät besitzt kein Gyroskop nicht!", Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    @TargetApi(24)
    private void displaySensorDetailsWithStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_gyro_withStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsGyro.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    private void displaySensorDetailsWithoutStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_gyro_withoutStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsGyro.setText(text);
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

    private void sendDataRest(double ... params){
        JSONObject data = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try{
            data.put("x", params[0]);
            data.put("y", params[1]);
            data.put("z", params[2]);
            data.put("session_id",Session.getID());
            jsonArray.put(data);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        new ConnectionRest().execute("gyroskop",data.toString());
        Log.d("RESTAPI",data.toString());
    }
}
