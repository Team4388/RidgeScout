package com.ridgebotics.ridgescout.utility;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SharePrompt {
    public static void shareContent(Context context, String fileName, String content, String mimeType) {
        shareContent(context, fileName, content.getBytes(), mimeType);
    }

    public static void shareContent(Context context, String fileName, byte[] content, String mimeType) {
        try {
            File file = new File(context.getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content);
            fos.close();

            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Share using"));
        } catch (IOException e) {
            AlertManager.error(e);
        }
    }
}
