package com.example.sensordaten_sammler;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SessionFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_listview, container, false);

        List<JSONObject> sessions = this.getSessionJsonArrayList();

        ListView listView = (ListView) view.findViewById(R.id.list_of_all_sensors);

        listView.setAdapter(new MySessionAdapter(getActivity(), R.layout.row_item, sessions));

        return view;
    }

    public List<JSONObject> getSessionJsonArrayList(){
        Context ctx = getContext();
        String fname = "sessionID.json";
        File f = new File(ctx.getFilesDir(), fname);
        if(!f.exists()) try {
            f.createNewFile();
            FileOutputStream fos = ctx.openFileOutput(fname, Context.MODE_PRIVATE);
            fos.write("{\"session\":[]}".getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //lesen und jsonobj erstellen
        ArrayList<JSONObject> jsArrayObj = new ArrayList<>();
        try {
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();

            JSONObject jsonSession = new JSONObject(text.toString());

            JSONArray jarray = jsonSession.getJSONArray("session");



            for (int j = 0; j < jarray.length(); j++)
                jsArrayObj.add((JSONObject) jarray.get(j));

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return jsArrayObj;
    }

    private class MySessionAdapter extends ArrayAdapter<JSONObject> {

        private int textViewResourceId;

        private class ViewHolder {
            private TextView itemView;
        }

        public MySessionAdapter(Context context, int textViewResourceId, List<JSONObject> items) {
            super(context, textViewResourceId, items);
            this.textViewResourceId = textViewResourceId;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext()).inflate(textViewResourceId, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.itemView = (TextView) convertView.findViewById(R.id.content);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            JSONObject item = getItem(getCount()-position-1);

            if (item != null) {
                try {
                    if(Integer.parseInt(item.get("id").toString()) == Session.getID())
                        viewHolder.itemView.setText("SessionID: " + item.get("id")+" --current Session--");
                    else
                        viewHolder.itemView.setText("SessionID: " + item.get("id"));
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }

            return convertView;
        }
    }
}

