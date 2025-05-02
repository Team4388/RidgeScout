package com.ridgebotics.ridgescout.ui.transfer.codes;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CodeScanTask extends AsyncTask<String, String, String>{
    private Function<String, String> resultFunction = null;
    private Bitmap image;

    @Override
    protected String doInBackground(String... str) {
        if(image == null){return null;}

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
//        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.of(BarcodeFormat.DATA_MATRIX));

        Reader reader = new DataMatrixReader();
        try {
            Result result = reader.decode(binaryBitmap, hints);
            return result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
//            AlertManager.error(e);
        }

        return null;
    }
    public void setImage(Bitmap image){this.image = image;}
    public void onResult(Function<String, String> func) {
        this.resultFunction = func;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(resultFunction != null){
            resultFunction.apply(result);
        }
    }
}

