package com.ridgebotics.ridgescout.utility;

import android.os.AsyncTask;
//import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPutFile extends AsyncTask<Void, Integer, Boolean> {

//    private static final String TAG = "FileUploadTask";

    public interface UploadCallback {
        void onResult(String error);
    }

    private String uploadUrl;
    private File fileToUpload;
    private UploadCallback callback;
    private String errorMessage;
    private String[] headers;

    public HttpPutFile(String uploadUrl, File fileToUpload, UploadCallback callback, String[] headers) {
        this.uploadUrl = uploadUrl;
        this.fileToUpload = fileToUpload;
        this.callback = callback;
        this.headers = headers;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        InputStream fileInputStream = null;
        OutputStream outputStream = null;

        try {
            if (!fileToUpload.exists()) {
                errorMessage = "File does not exist: " + fileToUpload.getAbsolutePath();
                return false;
            }

            URL url = new URL(uploadUrl);
            connection = (HttpURLConnection) url.openConnection();

            // Configure connection for PUT request
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Content-Length", String.valueOf(fileToUpload.length()));
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(60000); // 60 seconds

            for(int i = 0; i < headers.length; i++){
                String[] split = headers[i].split(": ");
                connection.setRequestProperty(split[0], split[1]);
            }
            connection.connect();

            outputStream = connection.getOutputStream();
            fileInputStream = new FileInputStream(fileToUpload);

            byte[] buffer = new byte[8192];
            long totalBytes = fileToUpload.length();
            long uploadedBytes = 0;
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                if (isCancelled()) {
                    return false;
                }

                outputStream.write(buffer, 0, bytesRead);
                uploadedBytes += bytesRead;

                // Update progress
                int progress = (int) ((uploadedBytes * 100) / totalBytes);
                publishProgress(progress);
            }

            outputStream.flush();

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
//                Log.d(TAG, "Upload successful. Response code: " + responseCode);
                return true;
            } else {
                // Read error response if available
                String errorResponse = readErrorResponse(connection);
                errorMessage = "Upload failed. Response code: " + responseCode +
                        (errorResponse != null ? ". Error: " + errorResponse : "");
                return false;
            }

        } catch (Exception e) {
            AlertManager.error(e);
            errorMessage = "Upload error: " + e.getMessage();
//            Log.e(TAG, errorMessage, e);
            return false;
        } finally {
            closeResources(fileInputStream, outputStream, connection);
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (callback != null) {
            callback.onResult(errorMessage);
        }
    }

    @Override
    protected void onCancelled() {
        if (callback != null) {
            callback.onResult("Upload cancelled");
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