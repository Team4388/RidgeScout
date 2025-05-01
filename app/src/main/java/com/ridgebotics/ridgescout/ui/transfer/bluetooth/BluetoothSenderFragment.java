package com.ridgebotics.ridgescout.ui.transfer.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentTransferBluetoothSenderBinding;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

// Class to send scouting data over bluetooth.
public class BluetoothSenderFragment extends Fragment {
    private BluetoothSender bluetoothSender;
    private ListView deviceListView;
    private Button sendFileButton;
    private ArrayAdapter<String> deviceArrayAdapter;
    private ArrayList<BluetoothDevice> deviceList;

    private FragmentTransferBluetoothSenderBinding binding;

    private static byte[] data_to_send = new byte[0];
    public static void set_data(byte[] data){
        data_to_send = data;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTransferBluetoothSenderBinding.inflate(inflater, container, false);

        bluetoothSender = new BluetoothSender(getContext());
        deviceListView = binding.deviceListView;
        sendFileButton = binding.sendFileButton;

        deviceList = new ArrayList<>();
        deviceArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceArrayAdapter);

        if (!bluetoothSender.isBluetoothSupported()) {
            AlertManager.toast("Bluetooth is not supported on this device");
            return binding.getRoot();
        }

        if (!bluetoothSender.isBluetoothEnabled()) {
            AlertManager.toast("Please enable Bluetooth");
        } else {
            listPairedDevices();
        }

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice selectedDevice = deviceList.get(position);
                try {
                    bluetoothSender.connectToDevice(selectedDevice);
                    AlertManager.toast("Connected to " + selectedDevice.getName());
                    sendFileButton.setEnabled(true);
                } catch (IOException e) {
                    AlertManager.toast("Failed to connect: " + e.getMessage());
                }
            }
        });

        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData();
            }
        });

        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    private void listPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothSender.getPairedDevices();
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            deviceList.addAll(pairedDevices);
            for (BluetoothDevice device : pairedDevices) {
                deviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            AlertManager.toast("No paired devices found");
        }
    }

    private void sendData() {
        try {
            byte[] compressed = FileEditor.blockCompress(data_to_send, FileEditor.lengthHeaderBytes);

            for(int i = 0; i < Math.ceil((double) compressed.length/1024); i++){
                bluetoothSender.sendData(FileEditor.getByteBlock(compressed, i*1024, (i+1)*1024));
            }

            bluetoothSender.close();
            sendFileButton.setEnabled(false);



            AlertManager.toast("File sent successfully");
        } catch (IOException e) {
            AlertManager.toast("Failed to send file: " + e.getMessage());
        }
    }

    public void onDestroy() {
        if (bluetoothSender != null)
            try {
                bluetoothSender.close();
            } catch (IOException e) {
                AlertManager.error(e);
            }
        super.onDestroy();
    }
}