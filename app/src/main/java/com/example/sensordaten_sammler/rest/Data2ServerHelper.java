package com.example.sensordaten_sammler.rest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.example.sensordaten_sammler.MainActivity;
import com.example.sensordaten_sammler.Session;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Data2ServerHelper extends BroadcastReceiver {

    // Connection flag
    protected Boolean connected;

    /**
     * Public constructor
     */
    public Data2ServerHelper() {
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
           new AsyncTaskCheck(this).execute();
        } else {
            connected = false;
        }

    }
    private void data2Server(){
        //List of the POST JSON OBJECTS
        ArrayList<String> arrlist = getArrayListFile("tmpData", "");
        if(arrlist!=null)
        try {
            //falls es was zu senden gibt
            while (arrlist.size() > 0) {
                arrlist = getArrayListFile("tmpData", "");
                int id;
                JSONObject json2Send;
                JSONArray jsonarr2Send = new JSONArray();
                ArrayList<String> newTmpData = new ArrayList<>();


                //hole das erste object
                json2Send = new JSONObject(arrlist.get(0));
                jsonarr2Send.put(json2Send);
                id = json2Send.getInt("session_id");
                //und alle nachfolgenden mit der selben id
                for(int i=1;i<arrlist.size();i++){
                    JSONObject j2SendTemp = new JSONObject(arrlist.get(i));
                    if(j2SendTemp.getInt("session_id")== id )
                        jsonarr2Send.put(j2SendTemp);
                    else
                        newTmpData.add(arrlist.get(i));
                }
                //schreibe die neue tmp datei ohne die zu verschickten daten
                FileOutputStream fos = MainActivity.ctx.openFileOutput("tmpData", Context.MODE_PRIVATE);
                for(int i = 0; i< newTmpData.size();i++) {
                    fos.write(newTmpData.get(i).getBytes());
                    fos.write("\n".getBytes());
                }
                fos.close();

                //sonderfall:
                //falls die "id" die aktuelle ist und negativ(offline) holle eine online id und setze alle session_id's zur online id
                //id<0 es wird eine online Session id benötigt
                if(id<0) {
                    //falls es die gerade genutzte id ist ändere alle daten in die online id
                    if (id == Session.getID()) {
                        Session.createIDnonAsync(MainActivity.ctx);
                        for (int i = 0; i < jsonarr2Send.length(); i++) {
                            jsonarr2Send.getJSONObject(i).put("session_id", Session.getID());
                            jsonarr2Send.getJSONObject(i).getJSONObject("post").getJSONObject("data").put("session_id", Session.getID());
                        }
                    }
                    else{
                        String urlstring = "http://sbcon.ddns.net:3000/api/lokalapp/session";
                        JSONObject data = new JSONObject();
                        data.put("value", "unknown");
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = RequestBody.create(JSON,data.toString());
                        Request request = new Request.Builder()
                                .url(urlstring)
                                .post(body)
                                .build();
                        Response response = client.newCall(request).execute();
                        JSONObject js = new JSONObject(response.body().string());
                        int session_id = js.getInt("id");
                        for (int i = 0; i < jsonarr2Send.length(); i++) {
                            jsonarr2Send.getJSONObject(i).put("session_id", session_id);
                            jsonarr2Send.getJSONObject(i).getJSONObject("post").getJSONObject("data").put("session_id", session_id);
                        }
                    }
                }
                for(int i = 0; i< jsonarr2Send.length(); i++){
                    new ConnectionRest().execute(jsonarr2Send.getJSONObject(i).getJSONObject("post").getString("url"),jsonarr2Send.getJSONObject(i).getJSONObject("post").getJSONObject("data").toString());
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public void data2Local(String url, String jsonObj){

        try{
            //file name is tmpData.json
            String fname = "tmpData";

            // create the structure of the JSONObject-> {session_id,post:{url,data}}
            JSONObject jsonSession = new JSONObject();
            jsonSession.put("session_id",Session.getID());

            JSONObject sendObj = new JSONObject();
            sendObj.put("url", url);
            sendObj.put("data", new JSONObject(jsonObj));
            jsonSession.put("post", sendObj);

            //schreibe die json file
            FileOutputStream fos = MainActivity.ctx.openFileOutput(fname, Context.MODE_APPEND);
            fos.write(jsonSession.toString().getBytes());
            fos.write("\n".getBytes());
            fos.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param fname name of file to read or create as JSONARR
     * @return a list of each line that was read or created
     */
    public static ArrayList<String> getArrayListFile(String fname, String content){
        File f = new File(MainActivity.ctx.getFilesDir(), fname);
        if(!f.exists())
            try {
                f.createNewFile();
                FileOutputStream fos = MainActivity.ctx.openFileOutput(fname, Context.MODE_PRIVATE);
                fos.write(content.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //lesen und jsonobj erstellen
        try {
           ArrayList<String> textarr = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;

            while ((line = br.readLine()) != null) {
                textarr.add(line);
            }
            br.close();
            return textarr;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     *
     * @param fname name of file to read or create as JSONARR
     * @return JSONARRAY that was read or created
     */
    public static JSONArray rcJsonFile(String fname, String content){
        File f = new File(MainActivity.ctx.getFilesDir(), fname);
        if(!f.exists())
            try {
                f.createNewFile();
                FileOutputStream fos = MainActivity.ctx.openFileOutput(fname, Context.MODE_PRIVATE);
                fos.write(content.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //lesen und jsonobj erstellen
        try {
            StringBuilder textarr = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;

            while ((line = br.readLine()) != null) {
                textarr.append(line);
            }
            br.close();
            return new JSONArray(textarr.toString());

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

   static class AsyncTaskCheck extends AsyncTask<Void, Void, Void> {
        Data2ServerHelper d2S;
        AsyncTaskCheck(Data2ServerHelper data2ServerTask){
            this.d2S = data2ServerTask;
        }
       @Override
       protected Void doInBackground(Void... voids) {
           try {
               int timeoutMs = 1500;
               Socket sock = new Socket();
               SocketAddress sockaddr = new InetSocketAddress("sbcon.ddns.net", 3000);

               sock.connect(sockaddr, timeoutMs);
               sock.close();

               this.d2S.data2Server();
           } catch (IOException e) {
               this.d2S.connected = false;
           }
           return null;
       }
   }

}
