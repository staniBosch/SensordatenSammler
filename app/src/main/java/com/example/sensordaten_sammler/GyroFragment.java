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

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GyroFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnGyro;
    Spinner sampleFreqSpinnerGyro;
    TextView tvXVal, tvYVal, tvZVal, tvAllDetailsGyro;
    Sensor sensorToBeListenedTo;
    String fileName = "GyroFile.csv";
    CheckBox csvGyro;
    Switch switchsv;

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
        saveFile("Zeit"+"," + "rad/s" + "," + "rad/s" + ","+ "rad/s"+"\n");
        switchsv = view.findViewById(R.id.switchsv);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsGyro = getActivity().findViewById(R.id.detailsGyro);
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
        }
        else{
            Toast.makeText(getActivity(), "Dein Gerät besitzt kein Gyroskop!", Toast.LENGTH_SHORT).show();
        }
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
        if(csvGyro.isChecked()) {
            saveFile(System.currentTimeMillis()+"," + event.values[0] + "," + event.values[1] + "," + event.values[2]+"\n");
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
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                        }
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
