package com.example.sensordaten_sammler;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Session {

    private static int ID =-1;

    public static int getID(){

        if(Session.ID<0){

            JSONObject data = new JSONObject();
            try{
                data.put("value", android.os.Build.MODEL);
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    String urlstring = "http://sbcon.ddns.net:3000/api/session";

                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(JSON,params[0]);
                    Request request = new Request.Builder()
                            .url(urlstring)
                            .post(body)
                            .build();
                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                        int i = new JSONObject(response.body().string()).getInt("id");
                        Session.ID = i;
                        Log.d("Response", "Added Device: with id: "+i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute(data.toString());
        }

        return Session.ID;
    }


    }

