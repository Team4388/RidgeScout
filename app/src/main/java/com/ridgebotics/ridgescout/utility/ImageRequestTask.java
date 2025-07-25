package com.ridgebotics.ridgescout.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Function;

import javax.net.ssl.HttpsURLConnection;

// Class to retrieve team icon from TBA

// https://stackoverflow.com/questions/37510411/download-an-image-into-bitmap-file-in-android
public class ImageRequestTask extends AsyncTask<String, Void, Bitmap> {

    Function<Bitmap, Void> resultFunction = null;
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // We do a little bit of spoofing
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setDoInput(true);
            connection.connect();

            int code = connection.getResponseCode();
            switch (code) {
                case 200:
                    InputStream input = connection.getInputStream();
                    return BitmapFactory.decodeStream(input);
                case 403:
//                    AlertManager.error("Got 403, Going to https://www.thebluealliance.com/avatars may fix this");
                    return null;
                default:
                    AlertManager.error("Error downloading image " + src, "Got response code: " + code);
                    return null;
            }
        } catch (FileNotFoundException e){
            return null;
        } catch (IOException e){
            AlertManager.error("Error downloading image " + src, e);
            return null;
        }
    }


    @Override
    protected Bitmap doInBackground(String... params) {
        return getBitmapFromURL(params[0]);
    }

    public void onResult(Function<Bitmap, Void> func) {
        this.resultFunction = func;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if(resultFunction != null){
            resultFunction.apply(result);
        }
    }

}