package com.ridgebotics.ridgescout.ui.transfer;

//import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.DataManager.evcode;
import static com.ridgebotics.ridgescout.utility.fileEditor.baseDir;

import com.ridgebotics.ridgescout.ui.data.FieldEditorHelper;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.ByteBuilder;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.ridgebotics.ridgescout.utility.settingsManager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class FTPSync extends Thread {
    public static final String remoteBasePath = "/RidgeScout/";
    public static final String timestampsFilename = "timestamps";
    public static long lastSyncTime = 0;

    private static Date curSyncTime;

    public interface onResult {
        void onResult(boolean error, int upCount, int downCount);
    }

    public onResult onResult;

    public static void sync(onResult onResult){
//        DataManager.reload_event();
        FTPSync ftpSync = new FTPSync();
        ftpSync.onResult = onResult;

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
                for (File localFile : localFiles) {
                    if(localFile.isDirectory()) continue;
                    // Remove timestamts file
                    if(localFile.getName().equals(timestampsFilename)) continue;
                    // Remove meta files if the option is disabled
                    if(!sendMetaFiles && meta_string_array.contains(localFile.getName())) continue;

                    Date remoteTimestamp = remoteTimestamps.get(localFile.getName());

                    if (remoteTimestamp == null || getLocalFileUtcTimestamp(localFile).after(remoteTimestamp)) {
                        uploadFile(localFile);
                        System.out.println("Uploaded " + localFile.getName());

                        setLocalFileTimestamp(localFile, curSyncTime);
                        remoteTimestamps.put(localFile.getName(), curSyncTime);
                        upCount++;
                    }else{
                        System.out.println("Did not upload " + localFile.getName());
                    }
                }
            }

            for (String remoteFile : remoteTimestamps.keySet()) {
                File localFile = new File(baseDir, remoteFile);
                if(remoteFile.equals(timestampsFilename)) continue;
                // Remove meta files if the option is disabled
                if(!sendMetaFiles && meta_string_array.contains(remoteFile)) continue;

//                    Date t1 = getLocalFileUtcTimestamp(localFile);
//                    Date t2 = getUtcTimestamp(remoteFile);
////
//                    System.out.println("- " + t1 + (t1.after(t2) ? ">" : "<") + t2);

                if (!localFile.exists() || remoteTimestamps.get(remoteFile).after(getLocalFileUtcTimestamp(localFile))) {
                    downloadFile(remoteFile, localFile);
                    System.out.println("Downloaded " + localFile.getName());
//                        Date d = getUtcTimestamp(remoteFile);
                    setLocalFileTimestamp(localFile, remoteTimestamps.get(localFile.getName()));
//                    remoteTimestamps.put(remoteFile, curSyncTime);
                    downCount++;
                }else{
                    System.out.println("Did not download " + remoteFile);
                }
            }

            setTimestamps(remoteTimestamps);

        } catch (Exception e) {
            AlertManager.error(e);
            onResult.onResult(true, upCount, downCount);
        } finally {
            onResult.onResult(false, upCount, downCount);
        }
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
            e.printStackTrace();
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
            AlertManager.error(e);
            return new HashMap<>();
        }
    }
}
