package com.ridgebotics.ridgescout.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AlertManager {
    public static Context context;

    public static void init(Context c){
        context = c;
    }

    public static void alert(String title, String content) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage(content);
                alert.setTitle(title);
                alert.setPositiveButton("OK", null);
                alert.setCancelable(true);

                alert.create().show();
            }
        });
    }

    public static void toast(String content) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, content, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void error(String content) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage(content);
                alert.setTitle("Error!");
                alert.setPositiveButton("OK", null);
                alert.setCancelable(true);

                alert.create().show();
            }
        });
    }

    public static void error(Exception e) {
        e.printStackTrace();
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));

                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage(sw.toString());
                alert.setTitle(e.getMessage());
                alert.setPositiveButton("OK", null);
                alert.setCancelable(true);

                alert.create().show();
            }
        });
    }

}
