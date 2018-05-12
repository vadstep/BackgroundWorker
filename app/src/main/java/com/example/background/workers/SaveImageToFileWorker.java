package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.background.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.work.Data;
import androidx.work.Worker;

public class SaveImageToFileWorker extends Worker {
    private static final String TAG = SaveImageToFileWorker.class.getSimpleName();

    private static final String TITLE = "Blurred Image";
    private static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault());

    @NonNull
    @Override
    public WorkerResult doWork() {
        Context applicationContext = getApplicationContext();
        WorkerUtils.makeStatusNotification("Doing SaveImageToFileWorker", applicationContext);
        WorkerUtils.sleep();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI, null);
        ContentResolver resolver = applicationContext.getContentResolver();
        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }
            Bitmap bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));
            String imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, TITLE, DATE_FORMATTER.format(new Date()));
            if (TextUtils.isEmpty(imageUrl)) {
                Log.e(TAG, "Writing to MediaStore failed");
                return WorkerResult.FAILURE;
            }
            setOutputData(new Data.Builder().putString(
                    Constants.KEY_IMAGE_URI, resourceUri).build());
            return WorkerResult.SUCCESS;
        } catch (Exception exception) {
            Log.e(TAG, "Unable to save image to Gallery", exception);
            return WorkerResult.FAILURE;
        }
    }
}
