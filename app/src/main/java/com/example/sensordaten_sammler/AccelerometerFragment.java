package com.example.sensordaten_sammler;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class AccelerometerFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnAcc;
    Spinner sampleFreqSpinnerAcc;
    TextView tvXVal, tvYVal, tvZVal, tvAllDetailsAcc;
    Sensor sensorToBeListenedTo;
    GraphView graphAcc;
    LineGraphSeries<DataPoint> seriesX, seriesY, seriesZ;
    Switch saveswitch;
    double graphLastXValTime;
    double x1,y1,z1;
    Timer timer = new Timer();
//    long startTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        startStopBtnAcc = view.findViewById(R.id.bStartStopAcc);
        startStopBtnAcc.setOnClickListener(this);
        sampleFreqSpinnerAcc = view.findViewById(R.id.spinnerSampleFreqAcc);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sampling_frequencies, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        sampleFreqSpinnerAcc.setAdapter(adapter);
//        startTime = System.nanoTime() / 10000000;
        saveswitch = view.findViewById(R.id.switchsv_ac);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsAcc = getActivity().findViewById(R.id.detailsAcc);
        tvXVal = getActivity().findViewById(R.id.xValueAcc);
        tvYVal = getActivity().findViewById(R.id.yValueAcc);
        tvZVal = getActivity().findViewById(R.id.zValueAcc);
        tvXVal.setText(getString(R.string.x_valAccEmpty, "--"));
        tvYVal.setText(getString(R.string.y_valAccEmpty, "--"));
        tvZVal.setText(getString(R.string.z_valAccEmpty, "--"));
        sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(sensorToBeListenedTo != null){
            if(Build.VERSION.SDK_INT >= 24)
                displaySensorDetailsWithStyle(sensorToBeListenedTo);
            else
                displaySensorDetailsWithoutStyle(sensorToBeListenedTo);
            setUpGraphView();
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
        tvXVal.setText(getString(R.string.x_valAcc, event.values[0]));
        tvYVal.setText(getString(R.string.y_valAcc, event.values[1]));
        tvZVal.setText(getString(R.string.z_valAcc, event.values[2]));
        seriesX.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        seriesY.appendData(new DataPoint(graphLastXValTime, event.values[1] ), true, 1000);
        seriesZ.appendData(new DataPoint(graphLastXValTime, event.values[2] ), true, 1000);
        graphLastXValTime++;
        x1 = event.values[0];
        z1 = event.values[1];
        y1 = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopAcc:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnAcc.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {
                        String sampleFreq = sampleFreqSpinnerAcc.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                        }
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnAcc.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    } else {
                        Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        MainActivity.sensorManager.unregisterListener(this, sensor);
                        startStopBtnAcc.setText(getResources().getString(R.string.start_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                        startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        saveswitch.setChecked(false);
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
