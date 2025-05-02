package com.ridgebotics.ridgescout.ui.transfer.bluetooth;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ridgebotics.ridgescout.utility.AlertManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothReceiver {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BluetoothReceiverApp";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    private InputStream inputStream;
    private boolean listening = false;
    private receivedData receiveddata;

    public BluetoothReceiver(Context context, receivedData receiveddata) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.receiveddata = receiveddata;
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    public static void
    requestBluetoothPermissions(Activity activity) {
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 and above
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
        } else {
            // For Android 11 and below
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
//                }
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    public static boolean hasBluetoothPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        } else {
            boolean hasBasicPermissions = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                return hasBasicPermissions && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//            } else {
                return hasBasicPermissions;
//            }
        }
    }

    @SuppressLint("MissingPermission")
    public void startListening() throws IOException {
        if(!hasBluetoothPermissions(context)){
            requestBluetoothPermissions((Activity) context);
            return;
        }
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        listening = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (listening) {
                    try {
                        socket = serverSocket.accept();
                        inputStream = socket.getInputStream();
                        // Handle incoming data here
                        handleIncomingData();
                    } catch (IOException e) {
                        AlertManager.error(e);
                    }
                }
            }
        }).start();
    }

    private void handleIncomingData() throws IOException  {
        byte[] buffer = new byte[1024];
        int bytes;
        try {
            while (true) {
                bytes = inputStream.read(buffer);
                if (bytes != -1) {
                    receiveddata.processReceivedData(buffer, bytes);
                }
            }
        } catch (IOException e) {
        if (e.getMessage() != null && e.getMessage().contains("bt socket closed, read return: -1")) {
            receiveddata.onConnectionStop();
            System.out.println("Bluetooth socket closed, treating as end of stream");
        } else {
            throw e;
        }
    }
    }

    public interface receivedData {
        public void processReceivedData(byte[] data, int bytes);
        public void onConnectionStop();
    }



    public void stopListening() throws IOException {
        listening = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (socket != null) {
            socket.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }
}