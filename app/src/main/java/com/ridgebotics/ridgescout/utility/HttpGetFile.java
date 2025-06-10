package com.ridgebotics.ridgescout.utility;

import android.os.AsyncTask;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetFile extends AsyncTask<Void, Integer, File> {

    public interface DownloadCallback {
        void onResult(String error);
    }

    private String downloadUrl;
    private File destinationFile;
    private DownloadCallback callback;
    private String errorMessage;
    public HttpGetFile(String downloadUrl, File destinationFile, DownloadCallback callback) {
        this.downloadUrl = downloadUrl;
        this.destinationFile = destinationFile;
        this.callback = callback;
    }

    @Override
    protected File doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();

            // Configure connection for GET request
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(60000); // 60 seconds

            connection.connect();

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                String errorResponse = readErrorResponse(connection);
                errorMessage = "Download failed. Response code: " + responseCode +
                        (errorResponse != null ? ". Error: " + errorResponse : "");
                return null;
            }

            // Get file size for progress tracking
            long fileSize = connection.getContentLengthLong();
            if (fileSize == -1) {
                fileSize = connection.getContentLength(); // fallback for older API
            }

            inputStream = connection.getInputStream();

            // Create destination file and directories if needed
            if (destinationFile.getParentFile() != null && !destinationFile.getParentFile().exists()) {
                if (!destinationFile.getParentFile().mkdirs()) {
                    errorMessage = "Failed to create destination directory: " + destinationFile.getParentFile().getAbsolutePath();
                    return null;
                }
            }

            outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[8192];
            long downloadedBytes = 0;
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (isCancelled()) {
                    deletePartialFile();
                    return null;
                }

                outputStream.write(buffer, 0, bytesRead);
                downloadedBytes += bytesRead;

                // Update progress if file size is known
                if (fileSize > 0) {
                    int progress = (int) ((downloadedBytes * 100) / fileSize);
                    publishProgress(progress);
                }
            }

            outputStream.flush();
//            Log.d(TAG, "Download successful. File saved to: " + destinationFile.getAbsolutePath());
            return destinationFile;

        } catch (Exception e) {
            AlertManager.error(e);
            errorMessage = "Download error: " + e.getMessage();
//            Log.e(TAG, errorMessage, e);
            deletePartialFile();
            return null;
        } finally {
            closeResources(inputStream, outputStream, connection);
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (callback != null) {
            callback.onResult(errorMessage);
        }
    }

    @Override
    protected void onCancelled() {
        deletePartialFile();
        if (callback != null) {
            callback.onResult("Download cancelled");
        }
    }

    private String readErrorResponse(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
        } catch (IOException e) {
            AlertManager.error(e);
//            Log.e(TAG, "Error reading error response", e);
        }
        return null;
    }

    private void deletePartialFile() {
        if (destinationFile != null && destinationFile.exists()) {
            if (destinationFile.delete()) {
//                Log.d(TAG, "Partial download file deleted");
            } else {
//                Log.w(TAG, "Failed to delete partial download file");
            }
        }
    }

    private void closeResources(InputStream inputStream, OutputStream outputStream, HttpURLConnection connection) {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            AlertManager.error(e);
//            Log.e(TAG, "Error closing input stream", e);
        }

        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException e) {
            AlertManager.error(e);
//            Log.e(TAG, "Error closing output stream", e);
        }

        if (connection != null) {
            connection.disconnect();
        }
    }
}