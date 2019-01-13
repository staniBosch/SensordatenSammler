package com.example.sensordaten_sammler.rest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.sensordaten_sammler.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class Data2ServerTask  extends BroadcastReceiver {

    // Connection flag
    protected Boolean connected;

    /**
     * Public constructor
     */
    public Data2ServerTask() {
        connected = null;
    }

    /**
     *
     * @param context  Context - Application context
     * @param intent  Intent - Manages application actions on network state changes
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null || intent.getExtras() == null) return;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if((ni != null) && ni.isConnected()) {
            data2Server();
        } else {
            connected = false;
        }

    }
    private void data2Server(){
        //new ConnectionRest().execute();
    }
    public void data2Local(String url, String jsonObj){

        try{
            //read From file JSONARRAY if not exist create
            JSONArray jsonarrmain = new JSONArray();
            //----

            //look up if array is not empty or session_id exist
            JSONObject jsonSession = new JSONObject();

            if(jsonarrmain.length()>1)
                for(int i = 0; i < jsonarrmain.length(); i++)
                    if(Session.getID() == jsonarrmain.getJSONObject(i).getInt("session_id"))
                        jsonSession = jsonarrmain.getJSONObject(i);
            //otherwise create
            if(jsonSession.length()<1)
                jsonSession.put("session_id", Session.getID());
            JSONArray sendArray = new JSONArray();


            JSONObject sendObj = new JSONObject();

            sendObj.put("url", url);
            sendObj.put("data", jsonObj);

            sendArray.put(sendObj);

            jsonSession.put("data_arr", sendArray);
            jsonarrmain.put(jsonSession);

        } catch (Exception e){

        }

    }


}
