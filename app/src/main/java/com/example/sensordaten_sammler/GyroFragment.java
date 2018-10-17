package com.example.sensordaten_sammler;

import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GyroFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnGyro;
    Spinner sampleFreqSpinnerGyro;

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
        return view;
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
            Drawable img = getContext().getResources().getDrawable( R.drawable.ic_play_arrow);
            startStopBtnGyro.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tvXVal = null, tvYVal = null, tvZVal = null;
        View contentView = getView();
        if (contentView != null) {
            tvXVal = getActivity().findViewById(R.id.xValueGyro);
            tvYVal = getActivity().findViewById(R.id.yValueGyro);
            tvZVal = getActivity().findViewById(R.id.zValueGyro);
            if (tvXVal != null)
                tvXVal.setText(getString(R.string.x_valGyro, event.values[0]));
            if (tvYVal != null)
                tvYVal.setText(getString(R.string.y_valGyro, event.values[1]));
            if (tvZVal != null)
                tvZVal.setText(getString(R.string.z_valGyro, event.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bStartStopGyro:
                if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
                    // Success! The sensor exists on the device.
                    String buttonText = startStopBtnGyro.getText().toString();
                    if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {
                        Sensor sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                        String sampleFreq = sampleFreqSpinnerGyro.getSelectedItem().toString();
                        int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        if(sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[0])){
                            sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                        }
                        else if(sampleFreq.equals(getResources().getStringArray(R.array.sampling_frequencies)[1])){
                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                        }
                        MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, sensorDelay);
                        startStopBtnGyro.setText(getResources().getString(R.string.stop_listening_btn));
                        Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                        startStopBtnGyro.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    }
                    else {
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
}
