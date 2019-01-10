package com.example.sensordaten_sammler;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectionRest extends AsyncTask<String, Void, Object> {

    private Consumer<JSONArray> fun;
    ConnectionRest (Consumer<JSONArray> c){
        this.fun = c;
    }
    ConnectionRest (){
        super();
    }

    @Override
    protected Object doInBackground(String... params) {
        String urlstring = "http://sbcon.ddns.net:3000/api/"+params[0];
        OkHttpClient client = new OkHttpClient();
        Request request;
        //POST
        if(params.length>1){
            Log.d("XRESTPOST",urlstring+params[1]);
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, params[1]);
            request = new Request.Builder()
                    .url(urlstring)
                    .post(body)
                    .build();
        }//GET
        else{
            Log.d("XRESTGET",urlstring);
            request = new Request.Builder()
                    .url(urlstring)
                    .build();
        }
        Response response;
        try{
            response = client.newCall(request).execute();
            //Log.d("Response", response.body().string());
            if(response.body()!=null)
                return new JSONArray(response.body().string());
            else return response;
        }
        catch(Exception e){
            if(params.length>1)
                Log.d("XRESTError", "couldnt send :"+params[1]);
            else
                Log.d("XRESTError", "couldnt get :"+urlstring);
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
