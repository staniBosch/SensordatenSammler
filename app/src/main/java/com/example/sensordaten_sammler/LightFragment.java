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
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Array;

public class LightFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnLight;
    Spinner sampleFreqSpinnerLight;
    TextView lightVal, tvAllDetailsLight, tvCsvContent;
    Sensor sensorToBeListenedTo;
    String fileName = "LightFile.csv";
    CheckBox csvLight;
    GraphView graphAcc3;
    LineGraphSeries<DataPoint> seriesX;
    double graphLastXValTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_light, container, false);
        startStopBtnLight = view.findViewById(R.id.bStartStopLight);
        startStopBtnLight.setOnClickListener(this);
        sampleFreqSpinnerLight = view.findViewById(R.id.spinnerSampleFreqLight);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sampling_frequencies, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        sampleFreqSpinnerLight.setAdapter(adapter);
        csvLight = view.findViewById(R.id.csvBoxLight);
        csvLight.setEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsLight = getActivity().findViewById(R.id.detailsLight);
        tvCsvContent = getActivity().findViewById(R.id.tvSavedCsvFileLight);
        lightVal = getActivity().findViewById(R.id.valLight);
        lightVal.setText(getString(R.string.illuminanceEmpty, "--"));
        sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(sensorToBeListenedTo != null){
            if(Build.VERSION.SDK_INT >= 24)
                displaySensorDetailsWithStyle(sensorToBeListenedTo);
            else
                displaySensorDetailsWithoutStyle(sensorToBeListenedTo);
            setUpGraphView();
        }
        else{
            Toast.makeText(getActivity(), "Dein Gerät besitzt kein Sensor zur Messung der Beleuchtungsstärke!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }




    private void setUpGraphView(){
        graphAcc3 = (GraphView) getActivity().findViewById(R.id.graphLight);
        graphAcc3.getViewport().setYAxisBoundsManual(true);
        graphAcc3.getViewport().setMinY(0);
        graphAcc3.getViewport().setMaxY(3000);
        graphAcc3.getViewport().setMinX(0);
        graphAcc3.getViewport().setMaxX(50);
        graphAcc3.getViewport().setXAxisBoundsManual(true);
        graphAcc3.getLegendRenderer().setVisible(true);
        graphAcc3.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphAcc3.getLegendRenderer().setPadding(5);
        graphAcc3.getLegendRenderer().setTextSize(25);
        graphAcc3.getLegendRenderer().setMargin(30);
        graphAcc3.getGridLabelRenderer().setVerticalAxisTitle("lx");
        graphAcc3.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graphAcc3.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        seriesX = new LineGraphSeries<DataPoint>();
        seriesX.setColor(Color.BLUE);
        seriesX.setTitle("X");
        graphAcc3.addSeries(seriesX);
    }


    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sensorManager.unregisterListener(this);
        String buttonText = startStopBtnLight.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn)) == 0) {
            startStopBtnLight.setText(getResources().getString(R.string.start_listening_btn));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnLight.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        lightVal.setText(getString(R.string.illuminance, event.values[0]));
        if(csvLight.isChecked()) {
            saveFile(event.timestamp / 1000000 + " : " + "x: " + event.values[0] + "\n", true);
            //Toast.makeText(getActivity(), "" + readFile("ACCFile.csv"), Toast.LENGTH_SHORT).show();
        }
        seriesX.appendData(new DataPoint(graphLastXValTime, event.values[0] ), true, 1000);
        graphLastXValTime++;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopLight:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnLight.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {
                        String sampleFreq = sampleFreqSpinnerLight.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                        }
                        if(csvLight.isChecked())
                            saveFile("Zeit"+"                 " + "X-Achse in lx\n", false);
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnLight.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnLight.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    } else {
                        Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                        MainActivity.sensorManager.unregisterListener(this, sensor);
                        startStopBtnLight.setText(getResources().getString(R.string.start_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                        startStopBtnLight.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        if(csvLight.isChecked()) {
                            Toast.makeText(getActivity(), "Datei-Speicherort: " + getActivity().getFilesDir() + "/" + fileName, Toast.LENGTH_LONG).show();
                            tvCsvContent.setText(getFileContent(fileName));
                        }
                    }
                } else {
                    // Failure! Sensor not found on device.
                    Toast.makeText(getActivity(), "Dein Gerät besitzt keinen Sensor für die Beleuchtungsstärke!", Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    @TargetApi(24)
    private void displaySensorDetailsWithStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_light_withStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsLight.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    private void displaySensorDetailsWithoutStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_light_withoutStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsLight.setText(text);
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
