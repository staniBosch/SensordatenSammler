package com.example.sensordaten_sammler;

import android.hardware.Sensor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class ListofAllAvailableSensorsFragment extends Fragment {

    private static final String TAG = "SensorList";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_listview, container, false);

        List<Sensor> deviceSensors = MainActivity.sensorManager.getSensorList(Sensor.TYPE_ALL);
        ListView listView = (ListView) view.findViewById(R.id.list_of_all_sensors);
        listView.setAdapter(new MySensorsAdapter(getActivity(), R.layout.row_item, deviceSensors));

        return view;
    }
}
