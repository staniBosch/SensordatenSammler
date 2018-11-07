package com.example.sensordaten_sammler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

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

                    try {
                        Response  response = client.newCall(request).execute();
                        JSONObject js = new JSONObject(response.body().string());
                        int i = js.getInt("id");
                        Session.ID = i;
                        Log.d("Response", "Added Device: with id: "+i);


                        //Speichere lokal die SessionIDs ab
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

                        StringBuilder text = new StringBuilder();
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();

                        JSONObject jsonSession = new JSONObject(text.toString());

                        JSONArray jarray  = jsonSession.getJSONArray("session");
                        jarray.put(js);
                        //speichere alle Sessions lokal
                        FileOutputStream fos = ctx.openFileOutput(fname, Context.MODE_PRIVATE);
                        fos.write(jsonSession.toString().getBytes());
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

