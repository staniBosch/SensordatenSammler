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

    private static int ID =0;
    public static int createID(Context ctx){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                JSONObject js;
                try {
                    //POST SESSION
                    String urlstring = "http://sbcon.ddns.net:3000/api/lokalapp/session";
                    JSONObject data = new JSONObject();
                    data.put("value", android.os.Build.MODEL);
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(JSON, data.toString());
                    Request request = new Request.Builder()
                            .url(urlstring)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    js = new JSONObject(response.body().string());
                    int i = js.getInt("id");
                    Session.ID = i;
                    Log.d("XREST", "Added Device: with id: " + i);
                } catch (Exception e) {
                    Session.ID = -1;
                }
                try {//Speichere lokal die SessionIDs ab

                    String fname = "sessionID.json";
                    File f = new File(ctx.getFilesDir(), fname);
                    if (!f.exists())
                        try {
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
                    JSONArray jarray = jsonSession.getJSONArray("session");

                    if (Session.ID < 1)
                        for (int i = 0; i < jarray.length(); i++)
                            if (Session.ID >= jarray.getJSONObject(i).getInt("id"))
                                Session.ID = jarray.getJSONObject(i).getInt("id") - 1;

                    js = new JSONObject("{'id':" + Session.ID + "}");
                    if (js != null)
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
            }.execute();
            return Session.ID;
        }


    public static void createIDnonAsync(Context ctx){
        JSONObject js;
        try {
            //POST SESSION
            String urlstring = "http://sbcon.ddns.net:3000/api/lokalapp/session";
            JSONObject data = new JSONObject();
            data.put("value", android.os.Build.MODEL);
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON,data.toString());
            Request request = new Request.Builder()
                    .url(urlstring)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.body()!=null) {
                js = new JSONObject(response.body().string());
                int i = js.getInt("id");
                Session.ID = i;
                Log.d("XREST", "Added Device: with id: " + i);
            }else
                Log.d("RESTXERROR", "response body empty");
        } catch(Exception e){
            Session.ID = -1;
        }
        try{//Speichere lokal die SessionIDs ab

            String fname = "sessionID.json";
            File f = new File(ctx.getFilesDir(), fname);
            if(!f.exists())
                try {
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

            Log.d("XSESSION",jarray.toString());
            if(Session.ID < 0)
                for(int i = 0; i<jarray.length();i++)
                    if(Session.ID >= jarray.getJSONObject(i).getInt("id"))
                        Session.ID = jarray.getJSONObject(i).getInt("id")-1;

            js = new JSONObject("{'id':"+Session.ID+"}");
            jarray.put(js);
            //speichere alle Sessions lokal
            FileOutputStream fos = ctx.openFileOutput(fname, Context.MODE_PRIVATE);
            fos.write(jsonSession.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getID(){
        return Session.ID;
    }

    }

