package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.background.Constants;

import java.io.FileNotFoundException;

import androidx.work.Data;
import androidx.work.Worker;

public class BlurWorker extends Worker {
    private static final String TAG = BlurWorker.class.getSimpleName();

    @NonNull
    @Override
    public WorkerResult doWork() {

        Context appContext = getApplicationContext();
        WorkerUtils.makeStatusNotification("Doing BlurWorker", appContext);
        WorkerUtils.sleep();
        String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI, null);
        try {
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }

            ContentResolver resolver = appContext.getContentResolver();
            // Create a bitmap
            Bitmap picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));

            Bitmap out = WorkerUtils.blurBitmap(picture, appContext);

            Uri outputUri = WorkerUtils.writeBitmapToFile(appContext, out);
            setOutputData(new Data.Builder().putString(
                    Constants.KEY_IMAGE_URI, outputUri.toString()).build());
            return WorkerResult.SUCCESS;
        } catch (FileNotFoundException e) {

            Log.d(TAG, "Error applying blur", e);
            return WorkerResult.FAILURE;
        }

    }
}
