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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MagnetometerFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnMagnetom;
    Spinner sampleFreqSpinnerMagnetom;
    CheckBox csvMag;
    TextView tvXVal, tvYVal, tvZVal, tvAllDetailsMagnetom, tvCsvContent;
    Sensor sensorToBeListenedTo;
    String fileName = "MagFile.csv";
    Switch saveswitch;
    double x1,y1,z1;
    Timer timer = new Timer();
    GraphView graphMagn, graphMagn2;
    LineGraphSeries<DataPoint> seriesX, seriesY, seriesZ;
    LineGraphSeries<DataPoint> seriesX2, seriesY2, seriesZ2;
    double graphLastXValTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_magnetometer, container, false);
        startStopBtnMagnetom = view.findViewById(R.id.bStartStopMagnetom);
        startStopBtnMagnetom.setOnClickListener(this);
        sampleFreqSpinnerMagnetom = view.findViewById(R.id.spinnerSampleFreqMagnetom);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sampling_frequencies, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        sampleFreqSpinnerMagnetom.setAdapter(adapter);
        csvMag = view.findViewById(R.id.csvBoxMag);
        csvMag.setEnabled(true);
        saveswitch = view.findViewById(R.id.switchsvmagn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsMagnetom = getActivity().findViewById(R.id.detailsMagnetom);
        tvCsvContent = getActivity().findViewById(R.id.tvSavedCsvFileMagn);
        tvXVal = getActivity().findViewById(R.id.xValueMagnetom);
        tvYVal = getActivity().findViewById(R.id.yValueMagnetom);
        tvZVal = getActivity().findViewById(R.id.zValueMagnetom);
        tvXVal.setText(getString(R.string.x_valMagnetomEmpty, "--"));
        tvYVal.setText(getString(R.string.y_valMagnetomEmpty, "--"));
        tvZVal.setText(getString(R.string.z_valMagnetomEmpty, "--"));
        sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(sensorToBeListenedTo != null){
            if(Build.VERSION.SDK_INT >= 24)
                displaySensorDetailsWithStyle(sensorToBeListenedTo);
            else
                displaySensorDetailsWithoutStyle(sensorToBeListenedTo);
            setUpGraphView();
            setUpGraphView2();
        }
        else{
            Toast.makeText(getActivity(), "Dein Gerät besitzt kein Magnetometer!", Toast.LENGTH_SHORT).show();
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
                            new ConnectionRest().execute("magnetometer", data.toString());
                            Log.d("RESTAPI", data.toString());
                        }
                    }, 0, 1000);
                } else {
                    timer.cancel();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sensorManager.unregisterListener(this);
        String buttonText = startStopBtnMagnetom.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn)) == 0) {
            startStopBtnMagnetom.setText(getResources().getString(R.string.start_listening_btn));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnMagnetom.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        tvXVal.setText(getString(R.string.x_valMagnetom, event.values[0]));
        tvYVal.setText(getString(R.string.y_valMagnetom, event.values[1]));
        tvZVal.setText(getString(R.string.z_valMagnetom, event.values[2]));
        seriesX.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        seriesY.appendData(new DataPoint(graphLastXValTime, event.values[1] ), true, 1000);
        seriesZ.appendData(new DataPoint(graphLastXValTime, event.values[2] ), true, 1000);
        seriesX2.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        seriesY2.appendData(new DataPoint(graphLastXValTime, event.values[1] ), true, 1000);
        seriesZ2.appendData(new DataPoint(graphLastXValTime, event.values[2] ), true, 1000);
        graphLastXValTime++;
        if(csvMag.isChecked()) {
            saveFile(event.timestamp / 1000000 + " :  " + "x: " + event.values[0] + "        y: " + event.values[1] + "        z: " + event.values[2]+"\n", true);
            //Toast.makeText(getActivity(), "" + readFile("ACCFile.csv"), Toast.LENGTH_SHORT).show();
        }
        x1 = event.values[0];
        y1 = event.values[1];
        z1 = event.values[2];
    }

    private void setUpGraphView(){
        graphMagn = (GraphView) getActivity().findViewById(R.id.graphMagn);
        graphMagn.getViewport().setYAxisBoundsManual(true);
        graphMagn.getViewport().setMinY(-1 * sensorToBeListenedTo.getMaximumRange());
        graphMagn.getViewport().setMaxY(sensorToBeListenedTo.getMaximumRange());
        graphMagn.getViewport().setMinX(0);
        graphMagn.getViewport().setMaxX(100);
        graphMagn.getViewport().setXAxisBoundsManual(true);
        graphMagn.getLegendRenderer().setVisible(true);
        graphMagn.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphMagn.getLegendRenderer().setPadding(5);
        graphMagn.getLegendRenderer().setTextSize(25);
        graphMagn.getLegendRenderer().setMargin(30);
        graphMagn.getGridLabelRenderer().setVerticalAxisTitle("µT");
        graphMagn.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphMagn.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        seriesX = new LineGraphSeries<DataPoint>();
        seriesX.setColor(Color.BLUE);
        seriesX.setTitle("X");
        graphMagn.addSeries(seriesX);
        seriesY = new LineGraphSeries<DataPoint>();
        seriesY.setColor(Color.GREEN);
        seriesY.setTitle("Y");
        graphMagn.addSeries(seriesY);
        seriesZ = new LineGraphSeries<DataPoint>();
        seriesZ.setColor(Color.RED);
        seriesZ.setTitle("Z");
        graphMagn.addSeries(seriesZ);
    }

    private void setUpGraphView2(){
        graphMagn2 = (GraphView) getActivity().findViewById(R.id.graphMagn2);
        graphMagn2.getViewport().setYAxisBoundsManual(true);
        graphMagn2.getViewport().setMinY(-1 * sensorToBeListenedTo.getMaximumRange());
        graphMagn2.getViewport().setMaxY(sensorToBeListenedTo.getMaximumRange());
        graphMagn2.getViewport().setMinX(0);
        graphMagn2.getViewport().setMaxX(1000);
        graphMagn2.getViewport().setXAxisBoundsManual(true);
        graphMagn2.getLegendRenderer().setVisible(true);
        graphMagn2.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphMagn2.getLegendRenderer().setPadding(5);
        graphMagn2.getLegendRenderer().setTextSize(25);
        graphMagn2.getLegendRenderer().setMargin(30);
        graphMagn2.getGridLabelRenderer().setVerticalAxisTitle("µT");
        graphMagn2.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphMagn2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        seriesX2 = new LineGraphSeries<DataPoint>();
        seriesX2.setColor(Color.BLUE);
        seriesX2.setTitle("X");
        graphMagn2.addSeries(seriesX2);
        seriesY2 = new LineGraphSeries<DataPoint>();
        seriesY2.setColor(Color.GREEN);
        seriesY2.setTitle("Y");
        graphMagn2.addSeries(seriesY2);
        seriesZ2 = new LineGraphSeries<DataPoint>();
        seriesZ2.setColor(Color.RED);
        seriesZ2.setTitle("Z");
        graphMagn2.addSeries(seriesZ2);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopMagnetom:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnMagnetom.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {
                        String sampleFreq = sampleFreqSpinnerMagnetom.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                            graphMagn.setVisibility(View.VISIBLE);
                            graphMagn2.setVisibility(View.INVISIBLE);
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                            graphMagn.setVisibility(View.INVISIBLE);
                            graphMagn2.setVisibility(View.VISIBLE);
                        }
                        if(csvMag.isChecked())
                            saveFile("Zeit"+"                 " + "X-Achse in µT" + "       " + "Y-Achse in µT" + "     "+ "Z-Achse in µT\n", false);
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnMagnetom.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnMagnetom.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    } else {
                        Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                        MainActivity.sensorManager.unregisterListener(this, sensor);
                        startStopBtnMagnetom.setText(getResources().getString(R.string.start_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                        startStopBtnMagnetom.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        saveswitch.setChecked(false);
                        if(csvMag.isChecked()) {
                            Toast.makeText(getActivity(), "Datei-Speicherort: " + getActivity().getFilesDir() + "/" + fileName, Toast.LENGTH_LONG).show();
                            tvCsvContent.setText(getFileContent(fileName));
                        }
                    }
                } else {
                    // Failure! Sensor not found on device.
                    Toast.makeText(getActivity(), "Dein Gerät besitzt kein Magnetometer!", Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    @TargetApi(24)
    private void displaySensorDetailsWithStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_magnetom_withStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsMagnetom.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    private void displaySensorDetailsWithoutStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_magnetom_withoutStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsMagnetom.setText(text);
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

}
