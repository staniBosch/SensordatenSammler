package com.example.sensordaten_sammler;

import android.annotation.TargetApi;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ProximityFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnProximity;
    Spinner sampleFreqSpinnerProximity;
    TextView proximityValue, tvAllDetailsProximity;
    CheckBox csvProx;
    Sensor sensorToBeListenedTo;
    String fileName = "ProxFile.csv";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proximity, container, false);
        startStopBtnProximity = view.findViewById(R.id.bStartStopProximity);
        startStopBtnProximity.setOnClickListener(this);
        sampleFreqSpinnerProximity = view.findViewById(R.id.spinnerSampleFreqProximity);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sampling_frequencies, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        sampleFreqSpinnerProximity.setAdapter(adapter);
        csvProx = view.findViewById(R.id.csvBoxProx);
        csvProx.setEnabled(true);
        saveFile("Zeit"+"," + "Proximity"+ "\n");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsProximity = getActivity().findViewById(R.id.detailsProximity);
        proximityValue = getActivity().findViewById(R.id.valProximity);
        proximityValue.setText(getString(R.string.proximityEmpty, "--"));
        sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if(Build.VERSION.SDK_INT >= 24)
            displaySensorDetailsWithStyle(sensorToBeListenedTo);
        else
            displaySensorDetailsWithoutStyle(sensorToBeListenedTo);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.sensorManager.unregisterListener(this);
        String buttonText = startStopBtnProximity.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn)) == 0) {
            startStopBtnProximity.setText(getResources().getString(R.string.start_listening_btn));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnProximity.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        proximityValue.setText(getString(R.string.proximity, event.values[0]));
        if(csvProx.isChecked()) {
            saveFile(System.currentTimeMillis()+"," + event.values[0] + "\n");
            //Toast.makeText(getActivity(), "" + readFile("ACCFile.csv"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopProximity:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnProximity.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {
                        String sampleFreq = sampleFreqSpinnerProximity.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                        }
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnProximity.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnProximity.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    } else {
                        Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                        MainActivity.sensorManager.unregisterListener(this, sensor);
                        startStopBtnProximity.setText(getResources().getString(R.string.start_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                        startStopBtnProximity.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    }
                } else {
                    // Failure! Sensor not found on device.
                    Toast.makeText(getActivity(), "Dein Gerät besitzt keinen Proximity-Sensor!", Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    @TargetApi(24)
    private void displaySensorDetailsWithStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_proximity_withStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsProximity.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    private void displaySensorDetailsWithoutStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_proximity_withoutStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsProximity.setText(text);
    }
    public void saveFile(String text)
    {

        try {
            FileOutputStream fos = getActivity().openFileOutput(fileName,getActivity().MODE_APPEND);
            fos.write(text.getBytes());
            fos.close();
            //Toast.makeText(getActivity(), "Gespeichert!", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
    public String readFile(String file)
    {
        String text ="";

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
