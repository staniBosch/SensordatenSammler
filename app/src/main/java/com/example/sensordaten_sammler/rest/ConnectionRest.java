package com.example.sensordaten_sammler.rest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectionRest extends AsyncTask<String, Void, Object> {

    private Consumer<JSONArray> fun;
    public ConnectionRest (Consumer<JSONArray> c){
        this.fun = c;
    }
    public ConnectionRest (){
        super();
    }

    @Override
    protected Object doInBackground(String... params) {
        String urlstring;
        if(params[0].contains("sbcon"))
            urlstring = params[0];
        else
            urlstring = "http://sbcon.ddns.net:3000/api/"+params[0];
        OkHttpClient client = new OkHttpClient();
        Request request;
        //POST
        if(params.length>1){
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, params[1]);
            request = new Request.Builder()
                    .url(urlstring)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if(response.body()!=null){
                    JSONObject responseJSON = new JSONObject(response.body().string());
                    Log.d("XRESTPOST", urlstring + params[1]);
                    return responseJSON;
                }
                else return null;
            } catch (Exception e){
                new Data2ServerHelper().data2Local(urlstring, params[1]);
                Log.d("XRESTError", "couldnt send :"+params[1]+" errmsg: "+e.getMessage());
            }
        }//GET
        else{
            request = new Request.Builder()
                    .url(urlstring)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d("XRESTGET", urlstring);
                if(response.body()!=null)
                    return new JSONArray(response.body().string());
                else return response;
            } catch (Exception e){
                Log.d("XRESTError", "couldnt get :"+urlstring);
                //if(this.fun!=null)
                //    this.fun.accept(null);
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(Object jsonarrdata) {
        super.onPostExecute(jsonarrdata);
        if(this.fun!=null)
            this.fun.accept((JSONArray) jsonarrdata);
    }
}
