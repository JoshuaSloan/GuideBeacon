package com.gimbal.hello_gimbal_android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class BackgroundTask extends AsyncTask< String , Void , String> {
    Context ctx;
    public AsyncResponse delegate = null;
    BackgroundTask(Context ctx){
        this.ctx=ctx;
    }


    @Override
    protected String doInBackground(String... params) {

        String login_url="http://wsumapping.cs.wichita.edu/login.php";
        Log.d("emad","Login");
        String response = "",line = "";
        String login_name = params[1];
        String login_pass = params[2];
        try {
            URL url = new URL((login_url));
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
            String data = URLEncoder.encode("login_name", "UTF-8")+"="+ URLEncoder.encode(login_name, "UTF-8")+"&"+
                    URLEncoder.encode("login_pass", "UTF-8")+"="+ URLEncoder.encode(login_pass, "UTF-8");
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            while((line=bufferedReader.readLine())!=null){
                response+=line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            return response;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }


}