package com.example.sensordaten_sammler;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectionRest extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        String urlstring = "http://sbcon.ddns.net:3000/api/"+params[0];

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, params[1]);
            Request request = new Request.Builder()
                    .url(urlstring)
                    .post(body)
                    .build();
            Response response = null;

            try{
                response = client.newCall(request).execute();
                String resstr = response.body().string();
                Log.d("Response", resstr);
                //if(resstr.contains("REFERENCES")); Session.ID = -1;
            }
            catch(IOException e){
                e.printStackTrace();
            }

        return null;
   }
}
