package com.example.sensordaten_sammler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Session {

    private static int ID =-1;

    public static int getID(Context ctx){

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
                        JSONObject js = new JSONObject(response.body().string());
                        int i = js.getInt("id");
                        Session.ID = i;
                        Log.d("Response", "Added Device: with id: "+i);

                        //lesen und jsonobj erstellen
                        String sessionjson = "sessionID.json";
                        StringBuffer datax = new StringBuffer("");
                       File f = new File(ctx.getFilesDir(), sessionjson);
                       if(!f.exists()) f.createNewFile();
                       else {
                           FileInputStream fis = ctx.openFileInput(sessionjson);
                           InputStreamReader isr = new InputStreamReader(fis);
                           BufferedReader bfr = new BufferedReader(isr);
                           String readString = bfr.readLine();
                           while (readString != null) {
                               datax.append(readString);
                               readString = bfr.readLine();
                           }

                           isr.close();
                           //

                           //JSONArray jsonArray = new JSONArray(readString);
                           Log.d("JSONREADED OMGGG", readString);


                       }

                        FileOutputStream fos = ctx.openFileOutput(sessionjson,ctx.MODE_APPEND);
                        fos.write(js.toString().getBytes());
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }.execute(data.toString());
        }

        return Session.ID;
    }

    public static int getID(){
        return Session.ID;
    }

    }

