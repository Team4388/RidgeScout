package com.ridgebotics.ridgescout.ui.transfer.codes;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.ridgebotics.ridgescout.utility.TaskRunner;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class CodeGenTask implements Callable<List<Bitmap>> {
//    private Function<List<Bitmap>, String> resultFunction = null;
//
//    @Override
//    protected List<Bitmap> doInBackground(String... strings) {
//

//
//        return new ArrayList<>();
//    }
//
//
//    public void onResult(Function<List<Bitmap>, String> func) {
//        this.resultFunction = func;
//    }
//
//
//    @Override
//    protected void onPostExecute(List<Bitmap> result) {
//        super.onPostExecute(result);
//        if(resultFunction != null){
//            resultFunction.apply(result);
//        }
//    }

    private final String data;
    private final int randID;
    private final int qrSize;
    private final int qrCount;

    public CodeGenTask(String data, int randID, int qrSize, int qrCount) {
        this.data = data;
        this.randID = randID;
        this.qrSize = qrSize;
        this.qrCount = qrCount;
    }

    @Override
    public List<Bitmap> call() {
        List<Bitmap> qrBitmaps = new ArrayList<>();

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

        return qrBitmaps;
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
}
