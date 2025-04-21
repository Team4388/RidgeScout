package com.ridgebotics.ridgescout.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

// Class to cause alerts and popups
public class AlertManager {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static void init(Context c){
        context = c;
    }

    private static AlertDialog currentError;
    private static final List<String> simpleErrorList = new ArrayList<>();
    private static final List<String> errorList = new ArrayList<>();

    public static void alert(String title, String content) {

            ((Activity) context).runOnUiThread(() -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle(title);
                alert.setMessage(content);
                alert.setPositiveButton("OK", null);
                alert.setCancelable(true);

                alert.create().show();
            });
    }

    public static void toast(String content) {
        ((Activity) context).runOnUiThread(() -> Toast.makeText(context, content, Toast.LENGTH_LONG).show());
    }

    public static void addSimpleError(String error) {
        simpleErrorList.add(error);
        updateErrors();
    }

    public static void error(String content) {
        errorList.add(content);
        updateErrors();
    }

    public static void error(String title, String content) {
        simpleErrorList.add(title);
        errorList.add(content);
        updateErrors();
    }

    public static void error(Exception e) {
        e.printStackTrace();
//        simpleErrorList.add(e.getMessage());

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        errorList.add(sw.getBuffer().toString());
        updateErrors();
    }

    public static void error(String title, Exception e) {
        simpleErrorList.add(title);

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));

        errorList.add(sw.toString());
        e.printStackTrace();
        updateErrors();
    }

    public static void updateErrors(){
        ((Activity) context).runOnUiThread(() -> {
            if(currentError != null && currentError.isShowing()){
                DialogInterface tmp = currentError;
                currentError = null;
                tmp.dismiss();
            }

            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            if(!simpleErrorList.isEmpty())
                alert.setTitle(simpleErrorList.get(0) + (simpleErrorList.size() > 1 ? "..." : ""));
            else
                alert.setTitle(errorList.size() + " Error" + (errorList.size() != 1 ? "s" : "") + "!");

            if(simpleErrorList.size() > 1)
                alert.setMessage(String.join("\n", simpleErrorList));

            alert.setPositiveButton("OK", (dialogInterface, i) -> {if(currentError != null){errorList.clear(); simpleErrorList.clear();}});

            String detailedErrors = String.join("\n\n\n\n\n", errorList);

            if(!errorList.isEmpty())
                alert.setNeutralButton("View Detailed Error" + (errorList.size() != 1 ? "s" : ""), (dialogInterface, i) -> alert("Details", detailedErrors));

//            alert.setOnDismissListener((x) -> {if(currentError != null){errorList.clear(); simpleErrorList.clear();}});

            alert.setCancelable(true);
            currentError = alert.create();
            currentError.show();
        });
    }

}
