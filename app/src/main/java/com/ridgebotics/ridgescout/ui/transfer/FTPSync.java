package com.ridgebotics.ridgescout.ui.transfer;

//import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.fileEditor.baseDir;

import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.utility.settingsManager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FTPSync extends Thread {
    public static final String remoteBasePath = "/RidgeScout/";
    public static final String timestampsFilename = "timestamps";



    public static long lastSyncTime = 0;
    private static Date curSyncTime;
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
        FTPSync ftpSync = new FTPSync();

        curSyncTime = new Date();

        ftpSync.start();
    }

    FTPClient ftpClient;

    private int upCount = 0;
    private int downCount = 0;

    private void downloadFile(String remoteFile, File localFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(localFile)) {
            ftpClient.retrieveFile(remoteBasePath + remoteFile, fos);
        }
    }

    private void uploadFile(File localFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(localFile)) {
            ftpClient.storeFile(remoteBasePath + localFile.getName(), fis);
        }
    }

    private FTPFile findRemoteFile(FTPFile[] remoteFiles, String fileName) {
        for (FTPFile file : remoteFiles) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    private Date getUtcTimestamp(FTPFile file) {
        return file.getTimestamp().getTime();
    }

    private Date getLocalFileUtcTimestamp(File file) {
        return new Date(file.lastModified());
    }

    private void setLocalFileTimestamp(File file, Date date) {
        file.setLastModified(date.getTime());
    }


    public void run() {
        isRunning = true;
        boolean sendMetaFiles = settingsManager.getFTPSendMetaFiles();

        // Meta files
        List<String> meta_string_array = Arrays.asList(
                "matches.fields",
                "pits.fields",
                evcode+".eventdata"
        );

        try {
            // Login to FTP
            ftpClient = new FTPClient();
            InetAddress address = InetAddress.getByName(settingsManager.getFTPServer());
            ftpClient.connect(address);
            ftpClient.login("anonymous", null);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            File localDir = new File(baseDir);
            File[] localFiles = localDir.listFiles();
            Map<String, Date> remoteTimestamps = getTimestamps();

            // Loop through local files and send all that are more recent
            if (localFiles != null) {
                for (int i = 0; i < localFiles.length; i++) {
                    File localFile = localFiles[i];
                    setUpdateIndicator("Uploading " + (i+1) + "/" + localFiles.length);

                    if(localFile.isDirectory()) continue;
                    // Remove timestamts file
                    if(localFile.getName().equals(timestampsFilename)) continue;
                    // Remove meta files if the option is disabled
                    if(!sendMetaFiles && meta_string_array.contains(localFile.getName())) continue;

                    Date remoteTimestamp = remoteTimestamps.get(localFile.getName());

                    Date localTimeStamp = getLocalFileUtcTimestamp(localFile);

                    if (remoteTimestamp == null || after(localTimeStamp, remoteTimestamp)) {
                        uploadFile(localFile);
                        System.out.println("Uploaded" + localFile.getName());

                        setLocalFileTimestamp(localFile, curSyncTime);
                        remoteTimestamps.put(localFile.getName(), curSyncTime);
                        upCount++;
                    }else{
                        System.out.println("Did not upload");
                    }
                }
            }

            Set<String> keySet = remoteTimestamps.keySet();
            Iterator<String> keyIt = keySet.iterator();
            for (int i = 0; i < keySet.size(); i++) {
                String remoteFile = keyIt.next();
                setUpdateIndicator("Downloading " + (i+1) + "/" + keySet.size());

                File localFile = new File(baseDir, remoteFile);
                if(remoteFile.equals(timestampsFilename)) continue;
                // Remove meta files if the option is disabled
                if(!sendMetaFiles && meta_string_array.contains(remoteFile)) continue;

//                    Date t1 = getLocalFileUtcTimestamp(localFile);
//                    Date t2 = getUtcTimestamp(remoteFile);
////
//                    System.out.println("- " + t1 + (t1.after(t2) ? ">" : "<") + t2);

                Date localTimeStamp = getLocalFileUtcTimestamp(localFile);
                Date remoteTimestamp = remoteTimestamps.get(remoteFile);



                if (!localFile.exists() || (after(remoteTimestamp, localTimeStamp) && !localTimeStamp.equals(remoteTimestamp))) {
                    downloadFile(remoteFile, localFile);

                    System.out.println("Downloaded " + localFile.getName());

                    if(!localFile.exists()) System.out.println("Not exist");
                    else if(after(remoteTimestamp, localTimeStamp)) System.out.println("Before: " + (localTimeStamp.getTime()-remoteTimestamp.getTime()));

//                        Date d = getUtcTimestamp(remoteFile);
                    setLocalFileTimestamp(localFile, remoteTimestamps.get(localFile.getName()));
//                    remoteTimestamps.put(remoteFile, curSyncTime);
                    downCount++;
                }else{
                    System.out.println("Did not download");
                }
            }

            setTimestamps(remoteTimestamps);

        } catch (Exception e) {
            AlertManager.error("Failed Syncing!", e);
            onResult.onResult(true, upCount, downCount);
            setUpdateIndicator("ERROR!");
        } finally {
            onResult.onResult(false, upCount, downCount);
            setUpdateIndicator("Finished");
        }

        isRunning = false;
    }

    private boolean setTimestamps(Map<String, Date> timestamps){
        try {
            ByteBuilder bb = new ByteBuilder();
            String[] filenames = timestamps.keySet().toArray(new String[0]);

            for(int i = 0; i < filenames.length; i++){
                bb.addString(filenames[i]);
                bb.addLong(timestamps.get(filenames[i]).getTime());
            }

            fileEditor.writeFile(timestampsFilename, bb.build());

            uploadFile(new File(baseDir + timestampsFilename));
            return true;
        } catch (ByteBuilder.buildingException | IOException e) {
            AlertManager.error("Failed Syncing!", e);
            return false;
        }
    }

    private Map<String, Date> getTimestamps() {
        try {
            downloadFile(timestampsFilename, new File(baseDir + timestampsFilename));

            byte[] data = fileEditor.readFile(timestampsFilename);

            if(data == null || data.length == 0)
                return new HashMap<>();

            BuiltByteParser bbp = new BuiltByteParser(data);
            List<BuiltByteParser.parsedObject> pa = bbp.parse();

            Map<String, Date> output = new HashMap<>();
            for(int i = 0; i < pa.size(); i+=2){
//                System.out.println((long) pa.get(i).get());
                output.put(
                        (String) pa.get(i).get(),
                        new Date((long) pa.get(i+1).get())
                );
            }
            return output;

        }catch (IOException | BuiltByteParser.byteParsingExeption e){
            AlertManager.error("Failed Syncing!", e);
            return new HashMap<>();
        }
    }
}
