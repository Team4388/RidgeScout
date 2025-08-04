package com.ridgebotics.ridgescout.ui.transfer;

import static com.ridgebotics.ridgescout.utility.FileEditor.baseDir;

import android.util.Log;

import com.ridgebotics.ridgescout.types.ColabArray;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.ridgebotics.ridgescout.utility.HttpGetFile;
import com.ridgebotics.ridgescout.utility.HttpPutFile;
import com.ridgebotics.ridgescout.utility.RequestTask;
import com.ridgebotics.ridgescout.utility.SettingsManager;
import com.ridgebotics.ridgescout.utility.ToDelete;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

// Class to synchronise data over HTTP.
public class HttpSync extends Thread {
    public static final String timestampsFilename = "timestamps";

    private static final long millisTolerance = 1000;

    private boolean after(Date a, Date b){
        return a.getTime() - b.getTime() > millisTolerance;
    }


    public interface onResult {
        void onResult(boolean error, int upCount, int downCount);
    }
    public interface UpdateIndicator {
        void onText(String text);
    }
    private static UpdateIndicator updateIndicator = text -> {};
    public static String text = "";
    private static void setUpdateIndicator(String m_text){
        text = m_text;
        updateIndicator.onText(m_text);
    }
    public static void setOnUpdateIndicator(UpdateIndicator m_updateIndicator){
        updateIndicator = m_updateIndicator;
    }

    private static onResult onResult = (error, upCount, downCount) -> {};
    public static void setOnResult(onResult result){
        onResult = result;
    }

    private static boolean isRunning = false;
    public static boolean getIsRunning(){return isRunning;}

    public static void sync(){
//        DataManager.reload_event();
        HttpSync sync = new HttpSync();

        sync.start();
    }

    private int upCount = 0;
    private int downCount = 0;

    private class TransferFile {
        public String filename;
        public Date updated;
        public String checksum;
    }

    private List<TransferFile> localFiles = new ArrayList<>();
    private List<TransferFile> remoteFiles = new ArrayList<>();



