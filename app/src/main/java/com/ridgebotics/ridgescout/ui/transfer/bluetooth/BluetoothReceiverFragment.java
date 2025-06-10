package com.ridgebotics.ridgescout.ui.transfer.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentTransferBluetoothReceiverBinding;
import com.ridgebotics.ridgescout.types.ScoutingFile;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.FileEditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

// Class to receive bluetooth transmissions from other devices
public class BluetoothReceiverFragment extends Fragment {
    private BluetoothReceiver bluetoothReceiver;
    private Button startListeningButton;
    private Button stopListeningButton;
    private TextView statusTextView;



//    private void alert(String title, String content) {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
//        dialog.setCancelable(true);
//        dialog.setTitle(title);
//        dialog.setMessage(content);
//
//        final AlertDialog alert = dialog.create();
//        alert.show();
//
//    }

    FragmentTransferBluetoothReceiverBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {

        binding = FragmentTransferBluetoothReceiverBinding.inflate(inflater, container, false);


//        bluetoothReceiver = new BluetoothReceiver(context);

        bluetoothReceiver = new BluetoothReceiver(getContext(), new BluetoothReceiver.receivedData() {
            @Override
            public void processReceivedData(byte[] data, int bytes) {
                receiveData(data, bytes);
            }

            @Override
            public void onConnectionStop() {
                finished_recieve();
            }
        });

        startListeningButton = binding.startListeningButton;
        stopListeningButton = binding.stopListeningButton;
        statusTextView = binding.statusTextView;

        if (!bluetoothReceiver.isBluetoothSupported()) {
            AlertManager.addSimpleError("Bluetooth is not supported on this device");
            return binding.getRoot();
        }

        if (!bluetoothReceiver.isBluetoothEnabled()) {
            AlertManager.addSimpleError("Please enable Bluetooth");
        }

        startListeningButton.setOnClickListener(v -> {
            startListening();
        });

        stopListeningButton.setOnClickListener(v -> {
            stopListening();
        });

        return binding.getRoot();
    }

    private void startListening() {
        try {
            bluetoothReceiver.startListening();
            statusTextView.setText("Listening for incoming connections...");
            startListeningButton.setEnabled(false);
            stopListeningButton.setEnabled(true);

            recievedBytes = new ArrayList<>();

        } catch (IOException e) {
            AlertManager.error("Failed to start listening", e);
        }
    }

    private void stopListening() {
        try {
            bluetoothReceiver.stopListening();
            statusTextView.setText("Not listening");
            startListeningButton.setEnabled(true);
            stopListeningButton.setEnabled(false);
        } catch (IOException e) {
            AlertManager.error("Failed to stop listening: " + e.getMessage(), e);
        }
    }

    private List<byte[]> recievedBytes;

    private void receiveData(byte[] data, int bytes) {
        byte[] newBytes = FileEditor.getByteBlock(data, 0, bytes);
        System.out.println("Recieved " + bytes + " Bytes over bluetooth!");
        recievedBytes.add(newBytes);
    }


    private void finished_recieve() {
        String result_filenames = "";
        try {

            byte[] resultBytes = FileEditor.combineByteArrays(recievedBytes);
            resultBytes = FileEditor.blockUncompress(resultBytes);


            BuiltByteParser bbp = new BuiltByteParser(resultBytes);
            ArrayList<BuiltByteParser.parsedObject> result = bbp.parse();

            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getType() != ScoutingFile.typecode) continue;
                ScoutingFile f = ScoutingFile.decode((byte[]) result.get(i).get());

                if (f != null) {
                    System.out.println(f.filename);
                    if (f.write())
                        result_filenames += f.filename + "\n";
                }
            }

        } catch (DataFormatException e) {
            AlertManager.error(e);
        } catch (BuiltByteParser.byteParsingExeption e) {
            AlertManager.error(e);
        }

        AlertManager.alert("Completed!", result_filenames);
    }


    @Override
    public void onDestroy() {
        if (bluetoothReceiver != null)
            try {
                bluetoothReceiver.stopListening();
            } catch (IOException e) {
                AlertManager.error(e);
            }
        super.onDestroy();
    }
}