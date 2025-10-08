package com.ridgebotics.ridgescout.ui.transfer.codes;

import static androidx.core.math.MathUtils.clamp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.ridgebotics.ridgescout.databinding.FragmentTransferCodeReceiverBinding;
import com.ridgebotics.ridgescout.types.ScoutingFile;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.BuiltByteParser;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.google.common.util.concurrent.ListenableFuture;
import com.ridgebotics.ridgescout.utility.TaskRunner;

import org.checkerframework.checker.units.qual.C;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Recieves data from the camera, scanning codes.
public class CodeScannerView extends Fragment {
    private CodeOverlayView CodeOverlayView;
    private Handler uiHandler;


    private void alert(String title, String content) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setMessage(content);
        alert.setTitle(title);
        alert.setPositiveButton("OK", null);
        alert.setCancelable(true);
        alert.create().show();
    }

    private final int downscale = 1;
    private LifecycleOwner lifecycle;

    private void setImage(Bitmap bmp){
        scanQRCode(bmp);
        binding.scannerImage.setImageBitmap(bmp);
    }

//    private Bitmap img

    int[] levelMap = new int[256];
    private void recalcMap(){
        for (int i = 0; i < 256; i++) {
            levelMap[i] = clamp(
                (clamp(
                    i-thresholdOffset, 0, 255) / (256 / numColors)) * (256 / numColors
                )+brightness, 0, 255
            );
        }
    }
    private Bitmap toGreyscale(Image image){
        // Turns out the "Y" In YUV is the Luminance of the pixel.
        // Makes converting to greyscale 1000x easier
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        final int width = image.getWidth();
        final int height = image.getHeight();

        int[] pixels = new int[width * height];
        for (int i = 0; i < width*height; i++) {
            int L = levelMap[yBuffer.get(i) & 0xff];
            pixels[i] = 0xff000000 | (L << 16) | (L << 8) | L;
        }

        Matrix matrix = new Matrix();

        matrix.postRotate(90);


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
//        Bitmap.rota

        return bitmap;
    }
    public void scanQRCode(Bitmap bitmap) {

//        CodeScanTask async = new CodeScanTask();
        new TaskRunner().executeAsync(new CodeScanTask(bitmap), data -> {
            if(data != null){
//                    alert("test", ""+fileEditor.byteFromChar(data.charAt(0)));
                compileData(
                        FileEditor.byteFromChar(data.charAt(0)),
                        FileEditor.byteFromChar(data.charAt(1)),
                        FileEditor.byteFromChar(data.charAt(2)),
                        (FileEditor.byteFromChar(data.charAt(3))+1),
                        data.substring(4)
                );
            }
        });


//        return contents;
    }






    private int numColors = 3;
    private int thresholdOffset = 128;
    private int brightness = 128;

    private FragmentTransferCodeReceiverBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                     @Nullable Bundle savedInstanceState) {

        binding = FragmentTransferCodeReceiverBinding.inflate(inflater, container, false);

        this.lifecycle = getViewLifecycleOwner();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        }


        uiHandler = new Handler();


        binding.scannerThreshold.setProgress(thresholdOffset);
        binding.scannerThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thresholdOffset = 127-progress;
                recalcMap();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        binding.scannerThreshold.setMax(255);

        binding.scannerColors.setProgress(numColors);
        binding.scannerColors.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numColors = 18-(progress-2);
                recalcMap();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        binding.scannerColors.setMax(18);
        binding.scannerBrightness.setProgress(brightness);
        binding.scannerBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness = progress-128;
                recalcMap();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        binding.scannerBrightness.setMax(256);


        recalcMap();

        CodeOverlayView = new CodeOverlayView(getContext());
        CodeOverlayView.bringToFront();
        ConstraintLayout.LayoutParams pointsOverlayViewParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT
        );

        CodeOverlayView.setLayoutParams(pointsOverlayViewParams);
        binding.container.addView(CodeOverlayView);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture
                = ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getContext()));

        return binding.getRoot();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)

//                .addCameraFilter(CameraFilters.NON)
                .build();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setOutputImageRotationEnabled(true)
//                .setTargetRotation(Surface.ROTATION_0)
                .build();

        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class) @Override
            public void analyze(@NonNull ImageProxy image) {
                Image img = Objects.requireNonNull(image.getImage());
                uiHandler.post(new Runnable() {
                    final Bitmap bmp = toGreyscale(img);

                    @Override
                    public void run() {
//                        setImage(toGreyscale(bmp));
                        setImage(bmp);
                    }
                });
                image.close();
            }
        });

        cameraProvider.unbindAll();
//        cameraProvider.ro

        cameraProvider.bindToLifecycle(lifecycle,
                cameraSelector, imageAnalysis, preview);

//        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

    }

    private String[] qrDataArr;
    private int qrScannedCount;
    private int[] barColors;
    private int randID;
    private int prevQrIndex;
    private void compileData(int dataVersion, int randID, int qrIndex, int qrCount, String qrData){
        if(dataVersion != FileEditor.internalDataVersion){
            alert("Error", "Incorrect data version ("+dataVersion+" != "+ FileEditor.internalDataVersion+")");
            return;
        }

        // Reset code array if ID Changes
        if(randID != this.randID){
            this.randID = randID;
            qrDataArr = new String[qrCount];
            Log.i("title", ""+qrCount);
            barColors = new int[qrCount];
            prevQrIndex = qrIndex;
            qrScannedCount = 0;
        }

        final boolean updated;

        if(qrDataArr[qrIndex] == null) {
            qrDataArr[qrIndex] = qrData;
            updated = true;
            qrScannedCount += 1;
        }else{
            updated = false;
        }

        barColors[prevQrIndex] = 2;
        barColors[qrIndex] = 1;
        CodeOverlayView.setBar(barColors);

        if(updated && qrScannedCount >= qrCount){

            AlertManager.startLoading("Decoding data...");
            new TaskRunner().executeAsync(new CodeDecodeTask(), result -> {
                AlertManager.stopLoading();
            });

        }
        prevQrIndex = qrIndex;
    }

    private class CodeDecodeTask implements Callable<Void> {
        @Override
        public Void call() {
            String compiledString = "";
            for(int i=0;i<qrDataArr.length;i++){
                compiledString += qrDataArr[i];
            }

            try {
                byte[] compiledBytes = compiledString.getBytes(StandardCharsets.ISO_8859_1);
                byte[] resultBytes = FileEditor.blockUncompress(compiledBytes);


                String result_filenames = "";

                BuiltByteParser bbp = new BuiltByteParser(resultBytes);
                ArrayList<BuiltByteParser.parsedObject> result = bbp.parse();

                for(int i = 0; i < result.size(); i++){
                    if(result.get(i).getType() != ScoutingFile.typecode) continue;
                    ScoutingFile f = ScoutingFile.decode((byte[]) result.get(i).get());

                    if(f != null)
                        if(f.write())
                            result_filenames += f.filename + "\n";
                }

                AlertManager.alert("Completed!", result_filenames);

            }catch (Exception e){
                AlertManager.error(e);
            }

            return null;
        }
    }
}
