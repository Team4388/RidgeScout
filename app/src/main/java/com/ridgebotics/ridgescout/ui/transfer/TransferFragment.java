package com.ridgebotics.ridgescout.ui.transfer;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.R;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.settingsManager;
import com.ridgebotics.ridgescout.databinding.FragmentTransferBinding;
import com.ridgebotics.ridgescout.ui.transfer.bluetooth.BluetoothSenderFragment;
import com.ridgebotics.ridgescout.ui.transfer.codes.CodeGeneratorView;

import java.util.Date;

public class TransferFragment extends Fragment {
    private FragmentTransferBinding binding;

//    private enum TransferTypes {
//        CAMERA,
//        BLUETOOTH,
//        LOCAL_WIFI,
//        SCOUTING_SERVER
//    }

    String evcode;

    private static final int background_color = 0x5000ff00;
    private static final int unselected_background_color = 0x2000ff00;

//    private Bundle b;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

//        b = savedInstanceState;

        binding = FragmentTransferBinding.inflate(inflater, container, false);

        evcode = settingsManager.getEVCode();

        binding.downloadButton.setOnClickListener(v -> {
            start_download();
        });

        binding.TBAButton.setOnClickListener(v -> {
            binding.noEventError.setVisibility(View.GONE);
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Warning");
            alert.setMessage("This action requires internet.");
            alert.setCancelable(true);

            alert.setPositiveButton("Ok", (dialog, which) -> {
                findNavController(this).navigate(R.id.action_navigation_transfer_to_navigation_tba);
            });

            alert.setNegativeButton("Cancel", null);
            alert.create().show();
        });


        if(!settingsManager.getWifiMode()) {
            binding.TBAButton.setEnabled(false);
            binding.SyncButton.setEnabled(false);
        }

        if(!settingsManager.getFTPEnabled() ||
            new Date().getTime()-FTPSync.lastSyncTime < FTPSync.timeTolerance) {
            binding.SyncButton.setEnabled(false);
        }

        binding.SyncButton.setOnClickListener(v -> {
            binding.SyncButton.setEnabled(false);
            FTPSync.sync((error, upcount, downcount) -> getActivity().runOnUiThread(() -> {
//                binding.SyncButton.setEnabled(true);
                AlertManager.toast((!error ? "Synced! " : "Error Syncing. ") + upcount + " Up " + downcount + " Down");
            }));
        });


        if(evcode.equals("unset")){
            binding.noEventError.setVisibility(View.VISIBLE);
            binding.uploadButton.setEnabled(false);
            binding.CSVButton.setEnabled(false);
            binding.downloadButton.setEnabled(true);
            return binding.getRoot();
        }

        binding.uploadButton.setOnClickListener(v -> {
            start_upload();
        });

        binding.CSVButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Chose data");

            builder.setNegativeButton("Pit data", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CSVExport.exportPits(getContext());
                }
            });

            builder.setPositiveButton("Match data", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CSVExport.exportMatches(getContext());
                }
            });

            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        });

        return binding.getRoot();
    }



    private void start_upload() {
        FileSelectorFragment.setOnSelect(data -> {
            TransferSelectorFragment.setOnSelect(new TransferSelectorFragment.onSelect() {
                @Override
                public void onSelectCodes(TransferSelectorFragment self) {
                    CodeGeneratorView.setData(data);
                    findNavController(self).navigate(R.id.action_navigation_transfer_selector_to_navigation_code_generator);
                }
                @Override
                public void onSelectBluetooth(TransferSelectorFragment self) {
                    BluetoothSenderFragment.set_data(data);
                    findNavController(self).navigate(R.id.action_navigation_transfer_selector_to_navigation_bluetooth_sender);
                }
                @Override
                public void onSelectFileBundle(TransferSelectorFragment self) {
                    FileBundle.send(data, getContext());
                }
            });
            findNavController(this).navigate(R.id.action_navigation_file_selector_to_navigation_transfer_selector);
        });
        findNavController(this).navigate(R.id.action_navigation_transfer_to_navigation_file_selector);
    }




    private void start_download(){

        TransferSelectorFragment.setOnSelect(new TransferSelectorFragment.onSelect() {
            @Override
            public void onSelectCodes(TransferSelectorFragment self) {
                findNavController(self).navigate(R.id.action_navigation_transfer_selector_to_navigation_code_scanner);
            }

            @Override
            public void onSelectBluetooth(TransferSelectorFragment self) {
                findNavController(self).navigate(R.id.action_navigation_transfer_selector_to_navigation_bluetooth_receiver);
            }

            @Override
            public void onSelectFileBundle(TransferSelectorFragment self) {
                FileBundle.receive(getActivity());
            }
        });
        findNavController(this).navigate(R.id.action_navigation_transfer_to_navigation_transfer_selector);
    }

}