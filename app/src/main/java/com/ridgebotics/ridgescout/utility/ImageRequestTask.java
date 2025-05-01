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
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (FileNotFoundException e) {
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