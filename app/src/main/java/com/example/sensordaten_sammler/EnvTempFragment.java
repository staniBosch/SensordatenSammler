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
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sensordaten_sammler.rest.ConnectionRest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class EnvTempFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnTemp;
    Spinner sampleFreqSpinnerTemp;
    TextView tempVal, tvAllDetailsTemp;
    Sensor sensorToBeListenedTo;
    CheckBox csvEnv;
    Switch saveswitch;
    String fileName = "EnvFile.csv";
    Timer timer = new Timer();
    double value;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_env_temp, container, false);
        startStopBtnTemp = view.findViewById(R.id.bStartStopTemp);
        startStopBtnTemp.setOnClickListener(this);
        sampleFreqSpinnerTemp = view.findViewById(R.id.spinnerSampleFreqTemp);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sampling_frequencies, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        sampleFreqSpinnerTemp.setAdapter(adapter);
        csvEnv = view.findViewById(R.id.csvBoxEnv);
        csvEnv.setEnabled(true);
        saveswitch = view.findViewById(R.id.switchsvenvt);
        saveFile("Zeit"+"," + "Umgebungstemperatur" +"\n");


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAllDetailsTemp = getActivity().findViewById(R.id.detailsTemp);
        tempVal = getActivity().findViewById(R.id.tempValue);
        tempVal.setText(getString(R.string.ambient_tempEmpty, "--"));
        sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if(sensorToBeListenedTo != null){
            if(Build.VERSION.SDK_INT >= 24)
                displaySensorDetailsWithStyle(sensorToBeListenedTo);
            else
                displaySensorDetailsWithoutStyle(sensorToBeListenedTo);
        }
        else{
            Toast.makeText(getActivity(), "Dein Gerät besitzt keinen Sensor für die Umgebungstemperatur!", Toast.LENGTH_SHORT).show();
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
                                data.put("value", value);
                                data.put("session_id", Session.getID());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            new ConnectionRest().execute("umgebungstemperatur", data.toString());
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
        String buttonText = startStopBtnTemp.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn)) == 0) {
            startStopBtnTemp.setText(getResources().getString(R.string.start_listening_btn));
            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
            startStopBtnTemp.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        tempVal.setText(getString(R.string.ambient_temp, event.values[0]));
        if(csvEnv.isChecked()) {
            saveFile(System.currentTimeMillis()+"," + event.values[0] +"\n");
            //Toast.makeText(getActivity(), "" + readFile("ACCFile.csv"), Toast.LENGTH_SHORT).show();
        }
        value = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartStopTemp:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnTemp.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {  // Benutzer hat Start gedrückt
                        String sampleFreq = sampleFreqSpinnerTemp.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        } else if (sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])) {
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                        }
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnTemp.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnTemp.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    } else {  // Benutzer hat Stop gedrückt
                        Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
                        MainActivity.sensorManager.unregisterListener(this, sensor);
                        startStopBtnTemp.setText(getResources().getString(R.string.start_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                        startStopBtnTemp.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    }
                } else {
                    // Failure! Sensor not found on device.
                    Toast.makeText(getActivity(), "Dein Gerät besitzt keinen Sensor für die Umgebungstemperatur!", Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    @TargetApi(24)
    private void displaySensorDetailsWithStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_ambient_temp_withStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsTemp.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    private void displaySensorDetailsWithoutStyle(Sensor sensorToBeListenedTo) {
        String text = getString(R.string.details_textfield_ambient_temp_withoutStyle, sensorToBeListenedTo.getName()
                , sensorToBeListenedTo.getVendor(), sensorToBeListenedTo.getVersion(), sensorToBeListenedTo.getPower(),
                sensorToBeListenedTo.getResolution(), sensorToBeListenedTo.getMaximumRange());
        tvAllDetailsTemp.setText(text);
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
