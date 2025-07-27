package com.ridgebotics.ridgescout.ui.transfer.codes;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ridgebotics.ridgescout.databinding.FragmentTransferCodeSenderBinding;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ridgebotics.ridgescout.utility.TaskRunner;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Class to show the code transfer thing.
public class CodeGeneratorView extends Fragment {
    private ImageView qrImage;
    private SeekBar qrSpeedSlider;
    private SeekBar qrSizeSlider;
    private TextView qrIndexN;
    private TextView qrIndexD;

    private static final int maxQrCount = 256; //The max number that can be stored in a byte

    private static final int maxQrSize = 800;


    private static final int maxQrSpeed = 50;
    private static final int minQrSpeed = 1000;
    private static final int defaultQrDelay = 12;



    private int minQrSize = 0;
    private int qrSize = 200;

    private double qrDelay = 0;
    private int qrIndex = 0;

    private CountDownTimer timer;
    private int qrCount = 0;

    private List<Bitmap> qrBitmaps = new ArrayList<>();

    private FragmentTransferCodeSenderBinding binding;

    private static byte[] data;
    public static void setData(String data){
        setData(data.getBytes(StandardCharsets.ISO_8859_1));
    }
    public static void setData(byte[] tmpdata){
        data = tmpdata;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentTransferCodeSenderBinding.inflate(inflater, container, false);

        qrImage = binding.qrImage;
        qrSpeedSlider = binding.qrSpeedSlider;
        qrSizeSlider = binding.qrSizeSlider;
        qrIndexN = binding.qrIndexN;
        qrIndexD = binding.qrIndexD;

        String compressed = new String(FileEditor.blockCompress(data, FileEditor.lengthHeaderBytes), StandardCharsets.ISO_8859_1);

        if(compressed.isEmpty()){
            AlertManager.alert("Error!", "Empty data!");
            return binding.getRoot();
        }

        minQrSize = Math.round((float)compressed.length() / maxQrCount)+1;
        qrSize += minQrSize;

        sendData(compressed);

        qrSpeedSlider.setMax(maxQrSpeed*2);
        qrSpeedSlider.setProgress(maxQrSpeed + defaultQrDelay);

        qrSizeSlider.setMax(maxQrSize-minQrSize);
        qrSizeSlider.setProgress(qrSize-minQrSize);

        startLoop();

        return binding.getRoot();
    }

    private void sendData(String data){
        qrCount = (data.length()/qrSize)+1;
        qrIndexD.setText(String.valueOf(qrCount));

        qrSpeedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qrDelay = ((double) progress /maxQrSpeed) - 1;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        qrSizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                qrSize = seekBar.getProgress() + minQrSize;
//                qrCount = (int)Math.ceil((double) (data.length()+1)/qrSize);
                qrCount = ((data.length()+1)/qrSize) +1;
                qrIndexD.setText(String.valueOf(qrCount));
                sendData(data);
            }
        });

        AlertManager.startLoading("Generating codes...");

        new TaskRunner().executeAsync(new CodeGenTask(data, new Random().nextInt(255), qrSize, qrCount), result -> {
            qrBitmaps = result;
            AlertManager.stopLoading();
            qrIndex = 0;
        });

    }

    private void updateQr(){
        if(qrBitmaps.isEmpty())
            return;

        qrImage.setImageBitmap(qrBitmaps.get(qrIndex));
        if(qrDelay > 0) {
            this.qrIndex += 1;
            if (this.qrIndex >= this.qrCount) {
                this.qrIndex = 0;
            }
        }else{
            this.qrIndex -= 1;
            if (this.qrIndex < 0) {
                this.qrIndex = this.qrCount-1;
            }
        }

        qrIndexN.setText(String.valueOf(qrIndex+1));
    }

    private void startLoop() {


        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    updateQr();
                }
                catch (Exception e) {
                    AlertManager.error(e);
                }
                finally{
                    double a = ((double) maxQrSpeed) / (Math.abs(qrDelay));
                    a = Math.min(Math.max(a, maxQrSpeed), minQrSpeed);
                    handler.postDelayed(this, (long) a);
                }
            }
        };

        handler.post(runnable);
    }
}
