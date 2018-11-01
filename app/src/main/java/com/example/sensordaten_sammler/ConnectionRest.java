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

public class ConnectionRest extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        String urlstring = "http://sbcon.ddns.net:3000/api/"+params[0];
        String body = params[1];
        try{
            URL url = new URL(urlstring);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            //conn.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
            writer.write(body);
            writer.flush();
            writer.close();
            outputStream.close();
            InputStream inputStream;
            if(conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST){
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }
            conn.disconnect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp;
            while((temp = bufferedReader.readLine())!=null){
                Log.d("Ergebnis", temp);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
   }
}
