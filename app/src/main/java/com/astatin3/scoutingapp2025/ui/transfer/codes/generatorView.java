package com.astatin3.scoutingapp2025.ui.transfer.codes;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.astatin3.scoutingapp2025.databinding.FragmentTransferBinding;
import com.astatin3.scoutingapp2025.utility.fileEditor;
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

public class generatorView extends ConstraintLayout {
    private FragmentTransferBinding binding;
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

    public generatorView(Context context) {
        super(context);
    }

    public generatorView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    private void alert(String title, String content) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setMessage(content);
        alert.setTitle(title);
        alert.setPositiveButton("OK", null);
        alert.setCancelable(true);
        alert.create().show();
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
            e.printStackTrace();
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

    public void start(FragmentTransferBinding binding, String inputData){
        start(binding, inputData.getBytes(StandardCharsets.ISO_8859_1));
    }
    public void start(FragmentTransferBinding binding, byte[] inputData){
        qrImage = binding.qrImage;
        qrSpeedSlider = binding.qrSpeedSlider;
        qrSizeSlider = binding.qrSizeSlider;
        qrIndexN = binding.qrIndexN;
        qrIndexD = binding.qrIndexD;


        String compiledData = "";

        for(int i=0;i<Math.ceil((double) inputData.length / fileEditor.maxCompressedBlockSize);i++){
            final int start = i*fileEditor.maxCompressedBlockSize;
            int end = ((i+1)*fileEditor.maxCompressedBlockSize);
            if(end > inputData.length) {
                end = inputData.length;
            }

            byte[] dataBlock = fileEditor.getByteBlock(inputData, start, end);

            final String compressedBlock =
                new String(
                    fileEditor.compress(dataBlock),
                        StandardCharsets.ISO_8859_1);

            compiledData +=
                new String(
                    fileEditor.toBytes(compressedBlock.length(), 2),
                        StandardCharsets.ISO_8859_1) +

                    compressedBlock;


        }

        if(compiledData.isEmpty()){
            alert("Error!", "Empty data!");
            return;
        }

        minQrSize = Math.round(compiledData.length()/maxQrCount)+1;

        qrSizeSlider.setMax(maxQrSize-minQrSize);
        qrSpeedSlider.setMax((minQrSpeed-maxQrSpeed)*2);

        qrSizeSlider.setProgress(minQrSize+qrSize);
        qrSpeedSlider.setProgress(defaultQrDelay+5);

        sendData(compiledData);
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
                    fileEditor.byteToChar(fileEditor.internalDataVersion) +
                                String.valueOf(fileEditor.byteToChar(randID)) +
                                fileEditor.byteToChar(i) +
                                fileEditor.byteToChar(qrCount - 1) +
                                data.substring(start, end)
                ));
//                alert("title", ""+(qrCount-1));
            }catch (WriterException e){
                e.printStackTrace();
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
