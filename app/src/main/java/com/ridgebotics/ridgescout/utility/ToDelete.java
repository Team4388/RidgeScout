package com.ridgebotics.ridgescout.utility;

import static android.widget.LinearLayout.VERTICAL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ridgebotics.ridgescout.types.ColabArray;

import java.util.ArrayList;
import java.util.List;

public class ToDelete {
    public static final String filename = "todelete.colabarray";

    public static void findCorruptedFiles(Context c) {
        new Thread(() -> {
            AlertManager.startLoading("Loading files...");
            List<String> filenames = FileEditor.findCorruptedFiles();
            AlertManager.stopLoading();
            ((Activity) c).runOnUiThread(() -> {
                deleteFiles(c, filenames, true);
            });
        }).start();
    }

    public static void deleteFiles(Context c, List<String> files, boolean defaultOption) {
        ScrollView sv = new ScrollView(c);
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(VERTICAL);
        sv.addView(ll);

        CheckBox[] checkboxes = new CheckBox[files.size()];

        for(int i =0; i < files.size(); i++){
            CheckBox cb = new CheckBox(c);
            cb.setText(files.get(i));
            cb.setChecked(defaultOption);
            ll.addView(cb);
            checkboxes[i] = cb;
        }


        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle("Delete files");
        alert.setView(sv);
        alert.setNeutralButton("Cancel", null);
        alert.setPositiveButton("Delete", (_dialogInterface, _i) -> {
            List<String> delete_files = new ArrayList<>();
            for(int i = 0; i < files.size(); i++) {
                if(checkboxes[i].isChecked())
                    delete_files.add(files.get(i));
            }


            AlertDialog.Builder confirm = new AlertDialog.Builder(c);
            alert.setTitle("Confirm");
            TextView tv = new TextView(c);
            tv.setText("Are you sure you want to delete " + delete_files.size() + " files?");
            alert.setView(tv);
            alert.setNeutralButton("Cancel", null);
            alert.setPositiveButton("Delete", (dialogInterface, i) -> {
                deleteFiles(delete_files);
            });
            alert.setCancelable(false);
            alert.create().show();
        });
        alert.setCancelable(false);
        alert.create().show();
    }

    public static ColabArray todelete_list = new ColabArray();
    public static void reload_todelete_list(){
        if(!FileEditor.fileExist(ToDelete.filename)) {todelete_list = new ColabArray(); return;}
        byte[] file = FileEditor.readFile(ToDelete.filename);
        if(file == null) {todelete_list = new ColabArray(); return;}

        try {
            todelete_list = ColabArray.decode(file);
        } catch (Exception e){
            AlertManager.error("Error loading todelete list", e);
            todelete_list = new ColabArray();
        }
    }

    public static void save_todelete_list() {
        try {
            FileEditor.writeFile(ToDelete.filename, todelete_list.encode());
        } catch (Exception e){
            AlertManager.error("Error saving todelete list", e);
        }
    }

    private static void deleteFiles(List<String> toDelete) {
        reload_todelete_list();

        for(String file : toDelete) {

            String hash;
            try {
                hash = FileEditor.getSHA256Hash(file);
            } catch (Exception e) {
                AlertManager.error("Failed to get hash of file: " + file, e);
                continue;
            }

            todelete_list.add(file+","+hash);

            FileEditor.deleteFile(file);
        }

        save_todelete_list();
    }

    public static void deleteFiles() {
        reload_todelete_list();
        List<String> toDelete = todelete_list.get();
        for(String filename : FileEditor.getFiles()){
            try {
                String hash = FileEditor.getSHA256Hash(filename);

                if(toDelete.contains(filename+","+hash)) {
                    FileEditor.deleteFile(filename);
                }

            } catch (Exception e) {
                AlertManager.error("Failed to get hash of file: " + filename, e);
                continue;
            }
        }
    }

    public static boolean contains(String localfile) {
        try {
            String hash = FileEditor.getSHA256Hash(localfile);
            return contains(localfile, hash);
        } catch (Exception e) {
            AlertManager.error("Failed to get hash of file: " + localfile, e);
            return false;
        }
    }

    public static boolean contains(String filename, String hash){
        return todelete_list.contains(filename+","+hash);
    }
}
