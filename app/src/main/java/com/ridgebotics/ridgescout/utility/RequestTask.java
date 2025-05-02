package com.ridgebotics.ridgescout.utility;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Function;

import javax.net.ssl.HttpsURLConnection;

public class RequestTask extends AsyncTask<String, String, String> {

    private Function<String, String> resultFunction = null;

    @Override
    protected String doInBackground(String... uri) {
        try {
            URL url = new URL(uri[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String[] headers = uri[1].split(", ");
            for(String header : headers){
                String[] split = header.split(": ");
                conn.setRequestProperty(split[0], split[1]);
            }
            if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK){
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                return readStream(conn.getInputStream());
                // Do normal input or output stream reading
            }
            else {
                return null; // See documentation for more info on response handling
            }
        } catch (IOException e) {
            AlertManager.error(e);
        }
        return null;
    }
    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            AlertManager.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    AlertManager.error(e);
                }
            }
        }
        return response.toString();
    }

    public void onResult(Function<String, String> func) {
        this.resultFunction = func;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(resultFunction != null){
            resultFunction.apply(result);
        }
    }
}