    private void await() {
        while(!runningRequest.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
    }


    AtomicBoolean runningRequest = new AtomicBoolean(false);

    public void run() {
        isRunning = true;
        boolean sendMetaFiles = SettingsManager.getFTPSendMetaFiles();

        ToDelete.reload_todelete_list();
        List<String> removeFiles = ToDelete.todelete_list.get();

        String serverIP = SettingsManager.getFTPServer();
        String serverKey = SettingsManager.getFTPKey();

        setUpdateIndicator("Getting Metadata...");

        // Load metadata from server
        getRemoteFileMetadata(serverIP, serverKey);

        if(!isRunning){
            setUpdateIndicator("Error Connecting");
            onResult.onResult(true, upCount, downCount);
            return;
        }


        getLocalFileMetadata();

        localFiles.removeIf(localFile -> removeFiles.contains(localFile.filename+","+localFile.checksum));
        remoteFiles.removeIf(remoteFile -> removeFiles.contains(remoteFile.filename+","+remoteFile.checksum));




        // Wait for metadata request to finish

        setUpdateIndicator("Uploading 0%");

        for(int i = 0; i < localFiles.size(); i++){
            TransferFile localFile = localFiles.get(i);

            TransferFile remoteFile = findInFileArray(remoteFiles, localFile.filename);


            boolean sendField = (sendMetaFiles || !(localFile.filename.endsWith(".fields")));

            boolean shouldUpload;
            boolean special;

            if(remoteFile == null) {
                shouldUpload = true;
                special = false;
            }
            else {
                boolean checksumsEqual = Objects.equals(localFile.checksum, remoteFile.checksum);
                special = FileEditor.requiresSpecialInteraction(remoteFile.filename);
                boolean after = after(localFile.updated, remoteFile.updated);

                shouldUpload = !checksumsEqual && (special || after);
            }

            if(sendField && shouldUpload) {
                uploadFile(localFile, serverIP, serverKey, special);
                Log.d(getClass().toString(), "LocalFile: " + localFile.filename + ", " + localFile.checksum + ", " + localFile.updated + ": Uploaded");
                upCount++;
            }else {
                Log.d(getClass().toString(), "LocalFile: " + localFile.filename + ", " + localFile.checksum + ", " + localFile.updated + ": Not uploaded");
            }

            setUpdateIndicator("Uploading " + (Math.floor((double) (i * 1000) / localFiles.size()) / 10) + "%");
        }

        setUpdateIndicator("Downloading 0%");

        for(int i = 0; i < remoteFiles.size(); i++){
            TransferFile remoteFile = remoteFiles.get(i);

            TransferFile localFile = findInFileArray(localFiles, remoteFile.filename);

            boolean shouldUpload;

            if(localFile == null) {
                shouldUpload = true;
            } else {
                boolean checksumsEqual = !Objects.equals(localFile.checksum, remoteFile.checksum);
                boolean after = after(remoteFile.updated, localFile.updated);
                boolean datesEqual = !localFile.updated.equals(remoteFile.updated);

                shouldUpload = (!checksumsEqual && (after) && !datesEqual);
            }

            if(shouldUpload) {
                downloadFile(remoteFile, serverIP);
//                await();
                Log.d(getClass().toString(), "RemoteFile: " + remoteFile.filename + ", " + remoteFile.checksum + ", " + remoteFile.updated + ": Downloaded");
                downCount++;
            } else {
                Log.d(getClass().toString(), "RemoteFile: " + remoteFile.filename + ", " + remoteFile.checksum + ", " + remoteFile.updated + ": Not downloaded");
            }


            setUpdateIndicator("Downloading " + (Math.floor((double) (i * 1000) / remoteFiles.size()) / 10) + "%");
        }


        ToDelete.deleteFiles();

        setUpdateIndicator("Finished, " + upCount + " Up, " + downCount + " Down");


        onResult.onResult(false, upCount, downCount);
        isRunning = false;
    }


    private TransferFile findInFileArray(List<TransferFile> files, String filename){
        for(TransferFile file : files) {
            if(file.filename.equals(filename))
                return file;
        }
        return null;
    }

    private Date getLocalFileUtcTimestamp(File file) {
        return new Date(file.lastModified());
    }

    private void getLocalFileMetadata() {
        File localDir = new File(baseDir);
        File[] localFileNames = localDir.listFiles();

        assert localFileNames != null;
        for (int i = 0; i < localFileNames.length; i++) {
            File file = localFileNames[i];

            if(file.isDirectory()) continue;
            // Remove timestamts file
            if(file.getName().equals(timestampsFilename)) continue;

            TransferFile tf = new TransferFile();
            tf.filename = file.getName();
            tf.updated = getLocalFileUtcTimestamp(file);
            try {
                tf.checksum = FileEditor.getSHA256Hash(file.getName());
            } catch (Exception e) {
                AlertManager.error("Failed to get hash of: " + file.getName(), e);
                continue;
            }
            localFiles.add(tf);
        }
    }

    // Send request to server and retrieve metadata
    private void getRemoteFileMetadata(String serverURL, String serverKey) {
        final RequestTask rq = new RequestTask();
        runningRequest.set(false);
        rq.onResult(metadata -> {
            try {
                JSONObject j = new JSONObject(metadata);
                for (Iterator<String> it = j.keys(); it.hasNext(); ) {
                    String key = it.next();

                    JSONObject obj = j.getJSONObject(key);

                    TransferFile tf = new TransferFile();
                    tf.filename = key;
                    tf.updated = new Date(Long.parseLong(obj.getString("modified")));
                    tf.checksum = obj.getString("sha256");

                    remoteFiles.add(tf);
                }
            }catch(JSONException | NullPointerException e ) {
                AlertManager.error(e);
                isRunning = false;
            }
            runningRequest.set(true);
            return  null;
        });
        rq.execute((serverURL + "/api/metadata"), "api_key: " + serverKey);
        await();
    }

    void uploadFile(TransferFile tf, String serverURL, String apiKey, boolean special) {
        runningRequest.set(false);

        if(special) {
            HttpGetFile getTask = new HttpGetFile(serverURL + "/api/" + tf.filename, new File(baseDir + tf.filename), (stream, error) -> {
                if(error != null) {
                    AlertManager.error(error);
                    return;
                } else if (stream == null) {
                    AlertManager.error("Output stream from download was null!");
                    return;
                }

                byte[] bytes = stream.toByteArray();

                FileEditor.syncColabArray(
                        tf.filename,
                        FileEditor.readFile(tf.filename),
                        bytes
                );


                HttpPutFile uploadTask = new HttpPutFile(serverURL + "/api/" + tf.filename, new File(baseDir + tf.filename), error2 -> {
                    if (error2 != null)
                        AlertManager.error(error2);
                    runningRequest.set(true);
                }, new String[]{
                        "api_key: " + apiKey,
                        ("modified: " + tf.updated.getTime())
                });

                uploadTask.execute();



            });

            getTask.execute();


        } else {

            HttpPutFile uploadTask = new HttpPutFile(serverURL + "/api/" + tf.filename, new File(baseDir + tf.filename), error -> {
                if (error != null)
                    AlertManager.error(error);
                runningRequest.set(true);
            }, new String[]{
                    "api_key: " + apiKey,
                    ("modified: " + tf.updated.getTime())
            }); // Pass auth token if needed

            uploadTask.execute();
            await();
        }
    }


    private void setLocalFileTimestamp(File file, Date date) {
        file.setLastModified(date.getTime());
    }
    void downloadFile(TransferFile tf, String serverURL) {
        runningRequest.set(false);
        File f = new File(baseDir + tf.filename);
        HttpGetFile uploadTask = new HttpGetFile(serverURL + "/api/" + tf.filename, f, (stream, error) -> {
            if(error != null) {
                AlertManager.error(error);
                return;
            } else if (stream == null) {
                AlertManager.error("Output stream from download was null!");
                return;
            }

            byte[] bytes = stream.toByteArray();

            if(FileEditor.requiresSpecialInteraction(tf.filename)) {
                FileEditor.syncColabArray(
                        tf.filename,
                        FileEditor.readFile(tf.filename),
                        bytes
                );
            } else {
                FileEditor.writeFile(tf.filename, bytes);
            }

            setLocalFileTimestamp(f, tf.updated);

            runningRequest.set(true);

        });

        uploadTask.execute();
        await();
    }
}
