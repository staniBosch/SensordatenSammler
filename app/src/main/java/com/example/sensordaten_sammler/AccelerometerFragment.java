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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AccelerometerFragment extends Fragment implements SensorEventListener, View.OnClickListener {

    Button startStopBtnAcc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        startStopBtnAcc = view.findViewById(R.id.bStartStopAcc);
        startStopBtnAcc.setOnClickListener(this);
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
        String buttonText = startStopBtnAcc.getText().toString();
        if (buttonText.compareTo(getResources().getString(R.string.stop_listening_btn)) == 0) {
            startStopBtnAcc.setText(getResources().getString(R.string.start_listening_btn));
            Drawable img = getContext().getResources().getDrawable( R.drawable.ic_play_arrow);
            startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tvXVal = null, tvYVal = null, tvZVal = null;
        View contentView = getView();
        if (contentView != null) {
            tvXVal = getActivity().findViewById(R.id.xValueAcc);
            tvYVal = getActivity().findViewById(R.id.yValueAcc);
            tvZVal = getActivity().findViewById(R.id.zValueAcc);
            if (tvXVal != null)
                tvXVal.setText(getString(R.string.x_valAcc, event.values[0]));
            if (tvYVal != null)
                tvYVal.setText(getString(R.string.y_valAcc, event.values[1]));
            if (tvZVal != null)
                tvZVal.setText(getString(R.string.z_valAcc, event.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bStartStopAcc:
                    if (MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                        // Success! The sensor exists on the device.
                        String buttonText = startStopBtnAcc.getText().toString();
                        if (buttonText.compareTo(getResources().getString(R.string.start_listening_btn)) == 0) {
                            Sensor sensorToBeListenedTo = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                            MainActivity.sensorManager.registerListener(this, sensorToBeListenedTo, SensorManager.SENSOR_DELAY_NORMAL);
                            startStopBtnAcc.setText(getResources().getString(R.string.stop_listening_btn));
                            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_stop);
                            startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        }
                        else {
                            Sensor sensor = MainActivity.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                            MainActivity.sensorManager.unregisterListener(this, sensor);
                            startStopBtnAcc.setText(getResources().getString(R.string.start_listening_btn));
                            Drawable img = getContext().getResources().getDrawable(R.drawable.ic_play_arrow);
                            startStopBtnAcc.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        }
                    } else {
                        // Failure! Sensor not found on device.
                        Toast.makeText(getActivity(), "Dein Gerät besitzt kein Accelerometer!", Toast.LENGTH_SHORT).show();
                    }

                break;
        }

    }
}
