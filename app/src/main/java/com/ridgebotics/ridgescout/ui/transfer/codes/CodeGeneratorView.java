package com.ridgebotics.ridgescout.ui.transfer.codes;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

// Class to show the code transfer thing.
public class CodeGeneratorView extends Fragment {
    private ImageView qrImage;
    private SeekBar qrSpeedSlider;
    private SeekBar qrSizeSlider;
    private TextView qrIndexN;
    private TextView qrIndexD;

    private final int maxQrCount = 256; //The max number that can be stored in a byte

    private final int maxQrSpeed = 5;
    private final int minQrSpeed = 300 + maxQrSpeed - 1;

    private int minQrSize = 0;
    private final int maxQrSize = 800;
    private int qrSize = 200;

    private final int defaultQrDelay = 419;
    private int qrDelay = 0;
    private int qrIndex = 0;

    private CountDownTimer timer;
    private int qrCount = 0;

    private ArrayList<Bitmap> qrBitmaps;

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

        qrSizeSlider.setMax(maxQrSize-minQrSize);
        qrSpeedSlider.setMax((minQrSpeed-maxQrSpeed)*2);

        qrSizeSlider.setProgress(minQrSize+qrSize);
        qrSpeedSlider.setProgress(defaultQrDelay+5);

        sendData(compressed);

        return binding.getRoot();
    }


    private Bitmap generateQrCode(String contents) throws WriterException {

        final int size = 512;

        if (contents == null) {
            return null;
        }

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);

        // The Charset must be UTF-8, Or data will not be transferred properly. IDK why.
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
//        hints.put(EncodeHintType.);
        hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
        MultiFormatWriter writer = new MultiFormatWriter();

        BitMatrix result;
        try {
            result = writer.encode(contents, BarcodeFormat.DATA_MATRIX, size, size, hints);
        } catch (IllegalArgumentException e) {
            // Unsupported format
            AlertManager.error(e);
            return null;
        }

        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }


    private void sendData(String data){



        qrCount = (data.length()/qrSize)+1;
        qrIndexD.setText(String.valueOf(qrCount));

//        alert("size", ""+binding.qrSizeSlider.getProgress()+"\n"+binding.qrSizeSlider.getMax());

        qrSpeedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qrDelay = -(minQrSpeed - progress - maxQrSpeed + 1);
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

//        qrSizeSlider.setProgress(qr);

        qrBitmaps = new ArrayList<>();

        int randID = new Random().nextInt(255);

        for(int i=0;i<=((data.length()+1)/qrSize);i++){
            final int start = i*qrSize;
            int end = (i+1)*qrSize;
            if(end >= data.length()){
                end = data.length();
            }
            try {
//                alert("test", ""+Math.ceil((double)data.length()/(double)qrSize));
                qrBitmaps.add(generateQrCode(
                    FileEditor.byteToChar(FileEditor.internalDataVersion, FileEditor.lengthHeaderBytes) +
                                String.valueOf(FileEditor.byteToChar(randID, FileEditor.lengthHeaderBytes)) +
                                FileEditor.byteToChar(i, FileEditor.lengthHeaderBytes) +
                                FileEditor.byteToChar(qrCount - 1, FileEditor.lengthHeaderBytes) +
                                data.substring(start, end)
                ));
//                alert("title", ""+(qrCount-1));
            }catch (WriterException e){
                AlertManager.error(e);
            }
        }
        qrIndex = 0;
        if(timer != null){
            timer.cancel();
        }
        qrLoop();
    }

    private void updateQr(){
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

    private void qrLoop(){
        timer = new CountDownTimer(minQrSpeed-Math.abs(qrDelay)+1, 1000) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                updateQr();
                qrLoop();
            }
        }.start();
    }
}
